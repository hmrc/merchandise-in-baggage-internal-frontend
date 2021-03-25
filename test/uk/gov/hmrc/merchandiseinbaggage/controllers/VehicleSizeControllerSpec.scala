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
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.VehicleSizeView

import scala.concurrent.ExecutionContext.Implicits.global

class VehicleSizeControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = app.injector.instanceOf[VehicleSizeView]
  val controller: DeclarationJourney => VehicleSizeController =
    declarationJourney => new VehicleSizeController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  forAll(declarationTypesTable) { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(SessionId("123"), importOrExport, goodsEntries = dynamicCompletedGoodsEntries(importOrExport))
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.VehicleSizeController.onPageLoad().url)
        val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messages(s"vehicleSize.$importOrExport.title"))
        result must include(messages(s"vehicleSize.$importOrExport.heading"))
        result must include(messages("vehicleSize.hint"))
      }
    }

    forAll(vehicleRegistrationNumberAnswer) { (yesOrNo, redirectTo) =>
      "onSubmit" should {
        s"redirect to $redirectTo after successful form submit with $yesOrNo for $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()

          val request = buildGet(routes.VehicleSizeController.onSubmit().url)
            .withFormUrlEncodedBody("value" -> yesOrNo.toString)
          val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit()(request)

          status(eventualResult) mustBe 303
          redirectLocation(eventualResult).get must endWith(redirectTo)
        }
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.VehicleSizeController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages(s"vehicleSize.$importOrExport.title"))
      result must include(messages(s"vehicleSize.$importOrExport.heading"))
      result must include(messages("vehicleSize.hint"))
    }

    s"return 400 with required form errors for $importOrExport" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.VehicleSizeController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "")

      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages(s"vehicleSize.error.$importOrExport.required"))
    }
  }
}
