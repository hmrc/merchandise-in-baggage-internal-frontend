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

import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{CategoryQuantityOfGoods, DeclarationJourney, DeclarationType, GoodsEntries, GoodsEntry, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.GoodsVatRateView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsVatRateControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[GoodsVatRateView]
  val controller: DeclarationJourney => GoodsVatRateController =
    declarationJourney => new GoodsVatRateController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  private val journey: DeclarationJourney = DeclarationJourney(
    SessionId("123"),
    DeclarationType.Import,
    goodsEntries = GoodsEntries(Seq(GoodsEntry(maybeCategoryQuantityOfGoods = Some(CategoryQuantityOfGoods("clothes", "1")))))
  )

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.GoodsVatRateController.onPageLoad(1).url)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages("goodsVatRate.title", "clothes"))
      result must include(messages("goodsVatRate.heading", "clothes"))
      result must include(messages("goodsVatRate.p"))
      result must include(messages("goodsVatRate.Zero"))
      result must include(messages("goodsVatRate.Five"))
      result must include(messages("goodsVatRate.Twenty"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.GoodsVatRateController.onSubmit(1).url)
        .withFormUrlEncodedBody("value" -> "Zero")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(1)(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.SearchGoodsCountryController.onPageLoad(1).url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.GoodsVatRateController.onSubmit(1).url)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages("goodsVatRate.title", "clothes"))
      result must include(messages("goodsVatRate.heading", "clothes"))
    }
  }
}
