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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.{GoodsOverThresholdController, PaymentCalculationController}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, OverThreshold, ThresholdCheck, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries}
import uk.gov.hmrc.merchandiseinbaggage.service.MibService
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.PaymentCalculationView
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenExchangeRateURL

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentCalculationControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData with WireMockSupport {

  private val view = app.injector.instanceOf[PaymentCalculationView]
  private lazy val stubbedCalculation: CalculationResponse => MibService = response =>
    new MibService(mibConnector) {
      override def paymentCalculations(goods: Seq[Goods], destination: GoodsDestination)(
        implicit hc: HeaderCarrier): Future[CalculationResponse] =
        Future.successful(response)
  }

  def controller(journey: DeclarationJourney, check: ThresholdCheck = WithinThreshold): PaymentCalculationController =
    new PaymentCalculationController(
      controllerComponents,
      stubProvider(journey),
      stubbedCalculation(aCalculationResponse.copy(thresholdCheck = check)),
      mibConnector,
      view)

  "onPageLoad" should {
    "return 200 with expected content" in {
      givenExchangeRateURL("http://something")

      val journey = DeclarationJourney(
        SessionId("123"),
        DeclarationType.Import,
        maybeGoodsDestination = Some(GoodsDestinations.GreatBritain),
        goodsEntries = GoodsEntries(Seq(completedImportGoods))
      )

      val request = buildGet(routes.PaymentCalculationController.onPageLoad().url)
      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad()(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages("paymentCalculation.greenchannel.p1"))
      result must include(messages("paymentCalculation.greenchannel.p2"))
      result must include(messages("paymentCalculation.greenchannel.p3"))
      result must include(messages("paymentCalculation.title", "£0.12"))
      result must include(messages("paymentCalculation.heading", "£0.12"))
      result must include(messages("paymentCalculation.table.col1.head"))
      result must include(messages("paymentCalculation.table.col2.head"))
      result must include(messages("paymentCalculation.table.col3.head"))
      result must include(messages("paymentCalculation.table.col4.head"))
      result must include(messages("paymentCalculation.table.col5.head"))
      result must include(messages("paymentCalculation.table.total"))
      result must include(messages("paymentCalculation.h3"))
    }

    forAll(declarationTypesTable) { importOrExport =>
      s"redirect to /goods-over-threshold for $importOrExport if its over threshold" in {
        givenExchangeRateURL("http://something")

        val journey =
          DeclarationJourney(
            SessionId("123"),
            importOrExport,
            maybeGoodsDestination = Some(GoodsDestinations.GreatBritain),
            goodsEntries = overThresholdGoods(importOrExport)
          )

        val request = buildGet(PaymentCalculationController.onPageLoad().url, aSessionId)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey), OverThreshold).onPageLoad()(request)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(GoodsOverThresholdController.onPageLoad().url)
      }
    }
  }
}
