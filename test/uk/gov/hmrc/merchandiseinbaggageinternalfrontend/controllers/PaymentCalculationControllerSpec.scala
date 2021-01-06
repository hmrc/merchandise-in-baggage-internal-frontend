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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.CurrencyConversionSupport.givenSuccessfulCurrencyConversionResponse
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.PaymentCalculationView

import scala.concurrent.ExecutionContext.Implicits.global

class PaymentCalculationControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[PaymentCalculationView]
  val controller: DeclarationJourney => PaymentCalculationController =
    declarationJourney => new PaymentCalculationController(component, stubProvider(declarationJourney), calculationService, view)

  "onPageLoad" should {
    "return 200 with expected content" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenSuccessfulCurrencyConversionResponse()

      val journey = DeclarationJourney(
        SessionId("123"),
        DeclarationType.Import,
        maybeGoodsDestination = Some(GoodsDestinations.GreatBritain),
        goodsEntries = GoodsEntries(Seq(completedGoodsEntry))
      )

      val request = buildGet(routes.PaymentCalculationController.onPageLoad().url)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages("paymentCalculation.title", "£18.34"))
      result must include(messages("paymentCalculation.heading", "£18.34"))
      result must include(messages("paymentCalculation.table.col1.head"))
      result must include(messages("paymentCalculation.table.col2.head"))
      result must include(messages("paymentCalculation.table.col3.head"))
      result must include(messages("paymentCalculation.table.col4.head"))
      result must include(messages("paymentCalculation.table.col5.head"))
      result must include(messages("paymentCalculation.table.total"))
      result must include(messages("paymentCalculation.h3"))
    }
  }
}
