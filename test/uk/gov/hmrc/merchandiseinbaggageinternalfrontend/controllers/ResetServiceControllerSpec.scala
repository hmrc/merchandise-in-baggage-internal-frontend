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

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._

import scala.concurrent.ExecutionContext.Implicits.global

class ResetServiceControllerSpec extends DeclarationJourneyControllerSpec {

  val controller = new ResetServiceController(component, actionProvider, repo)

  "onPageLoad" should {
    "return 200 with expected content" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      repo.insert(startedImportToGreatBritainJourney)

      val request = buildGet(routes.ResetServiceController.onPageLoad().url, sessionId)

      repo.findBySessionId(sessionId).futureValue.get.maybeGoodsDestination mustBe Some(GreatBritain)

      val eventualResult = controller.onPageLoad(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.ImportExportChoiceController.onPageLoad().url)

      repo.findBySessionId(sessionId).futureValue.get.maybeGoodsDestination mustBe None
    }
  }

  override def beforeEach(): Unit = repo.deleteAll().futureValue
}
