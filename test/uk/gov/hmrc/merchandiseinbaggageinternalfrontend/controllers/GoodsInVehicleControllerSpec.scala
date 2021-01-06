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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.GoodsInVehicleView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsInVehicleControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[GoodsInVehicleView]
  val controller: DeclarationJourney => GoodsInVehicleController =
    declarationJourney => new GoodsInVehicleController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  private val journey: DeclarationJourney =
    DeclarationJourney(SessionId("123"), DeclarationType.Import, goodsEntries = GoodsEntries(Seq(completedGoodsEntry)))

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.GoodsInVehicleController.onPageLoad().url)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad()(request)

      status(eventualResult) mustBe 200
      contentAsString(eventualResult) must include(messages("goodsInVehicle.Import.title"))
      contentAsString(eventualResult) must include(messages("goodsInVehicle.Import.heading"))
    }
  }

  "onSubmit" should {
    "redirect to /vehicle-size after successful form submit with Yes" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.GoodsInVehicleController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "Yes")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.VehicleSizeController.onPageLoad().url)
    }

    "redirect to /check-your-answers after successful form submit with No" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val declarationJourney = DeclarationJourney(
        SessionId("123"),
        DeclarationType.Import,
        goodsEntries = GoodsEntries(Seq(completedGoodsEntry, completedGoodsEntry))
      )

      val request = buildGet(routes.GoodsInVehicleController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "No")
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(declarationJourney)).onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.GoodsInVehicleController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages("goodsInVehicle.Import.title"))
      result must include(messages("goodsInVehicle.Import.heading"))
    }
  }
}
