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
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.CannotUseServiceView

class CannotUseServiceControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = injector.instanceOf[CannotUseServiceView]
  val controller: DeclarationJourney => CannotUseServiceController =
    declarationJourney => new CannotUseServiceController(controllerComponents, stubProvider(declarationJourney), view)

  forAll(declarationTypesTable) { importOrExport =>
    "onPageLoad" should {
      s"return 200 for $importOrExport with expected content" in {
        val journey =
          startedImportJourney.copy(declarationType = importOrExport, maybeGoodsDestination = Some(GoodsDestinations.GreatBritain))
        val request = buildGet(routes.CannotUseServiceController.onPageLoad().url)

        val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"cannotUseService.$importOrExport.title"))
        result must include(messageApi(s"cannotUseService.$importOrExport.heading"))
        result must include(messageApi(s"cannotUseService.$importOrExport.p1"))
        result must include(messageApi(s"cannotUseService.$importOrExport.p2"))
        result must include(messageApi(s"cannotUseService.$importOrExport.link.text"))
        result must include(messageApi(s"cannotUseService.$importOrExport.link.text"))
      }
    }
  }
}
