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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ReviewGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class ReviewGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[ReviewGoodsView]
  val controller: DeclarationJourney => ReviewGoodsController =
    declarationJourney => new ReviewGoodsController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  forAll(declarationTypes) { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(SessionId("123"), importOrExport, goodsEntries = GoodsEntries(Seq(completedGoodsEntry)))
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.ReviewGoodsController.onPageLoad.url)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi("reviewGoods.title"))
        result must include(messageApi("reviewGoods.heading"))
        result must include(messageApi("reviewGoods.list.item"))
        result must include(messageApi("reviewGoods.list.quantity"))
        if (importOrExport == Import) {
          result must include(messageApi("reviewGoods.list.vatRate"))
          result must include(messageApi("reviewGoods.list.country"))
        }
        if (importOrExport == Export) { result must include(messageApi("reviewGoods.list.destination")) }
        result must include(messageApi("reviewGoods.list.price"))
        result must include(messageApi("site.change"))
        result must include(messageApi("site.remove"))
        result must include(messageApi("reviewGoods.h3"))
      }
    }

    forAll(reviewGoodsAnswer) { (yesOrNo, redirectTo) =>
      "onSubmit" should {
        s"redirect to next page after successful form submit with $yesOrNo for $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()

          val request = buildGet(routes.ReviewGoodsController.onSubmit().url)
            .withFormUrlEncodedBody("value" -> yesOrNo.toString)

          val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)

          status(eventualResult) mustBe 303
          redirectLocation(eventualResult).get must include(s"$redirectTo")
        }
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.ReviewGoodsController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("reviewGoods.title"))
      result must include(messageApi("reviewGoods.heading"))
    }
  }
}
