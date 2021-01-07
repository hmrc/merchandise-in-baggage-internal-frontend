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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.{PurchaseDetailsExportView, PurchaseDetailsImportView}

import scala.concurrent.ExecutionContext.Implicits.global

class PurchaseDetailsControllerSpec extends DeclarationJourneyControllerSpec {

  val importView = app.injector.instanceOf[PurchaseDetailsImportView]
  val exportView = app.injector.instanceOf[PurchaseDetailsExportView]
  val controller: DeclarationJourney => PurchaseDetailsController = declarationJourney =>
    new PurchaseDetailsController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), importView, exportView)

  private val journey: DeclarationJourney = DeclarationJourney(
    SessionId("123"),
    DeclarationType.Import,
    goodsEntries = GoodsEntries(Seq(GoodsEntry(maybeCategoryQuantityOfGoods = Some(CategoryQuantityOfGoods("clothes", "1")))))
  )

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.PurchaseDetailsController.onPageLoad(1).url)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages("purchaseDetails.title", "clothes"))
      result must include(messages("purchaseDetails.heading", "clothes"))
      result must include(messages("purchaseDetails.price.label"))
      result must include(messages("purchaseDetails.currency.label"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(journey)
      val request = buildGet(routes.SearchGoodsCountryController.onSubmit(1).url)
        .withFormUrlEncodedBody("price" -> "20", "currency" -> "EUR")

      val eventualResult = controller(journey).onSubmit(1)(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.ReviewGoodsController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(journey)
      val request = buildGet(routes.SearchGoodsCountryController.onSubmit(1).url)
        .withFormUrlEncodedBody("abcd" -> "in valid")

      val eventualResult = controller(journey).onSubmit(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages("purchaseDetails.title", "clothes"))
      result must include(messages("purchaseDetails.heading", "clothes"))
    }
  }
}
