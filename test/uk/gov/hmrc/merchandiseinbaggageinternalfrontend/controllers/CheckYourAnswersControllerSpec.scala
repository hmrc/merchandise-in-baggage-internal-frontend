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

import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.tpspayments.TpsId
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.CurrencyConversionSupport.givenSuccessfulCurrencyConversionResponse
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MibBackendStub.givenDeclarationIsPersistedInBackend
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.TpsPaymentsBackendStub._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.{CheckYourAnswersExportView, CheckYourAnswersImportView}

import scala.concurrent.ExecutionContext.Implicits.global

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec {

  val importView: CheckYourAnswersImportView = app.injector.instanceOf[CheckYourAnswersImportView]
  val exportView: CheckYourAnswersExportView = app.injector.instanceOf[CheckYourAnswersExportView]

  val controller: DeclarationJourney => CheckYourAnswersController =
    declarationJourney =>
      new CheckYourAnswersController(
        component,
        stubProvider(declarationJourney),
        calculationService,
        tpsPaymentsService,
        mibConnector,
        stubRepo(declarationJourney),
        importView,
        exportView)

  forAll(declarationTypes) { importOrExport =>
    "onPageLoad" should {
      s"return 200 for type $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        givenSuccessfulCurrencyConversionResponse()

        val request = buildGet(routes.CheckYourAnswersController.onPageLoad().url, sessionId)

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
    }
  }

  "onSubmit" should {
    "redirect to payment page after successful form submit for Imports" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenSuccessfulCurrencyConversionResponse()
      givenDeclarationIsPersistedInBackend()
      givenTaxArePaid(TpsId("123"))

      val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(completedDeclarationJourney)).onSubmit()(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some("http://localhost:9124/tps-payments/make-payment/mib/123")
    }

    "redirect to payment page after successful form submit for Exports" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenSuccessfulCurrencyConversionResponse()
      givenDeclarationIsPersistedInBackend()

      val request = buildPost(routes.CheckYourAnswersController.onSubmit().url, sessionId)

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(completedDeclarationJourney.copy(declarationType = Export)))
        .onSubmit()(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.DeclarationConfirmationController.onPageLoad().url)
    }
  }
}
