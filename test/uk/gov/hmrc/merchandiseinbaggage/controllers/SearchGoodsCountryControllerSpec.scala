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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.SearchGoodsCountryView

import scala.concurrent.ExecutionContext.Implicits.global

class SearchGoodsCountryControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = app.injector.instanceOf[SearchGoodsCountryView]
  val controller: DeclarationJourney => SearchGoodsCountryController =
    declarationJourney => new SearchGoodsCountryController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  val journey: DeclarationJourney = DeclarationJourney(
    SessionId("123"),
    Export,
    goodsEntries = dynamicStartedGoodsEntries(Export)
  )

  "onPageLoad" should {
    s"return 200 with radio buttons for Export" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(routes.SearchGoodsCountryController.onPageLoad(1).url)
      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages(s"searchGoodsCountry.title", "test good"))
      result must include(messages(s"searchGoodsCountry.heading", "test good"))
    }
  }

  "onSubmit" should {
    s"redirect to next page after successful form submit for Export" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.SearchGoodsCountryController.onSubmit(1).url)
        .withFormUrlEncodedBody("country" -> "AF")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(1)(request)

      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.PurchaseDetailsController.onPageLoad(1).url)
    }

    s"return 400 with any form errors for Export" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.SearchGoodsCountryController.onSubmit(1).url)
        .withFormUrlEncodedBody("country" -> "in valid")

      val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages(s"searchGoodsCountry.title", "test good"))
      result must include(messages(s"searchGoodsCountry.heading", "test good"))
    }
  }
}
