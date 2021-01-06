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

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.GoodsDestinationView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsDestinationControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[GoodsDestinationView]
  val controller: DeclarationJourney => GoodsDestinationController =
    declarationJourney => new GoodsDestinationController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  private val journey: DeclarationJourney = DeclarationJourney(SessionId("123"), DeclarationType.Import)

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.GoodsDestinationController.onPageLoad.url)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messageApi("goodsDestination.Import.title"))
      result must include(messageApi("goodsDestination.Import.heading"))
      result must include(messageApi("goodsDestination.NorthernIreland"))
      result must include(messageApi("goodsDestination.GreatBritain"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit with GreatBritain" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "GreatBritain")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.ExciseAndRestrictedGoodsController.onPageLoad().url)
    }

    "redirect to /cannot-use-service-ireland after successful form submit with NorthernIreland" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "NorthernIreland")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.CannotUseServiceIrelandController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("goodsDestination.Import.title"))
      result must include(messageApi("goodsDestination.NorthernIreland"))
      result must include(messageApi("goodsDestination.GreatBritain"))
    }
  }
}
