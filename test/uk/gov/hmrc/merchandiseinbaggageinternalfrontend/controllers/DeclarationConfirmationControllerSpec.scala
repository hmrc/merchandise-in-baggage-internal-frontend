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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{AmountInPence, DeclarationJourney, TotalCalculationResult}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.DeclarationConfirmationView

import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationConfirmationControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[DeclarationConfirmationView]
  val controller: DeclarationJourney => DeclarationConfirmationController =
    declarationJourney =>
      new DeclarationConfirmationController(component, stubProvider(declarationJourney), view, mibConnector, stubRepo(declarationJourney))

  forAll(declarationTypes) { importOrExport =>
    "onPageLoad" should {
      s"return 200 for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val persistedDeclaration = declaration
          .copy(
            maybeTotalCalculationResult =
              Some(TotalCalculationResult(aPaymentCalculations, AmountInPence(10L), AmountInPence(5), AmountInPence(2), AmountInPence(3))),
            declarationType = importOrExport
          )
        MibBackendStub.givenPersistedDeclarationIsFound(persistedDeclaration, persistedDeclaration.declarationId)

        val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, sessionId)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(completedDeclarationJourney)).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messages("declarationConfirmation.title"))
        result must include(messages("declarationConfirmation.banner.title"))
        result must include(messages("declarationConfirmation.yourReferenceNumber.label"))
        result must include(messages("declarationConfirmation.h2.1"))
        result must include(messages("declarationConfirmation.ul.p"))
        result must include(messages("declarationConfirmation.ul.1"))
        result must include(messages(s"declarationConfirmation.$importOrExport.ul.3"))
        result must include(messages("declarationConfirmation.makeAnotherDeclaration"))
        result must include(messages("declarationConfirmation.date"))
        result must include(messages("declarationConfirmation.email", declaration.email.map(_.email).getOrElse("")))

        if (importOrExport == Import) {
          result must include(messages("declarationConfirmation.ul.2"))
          result must include(messages("declarationConfirmation.ul.2.strong"))
          result must include(messages("declarationConfirmation.amountPaid"))
          result must include(messages("declarationConfirmation.amountPaid.customsDuty"))
          result must include(messages("declarationConfirmation.amountPaid.vat"))
          result must include(messages("declarationConfirmation.amountPaid.totalTax"))
        }
      }
    }
  }
}
