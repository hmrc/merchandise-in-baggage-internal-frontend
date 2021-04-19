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

package uk.gov.hmrc.merchandiseinbaggage.smoketests

import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CategoryQuantityOfGoods, Paid}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{PurchaseDetailsInput, RetrieveDeclaration}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._

class AdditionalDeclarationImportSpec extends BaseUiSpec {

  "Additional Declaration Import journey - happy path" should {
    "work as expected" in {
      goto(ImportExportChoicePage.path)

      submitPage(ImportExportChoicePage, "AddToExisting")

      val paidDeclaration = declaration.copy(
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(aTotalCalculationResult),
        eori = eori,
        mibReference = mibReference)

      givenFindByDeclarationReturnSuccess(mibReference, eori, paidDeclaration)

      givenPersistedDeclarationIsFound(paidDeclaration, paidDeclaration.declarationId)

      givenAPaymentCalculation(aCalculationResult)
      givenEoriIsChecked(eori.toString)

      submitPage(RetrieveDeclarationPage, RetrieveDeclaration(mibReference, eori))

      webDriver.getCurrentUrl mustBe fullUrl(PreviousDeclarationDetailsPage.path)

      webDriver.getPageSource must include("wine")
      webDriver.getPageSource must include("99.99, Euro (EUR)")

      submitPage(PreviousDeclarationDetailsPage, "continue")

      submitPage(ExciseAndRestrictedGoodsPage, No)

      submitPage(ValueWeightOfGoodsPage, Yes)

      submitPage(GoodsTypeQuantityPage, CategoryQuantityOfGoods("sock", "one"))

      submitPage(GoodsVatRatePage, "Five")

      submitPage(GoodsOriginPage, "Yes")
      submitPage(PurchaseDetailsPage, PurchaseDetailsInput("100.50", "EUR"))

      webDriver.getPageSource must include("sock")
      webDriver.getPageSource must include("Yes")
      webDriver.getPageSource must include("100.50, Euro (EUR)")

      submitPage(ReviewGoodsPage, "No")

      submitPage(PaymentCalculationPage, "")

      webDriver.getPageSource must include("payButton")
      webDriver.getCurrentUrl mustBe fullUrl(CheckYourAnswersPage.path)
    }
  }
}
