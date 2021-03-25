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
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.{GoodsTypeQuantityController, GoodsVatRateController, SearchGoodsCountryController}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsTypeQuantityView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsTypeQuantityControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  private val view = app.injector.instanceOf[GoodsTypeQuantityView]
  val mockNavigator = mock[Navigator]

  val controller: DeclarationJourney => GoodsTypeQuantityController =
    declarationJourney =>
      new GoodsTypeQuantityController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view, mockNavigator)

  forAll(declarationTypesTable) { importOrExport =>
    val journey: DeclarationJourney = startedImportJourney.copy(declarationType = importOrExport)
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.GoodsTypeQuantityController.onPageLoad(1).url)
        val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad(1)(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"goodsTypeQuantity.title"))
        result must include(messageApi(s"goodsTypeQuantity.heading"))
        result must include(messageApi("goodsTypeQuantity.p"))
        result must include(messageApi("goodsTypeQuantity.quantity"))
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit for $importOrExport" in {
        val request = buildPost(GoodsTypeQuantityController.onSubmit(1).url, aSessionId)
          .withFormUrlEncodedBody("category" -> "clothes", "quantity" -> "1")
        val page =
          if (importOrExport == Import)
            GoodsVatRateController.onPageLoad(1)
          else SearchGoodsCountryController.onPageLoad(1)

        (mockNavigator
          .nextPage(_: RequestWithDeclarationType))
          .expects(RequestWithDeclarationType(GoodsTypeQuantityController.onPageLoad(1).url, importOrExport, 1))
          .returning(page)
          .once()

        controller(journey).onSubmit(1)(request).futureValue
      }

      s"return 400 with any form errors for $importOrExport" in {
        val request = buildPost(GoodsTypeQuantityController.onSubmit(1).url, aSessionId)
          .withFormUrlEncodedBody("xyz" -> "clothes", "abc" -> "1")

        val eventualResult = controller(journey).onSubmit(1)(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 400
        result must include(messageApi("error.summary.title"))
        result must include(messageApi(s"goodsTypeQuantity.title"))
        result must include(messageApi(s"goodsTypeQuantity.heading"))
        result must include(messageApi("goodsTypeQuantity.p"))
        result must include(messageApi("goodsTypeQuantity.quantity"))
      }
    }
  }
}
