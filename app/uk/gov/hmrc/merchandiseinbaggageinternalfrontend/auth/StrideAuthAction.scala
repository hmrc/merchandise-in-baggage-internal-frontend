/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth

import javax.inject.Inject
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.Results.Unauthorized
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.credentials
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.{ExecutionContext, Future}

class StrideAuthAction @Inject()(af: AuthorisedFunctions, appConfig: AppConfig, mcc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends ActionBuilder[AuthRequest, AnyContent] with AuthRedirects {
  override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def config: Configuration = appConfig.config

  override def env: Environment = appConfig.env

  override protected def executionContext: ExecutionContext = ec

  val logger: Logger = LoggerFactory.getLogger(getClass)

  override def invokeBlock[A](request: Request[A], block: AuthRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val r: Request[A] = request

    val strideEnrolment = Enrolment(appConfig.strideRole)

    af.authorised(strideEnrolment and AuthProviders(PrivilegedApplication)).retrieve(credentials) {
      case Some(c: Credentials) => block(new AuthRequest(request, c))
      case None =>
        logger.warn("User does not have credentials")
        Future.successful(Unauthorized)
    }.recover {
      case _: NoActiveSession =>
        val uri = if (request.host.contains("localhost")) s"http://${request.host}${request.uri}" else s"${request.uri}"
        toStrideLogin(uri)
      case e: AuthorisationException =>
        logger.warn(s"Unauthorised because of ${e.reason}, $e")
        Unauthorized
    }
  }
}
