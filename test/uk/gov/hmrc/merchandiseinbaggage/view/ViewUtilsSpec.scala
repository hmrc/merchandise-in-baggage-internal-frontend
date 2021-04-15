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

package uk.gov.hmrc.merchandiseinbaggage.view

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.merchandiseinbaggage.auth.AuthRequest
import uk.gov.hmrc.merchandiseinbaggage.controllers.{DeclarationGoodsRequest, DeclarationJourneyRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.views.ViewUtils

class ViewUtilsSpec extends BaseSpecWithApplication with CoreTestData {

  val fakeAuthRequest: AuthRequest[AnyContentAsEmpty.type] = AuthRequest(FakeRequest("", ""), None)

  "DeclarationJourney" should {
    "new import journey" in {
      val request = new DeclarationJourneyRequest(startedImportJourney, fakeAuthRequest)
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("new")
    }

    "new export journey" in {
      val request = new DeclarationJourneyRequest(startedExportJourney, fakeAuthRequest)
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("new")
    }

    "amend import journey" in {
      val request = new DeclarationJourneyRequest(startedImportJourney.copy(journeyType = Amend), fakeAuthRequest)
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("amend")
    }

    "amend export journey" in {
      val request = new DeclarationJourneyRequest(startedExportJourney.copy(journeyType = Amend), fakeAuthRequest)
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("amend")
    }

    "unknown journey" in {
      val request = fakeAuthRequest
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("")
    }
  }

  "DeclarationGoodsRequest" should {
    "new import journey" in {
      val djr = new DeclarationJourneyRequest(startedImportJourney, fakeAuthRequest)
      val request = new DeclarationGoodsRequest(djr, startedImportGoods)
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("new")
    }

    "new export journey" in {
      val djr = new DeclarationJourneyRequest(startedExportJourney, fakeAuthRequest)
      val request = new DeclarationGoodsRequest(djr, startedExportGoods)
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("new")
    }

    "amend import journey" in {
      val djr = new DeclarationJourneyRequest(startedImportJourney.copy(journeyType = Amend), fakeAuthRequest)
      val request = new DeclarationGoodsRequest(djr, startedImportGoods)
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("amend")
    }

    "amend export journey" in {
      val djr = new DeclarationJourneyRequest(startedExportJourney.copy(journeyType = Amend), fakeAuthRequest)
      val request = new DeclarationGoodsRequest(djr, startedExportGoods)
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("amend")
    }

    "unknown journey" in {
      val request = fakeAuthRequest
      val result = ViewUtils.googleAnalyticsJourneyType(request)

      result mustBe ("")
    }
  }

}
