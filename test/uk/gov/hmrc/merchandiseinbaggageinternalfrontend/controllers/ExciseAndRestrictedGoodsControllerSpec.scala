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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, DeclarationType, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ExciseAndRestrictedGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class ExciseAndRestrictedGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[ExciseAndRestrictedGoodsView]
  val controller: DeclarationJourney => ExciseAndRestrictedGoodsController =
    declarationJourney =>
      new ExciseAndRestrictedGoodsController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  private val journey: DeclarationJourney = DeclarationJourney(SessionId("123"), DeclarationType.Import)

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.ExciseAndRestrictedGoodsController.onPageLoad.url)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messageApi("exciseAndRestrictedGoods.Import.title"))
      result must include(messageApi("exciseAndRestrictedGoods.Import.heading"))
      result must include(messageApi("exciseAndRestrictedGoods.li1"))
      result must include(messageApi("exciseAndRestrictedGoods.li2"))
      result must include(messageApi("exciseAndRestrictedGoods.details"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit with No" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ExciseAndRestrictedGoodsController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "No")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.ValueWeightOfGoodsController.onPageLoad().url)
    }

    "redirect to next page after successful form submit with Yes" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ExciseAndRestrictedGoodsController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "Yes")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.CannotUseServiceController.onPageLoad().url)
    }

    "return 400 with any form errors" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ExciseAndRestrictedGoodsController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("exciseAndRestrictedGoods.Import.title"))
      result must include(messageApi("exciseAndRestrictedGoods.Import.heading"))
    }
  }
}
