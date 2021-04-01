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

package uk.gov.hmrc.merchandiseinbaggage.content

import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Eori, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core.RetrieveDeclaration
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.RetrieveDeclarationPage

class RetrieveDeclarationContentSpec extends RetrieveDeclarationPage with CoreTestData {

  "page should pre-populate the already stored inputs" in {
    val retrieveDeclaration = RetrieveDeclaration(MibReference("mib-ref"), Eori("eori"))
    givenAJourneyWithSession(declarationJourney = completedDeclarationJourney.copy(maybeRetrieveDeclaration = Some(retrieveDeclaration)))
    goToRetrieveDeclarationPage()

    findById("mibReference").getAttribute("value") mustBe "mib-ref"
    findById("eori").getAttribute("value") mustBe "eori"
  }

}
