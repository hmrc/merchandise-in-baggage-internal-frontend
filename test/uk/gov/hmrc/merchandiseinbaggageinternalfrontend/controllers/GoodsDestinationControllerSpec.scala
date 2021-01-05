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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.GoodsDestinationView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsDestinationControllerSpec extends BaseSpecWithApplication {

  val view = app.injector.instanceOf[GoodsDestinationView]
  val controller = new GoodsDestinationController(component, actionProvider, repo, view)

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))

      val request = buildGet(routes.GoodsDestinationController.onPageLoad.url)

      val eventualResult = controller.onPageLoad(request)
      status(eventualResult) mustBe 200
      contentAsString(eventualResult) must include(messageApi("goodsDestination.Import.title"))
      contentAsString(eventualResult) must include(messageApi("goodsDestination.Import.heading"))
      contentAsString(eventualResult) must include(messageApi("goodsDestination.NorthernIreland"))
      contentAsString(eventualResult) must include(messageApi("goodsDestination.GreatBritain"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit with GreatBritain" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))
      val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)

        .withFormUrlEncodedBody("value" -> "GreatBritain")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.ExciseAndRestrictedGoodsController.onPageLoad().url)
    }

    "redirect to /cannot-use-service-ireland after successful form submit with NorthernIreland" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))
      val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)

        .withFormUrlEncodedBody("value" -> "NorthernIreland")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.CannotUseServiceIrelandController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))
      val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)

        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 400

      contentAsString(eventualResult) must include(messageApi("error.summary.title"))
      contentAsString(eventualResult) must include(messageApi("goodsDestination.Import.title"))
      contentAsString(eventualResult) must include(messageApi("goodsDestination.NorthernIreland"))
      contentAsString(eventualResult) must include(messageApi("goodsDestination.GreatBritain"))
    }
  }
}
