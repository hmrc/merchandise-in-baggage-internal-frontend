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
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support.AddressLookupFrontendStub._
import uk.gov.hmrc.merchandiseinbaggage.support._

import scala.concurrent.ExecutionContext.Implicits.global

class EnterAgentAddressControllerSpec extends DeclarationJourneyControllerSpec {

  val controller: DeclarationJourney => EnterAgentAddressController =
    declarationJourney =>
      new EnterAgentAddressController(
        controllerComponents,
        stubProvider(declarationJourney),
        stubRepo(declarationJourney),
        addressLookupConnector)

  forAll(declarationTypesTable) { importOrExport =>
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenInitJourney()
        val journey = startedImportJourney

        val request = buildGet(routes.EnterAgentAddressController.onPageLoad().url)

        val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad()(request)
        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some("/blah")
      }

      s"returnFromAddressLookup for $importOrExport" must {
        val url = routes.EnterAgentAddressController.returnFromAddressLookup("id").url

        val address =
          Address(Seq("address line 1", "address line 2"), Some("AB12 3CD"), AddressLookupCountry("GB", Some("United Kingdom")))

        val request = buildGet(url, aSessionId)

        s"store address and redirect to ${routes.EoriNumberController.onPageLoad()} for $importOrExport" when {
          s"a declaration journey has been started for $importOrExport" in {
            givenConfirmJourney("id", address)

            val result =
              controller(givenADeclarationJourneyIsPersistedWithStub(startedImportJourney)).returnFromAddressLookup("id")(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).get mustEqual routes.EoriNumberController.onPageLoad().url
          }
        }

        s"store address and redirect to ${routes.CheckYourAnswersController.onPageLoad()} for $importOrExport" when {
          s"a declaration journey is complete for $importOrExport" in {
            givenConfirmJourney("id", address)

            val result =
              controller(givenADeclarationJourneyIsPersistedWithStub(completedDeclarationJourney)).returnFromAddressLookup("id")(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).get mustEqual routes.CheckYourAnswersController.onPageLoad().url
          }
        }
      }
    }
  }
}
