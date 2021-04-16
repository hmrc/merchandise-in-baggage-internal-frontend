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
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.support._

import scala.concurrent.ExecutionContext.Implicits.global

class ResetServiceControllerSpec extends DeclarationJourneyControllerSpec {

  val controller = new ResetServiceController(controllerComponents, actionProvider, repo)

  forAll(declarationTypesTable) { importOrExport =>
    "onPageLoad" should {
      s"return 200 with expected content for $importOrExport" in {
        repo.insert(startedImportToGreatBritainJourney.copy(declarationType = importOrExport)).futureValue

        val request = buildGet(routes.ResetServiceController.onPageLoad().url, aSessionId)

        repo.findBySessionId(aSessionId).futureValue.get.maybeGoodsDestination mustBe Some(GreatBritain)

        val eventualResult = controller.onPageLoad(request)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.ImportExportChoiceController.onPageLoad().url)

        repo.findBySessionId(aSessionId).futureValue.get.maybeGoodsDestination mustBe None
      }
    }
  }

  override def beforeEach(): Unit = {
    repo.deleteAll().futureValue
    super.beforeEach()
  }
}
