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
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, SessionId, YesNo}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.EnterEmailView

import scala.concurrent.ExecutionContext.Implicits.global

class EnterEmailControllerSpec extends DeclarationJourneyControllerSpec {

  val view = app.injector.instanceOf[EnterEmailView]
  val controller: DeclarationJourney => EnterEmailController =
    declarationJourney => new EnterEmailController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view)

  forAll(declarationTypes) { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(SessionId("123"), declarationType = importOrExport, maybeIsACustomsAgent = Some(YesNo.No))
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()

        val request = buildGet(routes.EnterEmailController.onPageLoad.url)
        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi("enterEmail.title"))
        result must include(messageApi("enterEmail.heading"))
        result must include(messageApi("enterEmail.hint"))
        result must include(messageApi("enterEmail.email"))
        result must include(messageApi("enterEmail.confirmation"))
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val request = buildGet(routes.EnterEmailController.onSubmit().url)
          .withFormUrlEncodedBody("email" -> "s@s.s", "confirmation" -> "s@s.s")

        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.JourneyDetailsController.onPageLoad().url)
      }

      s"return 400 with any form errors for $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val request = buildGet(routes.EnterEmailController.onSubmit().url)
          .withFormUrlEncodedBody("email1" -> "s@s.s", "confirmation" -> "")

        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 400
        result must include(messageApi("enterEmail.title"))
        result must include(messageApi("enterEmail.heading"))
        result must include(messageApi("enterEmail.hint"))
        result must include(messageApi("enterEmail.email"))
        result must include(messageApi("enterEmail.confirmation"))
      }
    }
  }
}
