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
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.TravellerDetailsPage

import scala.concurrent.ExecutionContext.Implicits.global

class TravellerDetailsControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = app.injector.instanceOf[TravellerDetailsPage]
  val controller: DeclarationJourney => TravellerDetailsController =
    declarationJourney => new TravellerDetailsController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  forAll(declarationTypes) { importOrExport =>
    val journey: DeclarationJourney = startedImportJourney.copy(declarationType = importOrExport, maybeIsACustomsAgent = Some(YesNo.No))
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.TravellerDetailsController.onPageLoad().url)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi("travellerDetails.title"))
        result must include(messageApi("travellerDetails.heading"))
        result must include(messageApi("travellerDetails.hint"))
        result must include(messageApi("travellerDetails.firstName"))
        result must include(messageApi("travellerDetails.lastName"))
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val request = buildGet(routes.TravellerDetailsController.onSubmit().url)
          .withFormUrlEncodedBody("firstName" -> "Foo", "lastName" -> "Bar")

        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.EnterEmailController.onPageLoad().url)
      }

      s"return 400 with required form errors for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val request = buildGet(routes.EoriNumberController.onSubmit().url)
          .withFormUrlEncodedBody("firstName" -> "", "lastName" -> "")

        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 400
        result must include(messageApi("travellerDetails.title"))
        result must include(messageApi("travellerDetails.heading"))
        result must include(messageApi("travellerDetails.hint"))
        result must include(messageApi("travellerDetails.firstName"))
        result must include(messageApi("travellerDetails.lastName"))
        result must include(messageApi("travellerDetails.firstName.error.required"))
        result must include(messageApi("travellerDetails.lastName.error.required"))
      }
    }
  }
}
