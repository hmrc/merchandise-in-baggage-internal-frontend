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

import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.{AgentDetailsController, EoriNumberController}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation.CustomsAgentRequest
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.CustomsAgentView

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class CustomsAgentControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  val view = app.injector.instanceOf[CustomsAgentView]
  val mockNavigator = mock[Navigator]
  val controller: DeclarationJourney => CustomsAgentController =
    declarationJourney =>
      new CustomsAgentController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), mockNavigator, view)

  private val journey: DeclarationJourney = startedImportJourney

  forAll(declarationTypesTable) { importOrExport =>
    "onPageLoad" should {
      s"return 200 with radio buttons on $importOrExport answer" in {
        val request = buildGet(routes.CustomsAgentController.onPageLoad.url)
        val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi("customsAgent.title"))
        result must include(messageApi("customsAgent.heading"))
        result must include(messageApi("customsAgent.hint"))
      }

      s"show required error on form submit without answering for $importOrExport" in {
        val request = buildGet(routes.CustomsAgentController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "")

        val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit(request)
        status(eventualResult) mustBe 400
        contentAsString(eventualResult) must include(messageApi("customsAgent.error.required"))
      }

      s"return 400 with any form errors $importOrExport" in {
        val request = buildGet(routes.CustomsAgentController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "in valid")

        val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 400
        result must include(messageApi("error.summary.title"))
        result must include(messageApi("customsAgent.title"))
        result must include(messageApi("customsAgent.heading"))
      }
    }
  }

  "onSubmit" should {
    s"redirect to ${routes.AgentDetailsController.onPageLoad()} on submit if answer is Yes" in {
      val request = buildGet(routes.CustomsAgentController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "Yes")

      (mockNavigator
        .nextPage(_: CustomsAgentRequest)(_: ExecutionContext))
        .expects(*, *)
        .returning(Future successful AgentDetailsController.onPageLoad())
        .once()

      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(AgentDetailsController.onPageLoad().url)
    }

    s"redirect to ${routes.EoriNumberController.onPageLoad()} on submit if answer is No" in {
      val request = buildGet(routes.CustomsAgentController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "No")

      (mockNavigator
        .nextPage(_: CustomsAgentRequest)(_: ExecutionContext))
        .expects(*, *)
        .returning(Future successful EoriNumberController.onPageLoad())
        .once()

      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(EoriNumberController.onPageLoad().url)
    }
  }
}
