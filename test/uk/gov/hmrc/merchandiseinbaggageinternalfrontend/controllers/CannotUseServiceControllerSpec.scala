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

import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, DeclarationType, GoodsDestinations, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.CannotUseServiceView

class CannotUseServiceControllerSpec extends BaseSpecWithApplication {

  val view = injector.instanceOf[CannotUseServiceView]
  val controller = new CannotUseServiceController(component, actionProvider, view)

  "onPageLoad" should {
    "return 200 with expected content" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeGoodsDestination = Some(GoodsDestinations.GreatBritain)))

      val request = buildGet(routes.CannotUseServiceController.onPageLoad.url)

      val eventualResult = controller.onPageLoad(request)
      status(eventualResult) mustBe 200
      contentAsString(eventualResult) must include(messageApi("cannotUseService.Import.title"))
      contentAsString(eventualResult) must include(messageApi("cannotUseService.Import.heading"))
      contentAsString(eventualResult) must include(messageApi("cannotUseService.Import.p1"))
      contentAsString(eventualResult) must include(messageApi("cannotUseService.Import.p2"))
      contentAsString(eventualResult) must include(messageApi("cannotUseService.Import.link.text"))
    }
  }
}
