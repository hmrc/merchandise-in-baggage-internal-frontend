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
import play.mvc.Http.Status
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.RemoveGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class RemoveGoodsControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[RemoveGoodsView]

  forAll(declarationTypes) { importOrExport =>
    val controller: DeclarationJourney => RemoveGoodsController =
      declarationJourney => new RemoveGoodsController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)
    val journey: DeclarationJourney =
      DeclarationJourney(SessionId("123"), importOrExport, goodsEntries = GoodsEntries(Seq(completedGoodsEntry)))
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.RemoveGoodsController.onPageLoad(1).url)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(1)(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messages("removeGoods.title", "wine"))
        result must include(messages("removeGoods.heading", "wine"))
      }
    }

    forAll(removeGoodsAnswer) { (yesNo, redirectTo) =>
      "onSubmit" should {
        s"redirect to $redirectTo after successful form submit with $yesNo and there was only one item for $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()

          val request = buildGet(routes.RemoveGoodsController.onSubmit(1).url)
            .withFormUrlEncodedBody("value" -> yesNo.toString)

          val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(1)(request)

          status(eventualResult) mustBe 303
          redirectLocation(eventualResult).get must endWith(redirectTo)
        }
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
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
  "on submit if answer No" should {
    val controller = new RemoveGoodsController(component, actionProvider, repo, view)
    s"redirect x back to ${routes.CheckYourAnswersController.onPageLoad().url} if journey was completed" in {
      val result = controller.removeGoodOrRedirect(1, completedDeclarationJourney, No)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

    s"redirect back to ${routes.ReviewGoodsController.onPageLoad().url} if journey was NOT completed" in {
      val result = controller.removeGoodOrRedirect(1, startedImportJourney, No)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.ReviewGoodsController.onPageLoad().url)
    }
  }

  "on submit if answer yes" should {
    val controller = new RemoveGoodsController(component, actionProvider, repo, view)
    s"redirect to ${routes.GoodsRemovedController.onPageLoad()} if goods contains an entry" in {
      val result = controller.removeGoodOrRedirect(1, importJourneyWithStartedGoodsEntry, Yes)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.GoodsRemovedController.onPageLoad().url)
    }

    s"redirect to ${routes.ReviewGoodsController.onPageLoad()} if goods contains more entries" in {
      val journey = importJourneyWithStartedGoodsEntry.copy(goodsEntries = GoodsEntries(Seq(startedGoodsEntry, startedGoodsEntry)))
      val result = controller.removeGoodOrRedirect(1, journey, Yes)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.ReviewGoodsController.onPageLoad().url)
    }

    s"redirect to ${routes.CheckYourAnswersController.onPageLoad()} if goods contains more entries and is completed" in {
      val journey = completedDeclarationJourney.copy(goodsEntries = GoodsEntries(Seq(completedGoodsEntry, completedGoodsEntry)))
      val result = controller.removeGoodOrRedirect(1, journey, Yes)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }
  }
}
