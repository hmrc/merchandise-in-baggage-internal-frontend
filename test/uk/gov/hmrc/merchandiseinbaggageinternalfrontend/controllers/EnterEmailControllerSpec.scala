/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, DeclarationType, SessionId, YesNo}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.EnterEmailView

import scala.concurrent.ExecutionContext.Implicits.global

class EnterEmailControllerSpec extends BaseSpecWithApplication {

  val view = app.injector.instanceOf[EnterEmailView]
  val controller = new EnterEmailController(component, actionProvider, repo, view)

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeIsACustomsAgent = Some(YesNo.No)))

      val request = FakeRequest(GET, routes.EnterEmailController.onPageLoad.url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

      val eventualResult = controller.onPageLoad(request)
      status(eventualResult) mustBe 200
      contentAsString(eventualResult) must include(messageApi("enterEmail.title"))
      contentAsString(eventualResult) must include(messageApi("enterEmail.heading"))
      contentAsString(eventualResult) must include(messageApi("enterEmail.hint"))
      contentAsString(eventualResult) must include(messageApi("enterEmail.email"))
      contentAsString(eventualResult) must include(messageApi("enterEmail.confirmation"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeIsACustomsAgent = Some(YesNo.No)))
      val request = FakeRequest(GET, routes.EnterEmailController.onSubmit().url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody("email" -> "s@s.s", "confirmation" -> "s@s.s")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.JourneyDetailsController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeIsACustomsAgent = Some(YesNo.No)))
      val request = FakeRequest(GET, routes.EnterEmailController.onSubmit().url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody("email1" -> "s@s.s", "confirmation" -> "")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 400

      contentAsString(eventualResult) must include(messageApi("enterEmail.title"))
      contentAsString(eventualResult) must include(messageApi("enterEmail.heading"))
      contentAsString(eventualResult) must include(messageApi("enterEmail.hint"))
      contentAsString(eventualResult) must include(messageApi("enterEmail.email"))
      contentAsString(eventualResult) must include(messageApi("enterEmail.confirmation"))
    }
  }
}
