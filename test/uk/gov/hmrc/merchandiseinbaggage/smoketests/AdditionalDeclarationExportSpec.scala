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

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CategoryQuantityOfGoods, Paid}
import uk.gov.hmrc.merchandiseinbaggage.model.core.RetrieveDeclaration
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.{CheckYourAnswersPage, ExciseAndRestrictedGoodsPage, GoodsTypeQuantityPage, ImportExportChoicePage, PreviousDeclarationDetailsPage, PurchaseDetailsExportPage, RetrieveDeclarationPage, ReviewGoodsPage, SearchGoodsCountryPage, ValueWeightOfGoodsPage}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised

class AdditionalDeclarationExportSpec extends BaseUiSpec {

  "Additional Declaration Export journey - happy path" should {
    "work as expected" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      goto(ImportExportChoicePage.path)

      submitPage(ImportExportChoicePage, "AddToExisting")

      val paidDeclaration = declaration.copy(
        declarationType = Export,
        paymentStatus = Some(Paid),
        maybeTotalCalculationResult = Some(aTotalCalculationResult),
        eori = eori,
        mibReference = mibReference)

      givenFindByDeclarationReturnSuccess(mibReference, eori, paidDeclaration)

      givenPersistedDeclarationIsFound(paidDeclaration, paidDeclaration.declarationId)

      submitPage(RetrieveDeclarationPage, RetrieveDeclaration(mibReference, eori))

      webDriver.getPageSource must include("wine")
      webDriver.getPageSource must include("99.99, Euro (EUR)")

      submitPage(PreviousDeclarationDetailsPage, "continue")

      // controlled or restricted goods
      submitPage(ExciseAndRestrictedGoodsPage, No)

      submitPage(ValueWeightOfGoodsPage, Yes)

      submitPage(GoodsTypeQuantityPage, CategoryQuantityOfGoods("sock", "one"))

      submitPage(SearchGoodsCountryPage, "FR")

      submitPage(PurchaseDetailsExportPage, "100.50")

      // Review the goods added
      webDriver.getPageSource must include("sock")
      webDriver.getPageSource must include("France")
      webDriver.getPageSource must include("£100.50")

      submitPage(ReviewGoodsPage, "No")

      webDriver.getPageSource must include("sock")
      webDriver.getPageSource must include("France")

      webDriver.getPageSource must include("makeDeclarationButton")
      webDriver.getCurrentUrl mustBe fullUrl(CheckYourAnswersPage.path)

    }
  }
}
