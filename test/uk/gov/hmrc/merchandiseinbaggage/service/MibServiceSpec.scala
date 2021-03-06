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

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, DeclarationGoods, DeclarationId, NotRequired, Paid}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{GoodsEntries, ThresholdAllowance}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.viewmodels.DeclarationView
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import com.softwaremill.quicklens._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MibServiceSpec extends BaseSpecWithApplication with CoreTestData with MockFactory {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val mockConnector = mock[MibConnector]
  private val service = new MibService(mockConnector)

  "retrieve payment calculations from mib backend" in {
    val stubbedResult =
      CalculationResult(aGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val expected = CalculationResponse(CalculationResults(List(stubbedResult)), WithinThreshold)

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(expected.results.calculationResults.map(_.goods.calculationRequest(GreatBritain)), *)
      .returning(Future.successful(CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)))

    service.paymentCalculations(Seq(aGoods), GreatBritain).futureValue mustBe expected
  }

  "check if over threshold for amend journey" in {
    val stubbedResult =
      CalculationResult(aGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val amended = completedImportJourneyWithGoodsOverThreshold
      .copy(journeyType = Amend)

    (mockConnector
      .calculatePaymentsAmendPlusExisting(_: CalculationAmendRequest)(_: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)))

    service.amendPlusOriginalCalculations(amended).value.futureValue.get.thresholdCheck mustBe WithinThreshold
  }

  "check threshold allowance" in {
    val entries = completedGoodsEntries(Import)
    val stubbedResult =
      CalculationResult(aImportGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val calculationResponse = CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(calculationResponse))

    val actual = service.thresholdAllowance(Some(GreatBritain), entries, New, aDeclarationId).value.futureValue
    actual mustBe Some(ThresholdAllowance(DeclarationGoods(List(aImportGoods)), calculationResponse, GreatBritain))
  }

  "check threshold allowance including existing declaration for amends" in {
    val entries: GoodsEntries = completedGoodsEntries(Import)
    val declarationId = aDeclarationId
    val existingDeclaration = declaration.modify(_.amendments.each.paymentStatus).setTo(Some(Paid))
    val stubbedResult =
      CalculationResult(aImportGoods, AmountInPence(7835), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val calculationResponse = CalculationResponse(CalculationResults(Seq(stubbedResult)), WithinThreshold)

    (mockConnector
      .findDeclaration(_: DeclarationId)(_: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(Some(existingDeclaration)))

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(calculationResponse))

    val actual = service.thresholdAllowance(Some(GreatBritain), entries, Amend, declarationId).value.futureValue
    actual mustBe Some(
      ThresholdAllowance(
        DeclarationGoods(aImportGoods +: DeclarationView.allGoods(existingDeclaration)),
        calculationResponse,
        GreatBritain))
  }

  s"add only goods in $Paid or $NotRequired status" in {
    val declarationId = aDeclarationId
    val unknown = completedAmendment(Import).modify(_.paymentStatus).setTo(None)
    val expectedGoods = Seq(aImportGoods)
    val plusUnsetStatus = declaration
      .copy(declarationId = declarationId)
      .modify(_.amendments)
      .using(_ ++ Seq(unknown))

    (mockConnector
      .findDeclaration(_: DeclarationId)(_: HeaderCarrier))
      .expects(*, *)
      .returning(Future.successful(Some(plusUnsetStatus)))

    service.addGoods(Amend, declarationId, expectedGoods).value.futureValue mustBe Some(
      expectedGoods ++ DeclarationView.allGoods(plusUnsetStatus))
  }

  s"send a request for calculation including declared goods plus amendments goods" in {
    val amendments = aAmendment :: aAmendmentPaid :: aAmendmentNotRequired :: Nil
    val foundDeclaration = declaration.modify(_.amendments).setTo(amendments)
    val expectedTotalGoods = foundDeclaration.declarationGoods.goods ++ aAmendmentPaid.goods.goods ++ aAmendmentNotRequired.goods.goods

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(where { (calculationRequests: Seq[CalculationRequest], _: HeaderCarrier) =>
        calculationRequests
          .map(_.goods) == expectedTotalGoods
      })
      .returning(Future.successful(aCalculationResponse))

    val actual = service.thresholdAllowance(foundDeclaration).futureValue
    actual mustBe a[ThresholdAllowance]
  }

  s"send a request for calculation including declared goods plus amendments goods for export" in {
    val amendments = aAmendment :: aAmendmentPaid :: aAmendmentNotRequired :: Nil
    val foundDeclaration = declaration
      .modify(_.declarationType)
      .setTo(Export)
      .modify(_.amendments)
      .setTo(amendments)
    val expectedTotalGoods = foundDeclaration.declarationGoods.goods ++ amendments.map(_.goods.goods)

    (mockConnector
      .calculatePayments(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(where { (calculationRequests: Seq[CalculationRequest], _: HeaderCarrier) =>
        calculationRequests
          .map(_.goods)
          .size == expectedTotalGoods.size
      })
      .returning(Future.successful(aCalculationResponse))

    val actual = service.thresholdAllowance(foundDeclaration).futureValue
    actual mustBe a[ThresholdAllowance]
  }
}
