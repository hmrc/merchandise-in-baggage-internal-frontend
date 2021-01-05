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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.CustomsAgentView

import scala.concurrent.ExecutionContext.Implicits.global

class CustomsAgentControllerSpec extends BaseSpecWithApplication {

  val view = app.injector.instanceOf[CustomsAgentView]
  val controller = new CustomsAgentController(component, actionProvider, repo, view)

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))

      val request = buildGet(routes.CustomsAgentController.onPageLoad.url)

      val eventualResult = controller.onPageLoad(request)
      status(eventualResult) mustBe 200
      contentAsString(eventualResult) must include(messageApi("customsAgent.title"))
      contentAsString(eventualResult) must include(messageApi("customsAgent.heading"))
      contentAsString(eventualResult) must include(messageApi("customsAgent.hint"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit with Yes" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))
      val request = buildGet(routes.CustomsAgentController.onSubmit().url)

        .withFormUrlEncodedBody("value" -> "Yes")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.AgentDetailsController.onPageLoad().url)
    }

    "redirect to next page after successful form submit with No" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))
      val request = buildGet(routes.CustomsAgentController.onSubmit().url)

        .withFormUrlEncodedBody("value" -> "No")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.EoriNumberController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))
      val request = buildGet(routes.CustomsAgentController.onSubmit().url)

        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 400

      contentAsString(eventualResult) must include(messageApi("error.summary.title"))
      contentAsString(eventualResult) must include(messageApi("customsAgent.title"))
      contentAsString(eventualResult) must include(messageApi("customsAgent.heading"))
    }
  }
}
