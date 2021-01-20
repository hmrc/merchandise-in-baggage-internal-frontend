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
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.ExciseAndRestrictedGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class ExciseAndRestrictedGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[ExciseAndRestrictedGoodsView]
  val controller: DeclarationJourney => ExciseAndRestrictedGoodsController =
    declarationJourney =>
      new ExciseAndRestrictedGoodsController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  forAll(declarationTypes) { importOrExport =>
    val journey: DeclarationJourney = DeclarationJourney(SessionId("123"), importOrExport)
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.ExciseAndRestrictedGoodsController.onPageLoad.url)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"exciseAndRestrictedGoods.$importOrExport.title"))
        result must include(messageApi(s"exciseAndRestrictedGoods.$importOrExport.heading"))
        result must include(messageApi("exciseAndRestrictedGoods.li1"))
        result must include(messageApi("exciseAndRestrictedGoods.li2"))
        result must include(messageApi("exciseAndRestrictedGoods.details"))
      }
    }

    "onSubmit" should {
      forAll(exciseAndRestrictedGoodsYesOrNoAnswer) { (yesOrNo, redirectTo) =>
        s"redirect to $redirectTo after successful form submit with $yesOrNo for $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()
          val request = buildGet(routes.ExciseAndRestrictedGoodsController.onSubmit().url)
            .withFormUrlEncodedBody("value" -> yesOrNo.toString)

          val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
          status(eventualResult) mustBe 303
          redirectLocation(eventualResult).get must endWith(redirectTo)
        }
      }

      s"return 400 with any form errors for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val request = buildGet(routes.ExciseAndRestrictedGoodsController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "in valid")

        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 400
        result must include(messageApi("error.summary.title"))
        result must include(messageApi(s"exciseAndRestrictedGoods.$importOrExport.title"))
        result must include(messageApi(s"exciseAndRestrictedGoods.$importOrExport.heading"))
      }
    }
  }
}
