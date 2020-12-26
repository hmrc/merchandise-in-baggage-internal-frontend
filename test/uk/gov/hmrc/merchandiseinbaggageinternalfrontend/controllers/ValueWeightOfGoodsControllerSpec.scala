/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ValueWeightOfGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class ValueWeightOfGoodsControllerSpec extends BaseSpecWithApplication {

  val view = app.injector.instanceOf[ValueWeightOfGoodsView]
  val controller = new ValueWeightOfGoodsController(component, actionProvider, repo, view)

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeGoodsDestination = Some(GreatBritain)))

      val request = FakeRequest(GET, routes.ValueWeightOfGoodsController.onPageLoad.url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

      val eventualResult = controller.onPageLoad(request)
      status(eventualResult) mustBe 200
      contentAsString(eventualResult) must include(messageApi("valueWeightOfGoods.GreatBritain.title"))
      contentAsString(eventualResult) must include(messageApi("valueWeightOfGoods.GreatBritain.heading"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit with No" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeGoodsDestination = Some(GreatBritain)))
      val request = FakeRequest(GET, routes.ValueWeightOfGoodsController.onSubmit().url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody("value" -> "No")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.GoodsTypeQuantityController.onPageLoad(1).url)
    }

    "redirect to next page after successful form submit with Yes" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeGoodsDestination = Some(GreatBritain)))
      val request = FakeRequest(GET, routes.ValueWeightOfGoodsController.onSubmit().url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody("value" -> "Yes")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.CannotUseServiceController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(
        DeclarationJourney(SessionId("123"), DeclarationType.Import, maybeGoodsDestination = Some(GreatBritain)))
      val request = FakeRequest(GET, routes.ValueWeightOfGoodsController.onSubmit().url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 400

      contentAsString(eventualResult) must include(messageApi("error.summary.title"))
      contentAsString(eventualResult) must include(messageApi("valueWeightOfGoods.GreatBritain.title"))
      contentAsString(eventualResult) must include(messageApi("valueWeightOfGoods.GreatBritain.heading"))
    }
  }
}
