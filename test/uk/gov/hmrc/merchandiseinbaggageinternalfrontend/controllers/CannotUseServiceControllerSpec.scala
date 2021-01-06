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

import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, DeclarationType, GoodsDestinations, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.CannotUseServiceView

class CannotUseServiceControllerSpec extends DeclarationJourneyControllerSpec {

  val view = injector.instanceOf[CannotUseServiceView]
  val controller: DeclarationJourney => CannotUseServiceController =
    declarationJourney => new CannotUseServiceController(component, stubProvider(declarationJourney), view)

  "onPageLoad" should {
    "return 200 with expected content" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val journey =
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeGoodsDestination = Some(GoodsDestinations.GreatBritain))
      val request = buildGet(routes.CannotUseServiceController.onPageLoad.url)

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messageApi("cannotUseService.Import.title"))
      result must include(messageApi("cannotUseService.Import.heading"))
      result must include(messageApi("cannotUseService.Import.p1"))
      result must include(messageApi("cannotUseService.Import.p2"))
      result must include(messageApi("cannotUseService.Import.link.text"))
    }
  }
}
