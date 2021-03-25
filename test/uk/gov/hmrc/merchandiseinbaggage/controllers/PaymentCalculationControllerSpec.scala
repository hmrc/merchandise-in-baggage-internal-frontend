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

import com.softwaremill.quicklens._
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries}
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.PaymentCalculationView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentCalculationControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  private val view = app.injector.instanceOf[PaymentCalculationView]
  private lazy val stubbedCalculation: CalculationResults => CalculationService = calculationResults =>
    new CalculationService(mibConnector) {
      override def paymentCalculations(importGoods: Seq[ImportGoods])(implicit hc: HeaderCarrier): Future[CalculationResults] =
        Future.successful(calculationResults)
  }

  val controller: DeclarationJourney => PaymentCalculationController =
    declarationJourney =>
      new PaymentCalculationController(component, stubProvider(declarationJourney), stubbedCalculation(aCalculationResults), view)

  "onPageLoad" should {
    "return 200 with expected content" in {
      givenTheUserIsAuthenticatedAndAuthorised()

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
      forAll(paymentCalculationThreshold) { (thresholdValue, redirectTo) =>
        s"redirect to $redirectTo for $importOrExport if threshold is $thresholdValue" in {
          givenTheUserIsAuthenticatedAndAuthorised()

          val journey = DeclarationJourney(
            SessionId("123"),
            DeclarationType.Export,
            maybeGoodsDestination = Some(GoodsDestinations.GreatBritain),
            goodsEntries = GoodsEntries(Seq(completedImportGoods.modify(_.maybePurchaseDetails.each.amount).setTo(thresholdValue)))
          )

          val request = buildGet(routes.PaymentCalculationController.onPageLoad().url)
          val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad()(request)

          status(eventualResult) mustBe 303
          redirectLocation(eventualResult).get must endWith(redirectTo)
        }
      }
    }
  }
}
