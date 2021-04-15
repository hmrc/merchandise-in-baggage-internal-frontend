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

import java.time.LocalDateTime
import play.api.mvc.DefaultActionBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, _}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support.DeclarationJourneyControllerSpec
import uk.gov.hmrc.merchandiseinbaggage.views.html.DeclarationConfirmationView
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationConfirmationControllerSpec extends DeclarationJourneyControllerSpec with WireMockSupport with MibConfiguration {

  lazy val actionBuilder: DeclarationJourneyActionProvider = injector.instanceOf[DeclarationJourneyActionProvider]

  import mibConf._
  private val view = app.injector.instanceOf[DeclarationConfirmationView]
  private val client = app.injector.instanceOf[HttpClient]
  private val connector = new MibConnector(client, s"$protocol://$host:${WireMockSupport.port}")

  "on page load return 200 if declaration exists and resets the journey" in {
    val sessionId = aSessionId //SessionId()
    val id = aDeclarationId //DeclarationId("456")
    val created = LocalDateTime.now.withSecond(0).withNano(0)
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, sessionId)

    val exportJourney: DeclarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId, declarationType = DeclarationType.Export, createdAt = created, declarationId = id)

    givenTheUserIsAuthenticatedAndAuthorised()

    val repo = stubRepo(givenADeclarationJourneyIsPersistedWithStub(exportJourney))

    lazy val defaultBuilder = injector.instanceOf[DefaultActionBuilder]
    lazy val stride = injector.instanceOf[StrideAuthAction]
    val ab = new DeclarationJourneyActionProvider(defaultBuilder, repo, stride)

    val controller =
      new DeclarationConfirmationController(controllerComponents, ab, view, connector, repo)

    givenPersistedDeclarationIsFound(exportJourney.declarationIfRequiredAndComplete.get, id)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 200

  }

  "on page load return an invalid request if journey is invalidated by resetting" in {
    givenTheUserIsAuthenticatedAndAuthorised()

    val connector = new MibConnector(client, "") {
      override def findDeclaration(declarationId: DeclarationId)(implicit hc: HeaderCarrier): Future[Option[Declaration]] =
        Future.failed(new Exception("not found"))
    }

    val controller =
      new DeclarationConfirmationController(controllerComponents, actionBuilder, view, connector, declarationJourneyRepository)
    val request = buildGet(routes.DeclarationConfirmationController.onPageLoad().url, aSessionId)

    val eventualResult = controller.onPageLoad()(request)
    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("/declare-commercial-goods/cannot-access-page")
  }

  "Import with value over 1000gbp, add an 'take proof' line" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Import, 111111)

    result must include(messageApi("declarationConfirmation.ul.3"))
  }

  "Import with value under 1000gbp, DONOT add an 'take proof' line" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Import, 100)

    result mustNot include(messageApi("declarationConfirmation.ul.3"))
  }

  "Export with value over 1000gbp, DONOT add an 'take proof' line" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Export, 111111)

    result mustNot include(messageApi("declarationConfirmation.ul.3"))
  }

  "New declaration displays 'take this declaration with you'" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Import, 111111, New)

    result must include(messageApi("declarationConfirmation.ul.2.New"))
  }

  "Amended declaration displays 'take this UPDATED declaration with you'" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Import, 111111, Amend)

    result must include(messageApi("declarationConfirmation.ul.2.Amend"))
  }

  "Import should display the destination country" in {
    val result = generateDeclarationConfirmationPage(DeclarationType.Export, 100)

    result must include("Destination")
    result must include("France")
  }

}
