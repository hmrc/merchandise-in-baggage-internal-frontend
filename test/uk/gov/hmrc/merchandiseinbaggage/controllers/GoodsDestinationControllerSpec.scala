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
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsDestinationView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsDestinationControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[GoodsDestinationView]
  val controller: DeclarationJourney => GoodsDestinationController =
    declarationJourney => new GoodsDestinationController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  forAll(declarationTypes) { importOrExport =>
    val journey: DeclarationJourney = startedImportJourney.copy(declarationType = importOrExport)
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.GoodsDestinationController.onPageLoad.url)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"goodsDestination.$importOrExport.title"))
        result must include(messageApi(s"goodsDestination.$importOrExport.heading"))
        result must include(messageApi("goodsDestination.NorthernIreland"))
        result must include(messageApi("goodsDestination.GreatBritain"))
      }
    }

    "onSubmit" should {
      forAll(goodsDestinationAnswer) { (destination, redirectTo) =>
        s"redirect to $redirectTo after successful form submit with GreatBritain for $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()
          val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)
            .withFormUrlEncodedBody("value" -> destination.entryName)

          val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
          status(eventualResult) mustBe 303
          redirectLocation(eventualResult).get must endWith(redirectTo)
        }
      }

      s"return 400 with any form errors for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "in valid")

        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 400
        result must include(messageApi("error.summary.title"))
        result must include(messageApi(s"goodsDestination.$importOrExport.title"))
        result must include(messageApi("goodsDestination.NorthernIreland"))
        result must include(messageApi("goodsDestination.GreatBritain"))
      }
    }
  }
}
