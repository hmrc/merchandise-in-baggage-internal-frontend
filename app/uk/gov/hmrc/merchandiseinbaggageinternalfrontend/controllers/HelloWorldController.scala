/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.actions.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.connectors.StrideAuthConnector
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.{ErrorTemplate, HelloWorldPage}

import scala.concurrent.Future

@Singleton
class HelloWorldController @Inject()(
                                    configuration: Configuration,
                                    environment: Environment,
                                    authConnector: StrideAuthConnector,
  implicit val appConfig: AppConfig,
  mcc: MessagesControllerComponents,
  helloWorldPage: HelloWorldPage, errorTemplate: ErrorTemplate)
    extends StrideAuthAction(configuration, environment, authConnector, mcc, errorTemplate) {

  val helloWorld: Action[AnyContent] =
    asyncForAuthRequestWithDefaultBody(
      adminRole
    ) { implicit request =>
      Future.successful(Ok(helloWorldPage()))
    }

}
