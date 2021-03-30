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

import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, Paid, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenPersistedDeclarationIsFound
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support.{DeclarationJourneyControllerSpec, WireMockSupport}
import uk.gov.hmrc.merchandiseinbaggage.views.html.PreviousDeclarationDetailsView

import scala.concurrent.ExecutionContext.Implicits.global

class PreviousDeclarationDetailsControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport with MibConfiguration {

  lazy val controllerComponents: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val actionBuilder: DeclarationJourneyActionProvider = injector.instanceOf[DeclarationJourneyActionProvider]

  "creating a page" should {
    "return 200 if declaration exists" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val mibConnector = injector.instanceOf[MibConnector]

      val controller: DeclarationJourney => PreviousDeclarationDetailsController =
        declarationJourney =>
          new PreviousDeclarationDetailsController(
            controllerComponents,
            stubProvider(declarationJourney),
            stubRepo(declarationJourney),
            mibConnector,
            view)

      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId)

      givenTheUserIsAuthenticatedAndAuthorised()

      givenADeclarationJourneyIsPersistedWithStub(importJourney)

      givenPersistedDeclarationIsFound(importJourney.declarationIfRequiredAndComplete.get, aDeclarationId)

      val request = buildGet(routes.PreviousDeclarationDetailsController.onPageLoad().url, aSessionId)
      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(importJourney)).onPageLoad()(request)
      status(eventualResult) mustBe 200

      contentAsString(eventualResult) must include("cheese")
    }

    "return 303 if declaration does NOT exist" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val mibConnector = injector.instanceOf[MibConnector]
      val controller =
        new PreviousDeclarationDetailsController(controllerComponents, actionBuilder, declarationJourneyRepository, mibConnector, view)

      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId)

      givenTheUserIsAuthenticatedAndAuthorised()

      givenADeclarationJourneyIsPersistedWithStub(importJourney)

      givenPersistedDeclarationIsFound(importJourney.declarationIfRequiredAndComplete.get, aDeclarationId)

      val request =
        buildGet(routes.PreviousDeclarationDetailsController.onPageLoad().url, SessionId()).withSession("declarationId" -> "987")
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 303

      contentAsString(eventualResult) mustNot include("cheese")
    }

    "return 200 if import declaration with an amendment exists" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val mibConnector = injector.instanceOf[MibConnector]

      val controller: DeclarationJourney => PreviousDeclarationDetailsController =
        declarationJourney =>
          new PreviousDeclarationDetailsController(
            controllerComponents,
            stubProvider(declarationJourney),
            stubRepo(declarationJourney),
            mibConnector,
            view)

      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId)

      givenTheUserIsAuthenticatedAndAuthorised()

      givenADeclarationJourneyIsPersistedWithStub(importJourney)

      val persistedDeclaration = importJourney.declarationIfRequiredAndComplete.map { declaration =>
        declaration
          .copy(maybeTotalCalculationResult = Some(aTotalCalculationResult), paymentStatus = Some(Paid), amendments = Seq(aAmendmentPaid))
      }

      givenPersistedDeclarationIsFound(persistedDeclaration.get, aDeclarationId)

      val request = buildGet(routes.PreviousDeclarationDetailsController.onPageLoad().url, aSessionId)
      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(importJourney)).onPageLoad()(request)
      status(eventualResult) mustBe 200

      contentAsString(eventualResult) must include("cheese")
      contentAsString(eventualResult) must include("more cheese")
      contentAsString(eventualResult) must include("Payment made")

    }

    "return 200 if import declaration with amendments exists" in {
      val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
      val mibConnector = injector.instanceOf[MibConnector]

      val controller: DeclarationJourney => PreviousDeclarationDetailsController =
        declarationJourney =>
          new PreviousDeclarationDetailsController(
            controllerComponents,
            stubProvider(declarationJourney),
            stubRepo(declarationJourney),
            mibConnector,
            view)

      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId)

      givenTheUserIsAuthenticatedAndAuthorised()

      givenADeclarationJourneyIsPersistedWithStub(importJourney)

      val persistedDeclaration = importJourney.declarationIfRequiredAndComplete.map { declaration =>
        declaration
          .copy(
            maybeTotalCalculationResult = Some(aTotalCalculationResult),
            paymentStatus = Some(Paid),
            amendments = Seq(aAmendmentPaid, aAmendmentPaid))
      }

      givenPersistedDeclarationIsFound(persistedDeclaration.get, aDeclarationId)

      val request = buildGet(routes.PreviousDeclarationDetailsController.onPageLoad().url, aSessionId)
      val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(importJourney)).onPageLoad()(request)
      status(eventualResult) mustBe 200

      contentAsString(eventualResult) must include("cheese")
      contentAsString(eventualResult).split("more cheese").length must equal(3)
      contentAsString(eventualResult) must include("Payment made")
    }
  }
}
