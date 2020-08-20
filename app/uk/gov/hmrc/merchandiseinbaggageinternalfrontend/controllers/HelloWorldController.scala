/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.HelloWorldPage

import scala.concurrent.Future

@Singleton
class HelloWorldController @Inject()(
  appConfig: AppConfig,
  strideAuth: StrideAuthAction,
  mcc: MessagesControllerComponents,
  helloWorldPage: HelloWorldPage)
    extends FrontendController(mcc) with I18nSupport {

  implicit val config: AppConfig = appConfig

  val helloWorld: Action[AnyContent] = strideAuth.async { implicit request =>
    Future.successful(Ok(helloWorldPage()))
  }

}
