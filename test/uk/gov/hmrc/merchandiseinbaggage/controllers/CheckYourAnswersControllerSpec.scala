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

import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationGoods, PaymentCalculations}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.TpsId
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.support.MibBackendStub.givenDeclarationIsPersistedInBackend
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support.TpsPaymentsBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport {

  val importView: CheckYourAnswersImportView = app.injector.instanceOf[CheckYourAnswersImportView]
  val exportView: CheckYourAnswersExportView = app.injector.instanceOf[CheckYourAnswersExportView]

  private lazy val stubbedCalculation: PaymentCalculations => CalculationService = aPaymentCalculations =>
    new CalculationService(mibConnector) {
      override def paymentBECalculation(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[PaymentCalculations] =
        Future.successful(aPaymentCalculations)
  }

  def controller(
    declarationJourney: DeclarationJourney,
    paymentCalcs: PaymentCalculations = aPaymentCalculations): CheckYourAnswersController =
    new CheckYourAnswersController(
      component,
      stubProvider(declarationJourney),
      stubbedCalculation(paymentCalcs),
      tpsPaymentsService,
      mibConnector,
      stubRepo(declarationJourney),
      importView,
      exportView
    )

  forAll(declarationTypes) { importOrExport =>
    "onPageLoad" should {
      s"return 200 for type $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url, aSessionId)

        val eventualResult = controller(
          givenADeclarationJourneyIsPersisted(completedDeclarationJourney.copy(declarationType = importOrExport))).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messages("checkYourAnswers.title"))
        result must include(messages("checkYourAnswers.change"))
        result must include(messages("checkYourAnswers.detailsOfTheGoods"))
        result must include(messages("checkYourAnswers.detailsOfTheGoods.category"))
        result must include(messages("checkYourAnswers.detailsOfTheGoods.quantity"))
        result must include(messages("checkYourAnswers.addMoreGoods"))
        result must include(messages("checkYourAnswers.personalDetails"))
        result must include(messages("checkYourAnswers.journeyDetails.travellingByVehicle"))
        result must include(messages("checkYourAnswers.journeyDetails.vehicleRegistrationNumber"))
        result must include(messages("checkYourAnswers.sendDeclaration.acknowledgement"))
        result must include(messages(s"checkYourAnswers.sendDeclaration.$importOrExport.acknowledgement.1"))
        result must include(messages("checkYourAnswers.sendDeclaration.confirm"))
        result must include(messages("checkYourAnswers.sendDeclaration.warning"))
        result must include(messages(s"checkYourAnswers.sendDeclaration.warning.$importOrExport.message"))

        if (importOrExport == Import) {
          result must include(messages("checkYourAnswers.detailsOfTheGoods.vatRate"))
          result must include(messages("checkYourAnswers.detailsOfTheGoods.country"))
          result must include(messages("checkYourAnswers.detailsOfTheGoods.price"))
          result must include(messages("checkYourAnswers.detailsOfTheGoods.paymentDue"))
          result must include(messages("checkYourAnswers.journeyDetails.placeOfArrival"))
          result must include(messages("checkYourAnswers.journeyDetails.dateOfArrival"))
          result must include(messages("checkYourAnswers.payButton"))
        }

        if (importOrExport == Export) {
          result must include(messages("checkYourAnswers.makeDeclarationButton"))
          result must include(messages("checkYourAnswers.detailsOfTheGoods.destination"))
          result must include(messages("checkYourAnswers.journeyDetails.placeOfDeparture"))
          result must include(messages("checkYourAnswers.journeyDetails.dateOfDeparture"))
        }
      }

      s"return 200 for type $importOrExport when email is None" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url, aSessionId)
        val eventualResult = controller(
          givenADeclarationJourneyIsPersisted(completedDeclarationJourney
            .copy(declarationType = importOrExport, maybeEmailAddress = None))).onPageLoad()(request)

        status(eventualResult) mustBe 200
      }
    }
  }

  "onSubmit" should {
    "redirect to payment page after successful form submit for Imports" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenDeclarationIsPersistedInBackend()
      givenTaxArePaid(TpsId("123"))
      val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, aSessionId)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(completedDeclarationJourney)).onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://localhost:9124/tps-payments/make-payment/mib/123")
    }

    "redirect to payment page after successful form submit for Exports" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenDeclarationIsPersistedInBackend()
      val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, aSessionId)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(completedDeclarationJourney.copy(declarationType = Export)))
        .onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
    }
  }
}
