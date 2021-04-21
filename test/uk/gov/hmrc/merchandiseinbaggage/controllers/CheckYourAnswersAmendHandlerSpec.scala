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

import java.time.LocalDateTime

import cats.data.OptionT
import org.scalamock.scalatest.MockFactory
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, OverThreshold, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.TpsId
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.support.{DeclarationJourneyControllerSpec, PropertyBaseTables}
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersAmendHandlerSpec
    extends DeclarationJourneyControllerSpec with MibConfiguration with MockFactory with PropertyBaseTables {

  lazy val actionBuilder: DeclarationJourneyActionProvider = injector.instanceOf[DeclarationJourneyActionProvider]

  private lazy val importView = injector.instanceOf[CheckYourAnswersAmendImportView]
  private lazy val exportView = injector.instanceOf[CheckYourAnswersAmendExportView]
  private lazy val mockTpsPaymentsService = mock[TpsPaymentsService]
  private lazy val mockCalculationService = mock[CalculationService]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val sessionId = SessionId()
  implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

  val handler = new CheckYourAnswersAmendHandler(
    actionBuilder,
    mockCalculationService,
    mockTpsPaymentsService,
    importView,
    exportView,
  )

  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    "onPageLoad" should {
      s"return Ok with correct page content for $importOrExport" in {
        val id = DeclarationId("abc")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id)

        if (importOrExport == Import)(mockCalculationService
          .isAmendPlusOriginalOverThresholdImport(_: DeclarationJourney)(_: HeaderCarrier))
          .expects(*, *)
          .returning(OptionT.pure[Future](CalculationResponse(aCalculationResults, WithinThreshold)))
          .once()
        else
          (mockCalculationService
            .isAmendPlusOriginalOverThresholdExport(_: DeclarationJourney)(_: HeaderCarrier))
            .expects(*, *)
            .returning(OptionT.pure[Future](CalculationResponse(aCalculationResults, WithinThreshold)))
            .once()

        val amendment = completedAmendment(importOrExport)
        val eventualResult = handler.onPageLoad(journey.copy(declarationType = importOrExport), amendment)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messageApi("checkYourAnswers.amend.title"))
      }

      s"return 303 for goods over threshold for $importOrExport" in {
        val id = DeclarationId("xxx")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(
            sessionId = sessionId,
            declarationType = importOrExport,
            createdAt = created,
            declarationId = id,
            goodsEntries = overThresholdGoods(importOrExport))

        val amendment = completedAmendment(importOrExport)

        if (importOrExport == Import)(mockCalculationService
          .isAmendPlusOriginalOverThresholdImport(_: DeclarationJourney)(_: HeaderCarrier))
          .expects(*, *)
          .returning(OptionT.pure[Future](CalculationResponse(aCalculationResults, OverThreshold)))
          .once()
        else
          (mockCalculationService
            .isAmendPlusOriginalOverThresholdExport(_: DeclarationJourney)(_: HeaderCarrier))
            .expects(*, *)
            .returning(OptionT.pure[Future](CalculationResponse(aCalculationResults, OverThreshold)))
            .once()

        val eventualResult = handler.onPageLoad(journey.copy(declarationType = importOrExport), amendment)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.GoodsOverThresholdController.onPageLoad().url)
      }
    }
  }

  "on submit" should {
    "calculate tax and send payment request to tps for Imports" in {
      val id = DeclarationId("testAmend")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, journeyType = Amend, declarationType = Import, createdAt = created, declarationId = id)

      (mockCalculationService
        .findDeclaration(_: DeclarationId)(_: HeaderCarrier))
        .expects(id, *)
        .returning(Future.successful(Some(declaration.copy(declarationType = Import, declarationId = id))))
        .once()

      (mockCalculationService
        .paymentCalculations(_: Seq[ImportGoods], _: GoodsDestination)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(CalculationResponse(aCalculationResults, WithinThreshold)))
        .once()

      (mockCalculationService
        .amendDeclaration(_: Declaration)(_: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(id))
        .once()

      (mockTpsPaymentsService
        .createTpsPayments(_: String, _: Option[Int], _: Declaration, _: CalculationResults)(_: HeaderCarrier))
        .expects("someid", Some(1), *, *, *)
        .returning(Future.successful(TpsId("someid")))
        .once()

      val amendment = completedAmendment(Import)
      val eventualResult = handler.onSubmit(importJourney.declarationId, "someid", amendment)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://localhost:9124/tps-payments/make-payment/mib/someid")
    }
  }
}
