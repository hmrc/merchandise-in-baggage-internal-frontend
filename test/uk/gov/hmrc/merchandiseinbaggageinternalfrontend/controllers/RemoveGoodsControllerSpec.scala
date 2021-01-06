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

import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.RemoveGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class RemoveGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[RemoveGoodsView]
  val controller: DeclarationJourney => RemoveGoodsController =
    declarationJourney => new RemoveGoodsController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  private val journey: DeclarationJourney = DeclarationJourney(
    SessionId("123"),
    DeclarationType.Import,
    goodsEntries = GoodsEntries(Seq(completedGoodsEntry))
  )

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.RemoveGoodsController.onPageLoad(1).url)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages("removeGoods.title", "wine"))
      result must include(messages("removeGoods.heading", "wine"))
    }
  }

  "onSubmit" should {
    "redirect to /goods-removed after successful form submit with Yes and there was only one item" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.RemoveGoodsController.onSubmit(1).url)
        .withFormUrlEncodedBody("value" -> "Yes")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(1)(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.GoodsRemovedController.onPageLoad().url)
    }

    "redirect to /review-goods after successful form submit with Yes and there was more than one item" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val declarationJourney = DeclarationJourney(
        SessionId("123"),
        DeclarationType.Import,
        goodsEntries = GoodsEntries(Seq(completedGoodsEntry, completedGoodsEntry))
      )

      val request = buildGet(routes.RemoveGoodsController.onSubmit(1).url)
        .withFormUrlEncodedBody("value" -> "Yes")
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(declarationJourney)).onSubmit(1)(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.ReviewGoodsController.onPageLoad().url)
    }

    "redirect to next page after successful form submit with No" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.RemoveGoodsController.onSubmit(1).url)
        .withFormUrlEncodedBody("value" -> "No")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(1)(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.ReviewGoodsController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(journey)

      val request = buildGet(routes.RemoveGoodsController.onSubmit(1).url)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages("removeGoods.title", "wine"))
      result must include(messages("removeGoods.heading", "wine"))
    }
  }
}
