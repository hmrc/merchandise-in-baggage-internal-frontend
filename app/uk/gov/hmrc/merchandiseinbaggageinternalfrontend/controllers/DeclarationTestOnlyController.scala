/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import cats.data.EitherT
import javax.inject.Inject
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.Forms._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.{DeclarationIdResponse, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service.MIBBackendService
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.{DeclarationFoundTestOnlyPage, DeclarationTestOnlyPage}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class DeclarationTestOnlyController @Inject()(mcc: MessagesControllerComponents,
                                              httpClient: HttpClient,
                                              views: DeclarationTestOnlyPage,
                                              declarationFoundView: DeclarationFoundTestOnlyPage,
                                              strideAuth: StrideAuthAction
                                             )
                                             (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with Forms with MIBBackendService {

  def declarations(): Action[AnyContent] = strideAuth.async { implicit request  =>
    Future.successful(Ok(views(declarationForm(declarationFormIdentifier))))
  }

  def findDeclaration(declarationId: DeclarationId): Action[AnyContent] = strideAuth.async { implicit request =>
    declarationById(httpClient,declarationId).map(declaration =>
      Ok(declarationFoundView(declaration))
    ).recover({
      case _ => NotFound("Declaration Not Found")
    })
  }

  def onSubmit(): Action[AnyContent] = strideAuth.async { implicit request  =>
    import cats.instances.future._
    val newDeclaration: EitherT[Future, BusinessError, DeclarationIdResponse] =
      for {
        declarationRequest  <- EitherT.fromOption(Json.parse(bindForm.data(declarationFormIdentifier))
          .asOpt[DeclarationRequest], InvalidDeclarationRequest)
        declarationResponse <- EitherT.liftF(addDeclaration(httpClient, declarationRequest))
      } yield declarationResponse

    newDeclaration.fold ({
      case InvalidDeclarationRequest =>
        InternalServerError("Invalid Request")
      case err                       =>
        InternalServerError(s"$err")
    }, declarationIdResponse =>
      Redirect(routes.DeclarationTestOnlyController.findDeclaration(declarationIdResponse.id))
    ).recover({case err => InternalServerError(s"$err") })
  }

  protected def bindForm(implicit request: Request[_]): Form[DeclarationData] =
    declarationForm(declarationFormIdentifier).bindFromRequest
}



