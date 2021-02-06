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
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries}
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsRemovedView

class GoodsRemovedControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = app.injector.instanceOf[GoodsRemovedView]
  val controller: DeclarationJourney => GoodsRemovedController =
    declarationJourney => new GoodsRemovedController(component, stubProvider(declarationJourney), view)

  forAll(declarationTypes) { importOrExport =>
    "onPageLoad" should {
      s"return 200 for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val journey = DeclarationJourney(SessionId("123"), importOrExport).copy(goodsEntries = GoodsEntries(Seq(completedImportGoods)))

        val request = buildGet(routes.GoodsRemovedController.onPageLoad().url)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad()(request)

        status(eventualResult) mustBe 200
        contentAsString(eventualResult) must include(messages("goodsRemoved.title"))
        contentAsString(eventualResult) must include(messages("goodsRemoved.heading"))
        contentAsString(eventualResult) must include(messages("goodsRemoved.p1"))
        contentAsString(eventualResult) must include(messages("goodsRemoved.link"))
      }
    }
  }
}
