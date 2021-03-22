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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.AgentDetailsView

import scala.concurrent.ExecutionContext.Implicits.global

class AgentDetailsControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[AgentDetailsView]
  val controller: DeclarationJourney => AgentDetailsController =
    declarationJourney => new AgentDetailsController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  private val journey: DeclarationJourney = startedImportJourney

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.AgentDetailsController.onPageLoad().url)
      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages("agentDetails.title"))
      result must include(messages("agentDetails.heading"))
      result must include(messages("agentDetails.hint"))
    }
  }

  "onSubmit" should {
    s"redirect to ${routes.EnterAgentAddressController.onPageLoad().url} for import submit" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.AgentDetailsController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "business-name")

      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit()(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.EnterAgentAddressController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.AgentDetailsController.onSubmit().url)
        .withFormUrlEncodedBody("value123" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages("agentDetails.title"))
      result must include(messages("agentDetails.heading"))
    }
  }
}
