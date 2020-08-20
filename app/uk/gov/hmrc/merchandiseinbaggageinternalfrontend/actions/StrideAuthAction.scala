/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.actions

import java.util.NoSuchElementException

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthProviders, AuthorisationException, AuthorisedFunctions, Enrolment, InsufficientEnrolments, NoActiveSession}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.connectors.StrideAuthConnector
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.AuthFrontendController
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.models.auth.{AuthRequest, MultipleRoleException}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.models.auth.AuthRequest._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ErrorTemplate
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class StrideAuthAction @Inject()(override val config: Configuration,
                                 override val env: Environment,
                                 override val authConnector: StrideAuthConnector,
                                 mcc: MessagesControllerComponents,
                                 errorTemplate: ErrorTemplate
                                )(implicit appConfig: AppConfig)
extends AuthFrontendController(mcc)
with AuthorisedFunctions
with AuthRedirects
with I18nSupport {

  protected lazy val adminRole: Set[Enrolment] = Set(Enrolment(appConfig.adminRole))

  def asyncForAuthRequestWithDefaultBody(enrolments: Set[Enrolment])(action: AuthRequest[AnyContent] => Future[Result]): Action[AnyContent] =
      asyncForAuthRequest(parse.default)(enrolments)(action)

  def asyncForAuthRequest[A](bodyParser: BodyParser[A])(enrolments: Set[Enrolment])(action: AuthRequest[A] => Future[Result]): Action[A] =
    Action.async[A](bodyParser) { implicit request =>
      authorised(checkStrideRoles(enrolments).and(AuthProviders(PrivilegedApplication)))
        .retrieve[AuthRetrievals](Retrievals.credentials.and(Retrievals.authorisedEnrolments)) { retrievals =>
          val authRequest = AuthRequest(retrievals)
          action(authRequest).map(result => result.withSession(authRequest.session))
        }
        .recover(unauthorized)
    }

  private def checkStrideRoles(enrolments: Set[Enrolment]): Predicate =
    enrolments.reduce { (enrolment: Predicate, acc: Predicate) =>
      enrolment.or(acc)
    }

  private def unauthorized(implicit request: Request[_]): PartialFunction[Throwable, Result] = {

    case _: NoActiveSession =>
      val uri = if (request.host.contains("localhost")) s"http://${request.host}${request.uri}" else s"${request.uri}"
      toStrideLogin(uri)

    case _: MultipleRoleException =>
      Unauthorized(
        errorTemplate(
          "Single role violation",
          "Single role violation",
          "The current implementation prohibits the specification of more than one role"))

    case _: InsufficientEnrolments | _: NoSuchElementException =>
      val messages: Messages = messagesApi.preferred(request)

      val errorTitle   = messages("error.auth.stride.insufficient.enrolments.title")
      val errorHeading = messages("error.auth.stride.insufficient.enrolments.heading")
      val errorMessage = messages("error.auth.stride.insufficient.enrolments.message")
      Unauthorized(errorTemplate(errorTitle, errorHeading, errorMessage))

    case _: AuthorisationException =>
      val messages: Messages = messagesApi.preferred(request)

      val errorTitle   = messages("error.auth.service.unknown.title")
      val errorHeading = messages("error.auth.service.unknown.heading")
      val errorMessage = messages("error.auth.service.unknown.message")
      Unauthorized(errorTemplate(errorTitle, errorHeading, errorMessage))
  }
}
