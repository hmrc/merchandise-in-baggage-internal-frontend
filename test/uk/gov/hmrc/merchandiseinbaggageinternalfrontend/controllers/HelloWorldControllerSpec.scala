/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.connectors.StrideAuthConnector
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.{ErrorTemplate, HelloWorldPage}

class HelloWorldControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  private val fakeRequest = FakeRequest("GET", "/")

  private val env           = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration)
  private val appConfig     = new AppConfig(configuration, serviceConfig)

  val helloWorldPage: HelloWorldPage = app.injector.instanceOf[HelloWorldPage]
  val errorTemplate = app.injector.instanceOf[ErrorTemplate]
  val authConnector = app.injector.instanceOf[StrideAuthConnector]

  private val controller = new HelloWorldController(configuration, env, authConnector, appConfig, stubMessagesControllerComponents(), helloWorldPage, errorTemplate)

//  "GET /" should {
//    "return 200" in {
//      val result = controller.helloWorld(fakeRequest)
//      status(result) shouldBe Status.OK
//    }
//  }
}
