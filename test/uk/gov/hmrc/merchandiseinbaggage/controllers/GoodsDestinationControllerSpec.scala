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
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.{ExciseAndRestrictedGoodsController, GoodsDestinationController}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation.GoodsDestinationRequest
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsDestinationView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class GoodsDestinationControllerSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables with MockFactory {

  val view = injector.instanceOf[GoodsDestinationView]
  val mockNavigator: Navigator = mock[Navigator]

  def controller(declarationJourney: DeclarationJourney) =
    new GoodsDestinationController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      mockNavigator,
      view)

  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport)
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        val request = buildGet(GoodsDestinationController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"goodsDestination.$importOrExport.title"))
        result must include(messageApi(s"goodsDestination.$importOrExport.heading"))
        result must include(messageApi("goodsDestination.NorthernIreland"))
        result must include(messageApi("goodsDestination.GreatBritain"))
      }
    }

    "onSubmit" should {
      s"redirect for $importOrExport using Navigator" in {
        val request = buildPost(GoodsDestinationController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "GreatBritain")

        (mockNavigator
          .nextPage(_: GoodsDestinationRequest)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future.successful(ExciseAndRestrictedGoodsController.onPageLoad()))
          .once()

        controller(journey).onSubmit(request).futureValue
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      val request = buildPost(GoodsDestinationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
    }
  }
}
