/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.HelloWorldPage

class HelloWorldControllerSpec extends BaseSpecWithApplication with Status{
  private lazy val mcc = injector.instanceOf[MessagesControllerComponents]
  private lazy val strideAuthAction = injector.instanceOf[StrideAuthAction]
  private lazy val appConfig = injector.instanceOf[AppConfig]
  private lazy val helloWorldPage: HelloWorldPage = injector.instanceOf[HelloWorldPage]
  private lazy val controller = new HelloWorldController(appConfig, strideAuthAction, mcc, helloWorldPage)

  private val request: FakeRequest[AnyContentAsEmpty.type] = buildGet(routes.HelloWorldController.helloWorld().url)

  "hello world" should {
    "return OK and display" when {
      "the user is authenticated and authorised" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val result = controller.helloWorld(request)
        status(result) mustBe OK
      }
    }

    "redirect to the login page" when {
      "the user is not authenticated" in {
        givenTheUserIsNotAuthenticated()
        val result = controller.helloWorld(request)
        status(result) mustBe SEE_OTHER
      }

      "the auth call fails with an internal error" in {
        givenAuthFailsWith("Boom")
        val result = controller.helloWorld(request)
        status(result) mustBe SEE_OTHER
      }

      "the user has no credentials" in {
        givenTheUserHasNoCredentials()
        val result = controller.helloWorld(request)
        status(result) mustBe SEE_OTHER
      }
    }

    "return 403" when {
      "the user is authenticated but does not have the required role" in {
        givenAuthFailsWith("UnsupportedCredentialRole")
        val result = controller.helloWorld(request)
        status(result) mustBe FORBIDDEN
      }
    }
  }
}
