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

import com.softwaremill.quicklens._
import org.scalamock.scalatest.MockFactory
import play.api.mvc.{Request, RequestHeader}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.TpsId
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.support.{DeclarationJourneyControllerSpec, WireMockSupport}
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersNewHandlerSpec extends DeclarationJourneyControllerSpec with MibConfiguration with WireMockSupport with MockFactory {

  private lazy val httpClient = injector.instanceOf[HttpClient]
  private lazy val mockTpsPaymentsService = mock[TpsPaymentsService]
  private lazy val importView = injector.instanceOf[CheckYourAnswersImportView]
  private lazy val exportView = injector.instanceOf[CheckYourAnswersExportView]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad().url)

  private lazy val testMibConnector = new MibConnector(httpClient, "") {
    override def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
      Future.successful(DeclarationId("abc"))
  }

  private lazy val stubbedCalculation: CalculationResults => CalculationService = aPaymentCalculations =>
    new CalculationService(mibConnector) {
      override def paymentCalculations(importGoods: Seq[ImportGoods])(implicit hc: HeaderCarrier): Future[CalculationResults] =
        Future.successful(aPaymentCalculations)
  }

  private def newHandler(paymentCalcs: CalculationResults = aCalculationResults) =
    new CheckYourAnswersNewHandler(
      stubbedCalculation(paymentCalcs),
      mockTpsPaymentsService,
      testMibConnector,
      importView,
      exportView,
    )

  declarationTypes.foreach { importOrExport: DeclarationType =>
    "onPageLoad" should {
      s"return Ok with correct page content for $importOrExport" in {
        val sessionId = SessionId()
        val id = DeclarationId("xxx")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id)

        givenADeclarationJourneyIsPersistedWithStub(journey)

        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

        val eventualResult = newHandler().onPageLoad(declaration)

        status(eventualResult) mustBe OK
        contentAsString(eventualResult) must include(messageApi("checkYourAnswers.title"))
      }

      s"return 303 for goods over threshold for $importOrExport" in {
        val sessionId = SessionId()
        val id = DeclarationId("xxx")
        val created = LocalDateTime.now.withSecond(0).withNano(0)
        val journey: DeclarationJourney = completedDeclarationJourney
          .copy(sessionId = sessionId, declarationType = importOrExport, createdAt = created, declarationId = id)

        givenADeclarationJourneyIsPersistedWithStub(journey)

        val overThresholdGoods = aCalculationResults
          .modify(_.calculationResults.each)
          .setTo(aCalculationResult.modify(_.gbpAmount).setTo(AmountInPence(150000001)))

        implicit val request: Request[_] = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

        val eventualResult = newHandler(overThresholdGoods).onPageLoad(declaration)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.GoodsOverThresholdController.onPageLoad().url)
      }
    }
  }

  "on submit" should {
    "will calculate tax and send payment request to pay api for Imports" in {
      val sessionId = SessionId()
      val id = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersistedWithStub(importJourney)

      (mockTpsPaymentsService
        .createTpsPayments(_: String, _: Declaration, _: CalculationResults)(_: HeaderCarrier))
        .expects("123", *, *, *)
        .returning(Future.successful(TpsId("someid")))
        .once()
      val eventualResult = newHandler().onSubmit(declaration, "123")

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://localhost:9124/tps-payments/make-payment/mib/someid")
    }

    "will redirect to confirmation if totalTax is £0 and should not call pay api" in {
      val sessionId = SessionId()
      val id = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, createdAt = created, declarationId = id)

      givenADeclarationJourneyIsPersistedWithStub(importJourney)

      val eventualResult = newHandler(aCalculationResultsWithNoTax).onSubmit(declaration, "123")

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
    }

    "will redirect to declaration-confirmation for Export" in {
      val sessionId = SessionId()
      val stubbedId = DeclarationId("xxx")
      val created = LocalDateTime.now.withSecond(0).withNano(0)
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(sessionId = sessionId, declarationType = Export, createdAt = created, declarationId = stubbedId)

      givenADeclarationJourneyIsPersistedWithStub(exportJourney)

      val eventualResult = newHandler().onSubmit(declaration.copy(declarationType = Export), "345")

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
    }
  }
}