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
import uk.gov.hmrc.merchandiseinbaggage.support.DeclarationJourneyControllerSpec
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.views.html.DeclarationNotFoundView

class DeclarationNotFoundControllerSpec extends DeclarationJourneyControllerSpec {

  val view = injector.instanceOf[DeclarationNotFoundView]
  val controller = new DeclarationNotFoundController(component, stubProvider(startedImportJourney), view)

  "return 200 on pageLoad" in {
    val request = buildGet(routes.DeclarationNotFoundController.onPageLoad().url, aSessionId)

    givenTheUserIsAuthenticatedAndAuthorised()
    val eventualResult = controller.onPageLoad(request)

    status(eventualResult) mustBe 200
    val result = contentAsString(eventualResult)

    result must include(messages("declarationNotFound.heading"))
    result must include(messages("declarationNotFound.title"))
  }
}
