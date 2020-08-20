/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.{AuthWireMockResponses, BaseSpecWithApplication}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.HelloWorldPage

class HelloWorldControllerSpec extends BaseSpecWithApplication {

  val helloWorldPage: HelloWorldPage = app.injector.instanceOf[HelloWorldPage]

  private val controller = new HelloWorldController(appConfig, strideAuthAction, mcc, helloWorldPage)

  "GET when logged in with stride and with mib role" should {
    "return 200" in {
      AuthWireMockResponses.authorised("PrivilegedApplication", "userId")
      val result = controller.helloWorld(buildGet(routes.HelloWorldController.helloWorld().url))
      status(result) mustBe Status.OK
    }
  }
}
