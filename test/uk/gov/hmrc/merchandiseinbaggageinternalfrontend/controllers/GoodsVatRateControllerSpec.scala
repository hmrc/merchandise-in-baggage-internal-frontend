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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.GoodsVatRateView

import scala.concurrent.ExecutionContext.Implicits.global

class GoodsVatRateControllerSpec extends BaseSpecWithApplication {

  val view = app.injector.instanceOf[GoodsVatRateView]
  val controller = new GoodsVatRateController(component, actionProvider, repo, view)

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))

      val request = FakeRequest(GET, routes.GoodsVatRateController.onPageLoad(1).url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

      val eventualResult = controller.onPageLoad(1)(request)
      status(eventualResult) mustBe 200
      contentAsString(eventualResult) must include(messageApi("goodsVatRate.title"))
      contentAsString(eventualResult) must include(messageApi("goodsVatRate.heading"))
      contentAsString(eventualResult) must include(messageApi("goodsVatRate.p"))
      contentAsString(eventualResult) must include(messageApi("goodsVatRate.Zero"))
      contentAsString(eventualResult) must include(messageApi("goodsVatRate.Five"))
      contentAsString(eventualResult) must include(messageApi("goodsVatRate.Twenty"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))
      val request = FakeRequest(GET, routes.GoodsVatRateController.onSubmit(1).url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody("value" -> "Zero")

      val eventualResult = controller.onSubmit(1)(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.SearchGoodsCountryController.onPageLoad(1).url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(DeclarationJourney(SessionId("123"), DeclarationType.Import))
      val request = FakeRequest(GET, routes.GoodsVatRateController.onSubmit(1).url)
        .withSession((SessionKeys.sessionId, "123"))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller.onSubmit(1)(request)
      status(eventualResult) mustBe 400

      contentAsString(eventualResult) must include(messageApi("goodsVatRate.title"))
      contentAsString(eventualResult) must include(messageApi("goodsVatRate.heading"))
    }
  }
}
