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

import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.DeclarationConfirmationView

import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationConfirmationControllerSpec extends BaseSpecWithApplication {

  val view = app.injector.instanceOf[DeclarationConfirmationView]
  val controller = new DeclarationConfirmationController(component, actionProvider, view, mibConnector, repo)

  "onPageLoad" should {
    "return 200" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      givenADeclarationJourneyIsPersisted(completedDeclarationJourney)
      MibBackendStub.givenPersistedDeclarationIsFound(declaration, declaration.declarationId)

      val request = FakeRequest(GET, routes.DeclarationConfirmationController.onPageLoad().url)
        .withSession((SessionKeys.sessionId, sessionId.value))
        .withCSRFToken
        .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 200
      contentAsString(eventualResult) must include(messages("declarationConfirmation.title"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.banner.title"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.yourReferenceNumber.label"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.h2.1"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.ul.p"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.ul.1"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.ul.1.strong"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.ul.2"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.Import.ul.3"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.makeAnotherDeclaration"))
      contentAsString(eventualResult) must include(messages("declarationConfirmation.printOrSave.label"))
    }
  }
}
