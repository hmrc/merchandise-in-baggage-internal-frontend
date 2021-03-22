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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggage.support._
import uk.gov.hmrc.merchandiseinbaggage.views.html.EoriNumberView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EoriNumberControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  private val view = app.injector.instanceOf[EoriNumberView]
  private val client = app.injector.instanceOf[HttpClient]
  private val connector = new MibConnector(client, "some url") {
    override def checkEoriNumber(eori: String)(implicit hc: HeaderCarrier): Future[CheckResponse] =
      Future.successful(CheckResponse("123", valid = true, None))
  }
  val controller: DeclarationJourney => EoriNumberController =
    declarationJourney =>
      new EoriNumberController(component, stubProvider(declarationJourney), stubRepo(declarationJourney), view, connector)

  forAll(declarationTypes) { importOrExport =>
    forAll(traderYesOrNoAnswer) { (yesNo, traderOrAgent) =>
      val journey: DeclarationJourney = startedImportJourney.copy(declarationType = importOrExport, maybeIsACustomsAgent = Some(yesNo))
      "onPageLoad" should {
        s"return 200 with radio buttons for $traderOrAgent $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()

          val request = buildGet(routes.EoriNumberController.onPageLoad().url)
          val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onPageLoad(request)
          val result = contentAsString(eventualResult)

          status(eventualResult) mustBe 200
          result must include(messageApi(s"eoriNumber.$traderOrAgent.$importOrExport.title"))
          result must include(messageApi(s"eoriNumber.$traderOrAgent.$importOrExport.heading"))
          result must include(messageApi("eoriNumber.hint"))
          result must include(messageApi("eoriNumber.a.text"))
          result must include(messageApi("eoriNumber.a.href"))
        }
      }

      "onSubmit" should {
        s"redirect to next page after successful form submit for $traderOrAgent $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()
          val request = buildGet(routes.EoriNumberController.onSubmit().url)
            .withFormUrlEncodedBody("eori" -> "GB123456780000")

          val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit(request)

          status(eventualResult) mustBe 303
          redirectLocation(eventualResult) mustBe Some(routes.TravellerDetailsController.onPageLoad().url)
        }

        s"return 400 with any form errors for $traderOrAgent $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()
          val request = buildGet(routes.EoriNumberController.onSubmit().url)
            .withFormUrlEncodedBody("eori" -> "in valid")

          val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit()(request)
          val result = contentAsString(eventualResult)

          status(eventualResult) mustBe 400
          result must include(messageApi("eoriNumber.error.invalid"))
        }

        s"return 400 with required form errors for $traderOrAgent $importOrExport" in {
          givenTheUserIsAuthenticatedAndAuthorised()
          val request = buildGet(routes.EoriNumberController.onSubmit().url)
            .withFormUrlEncodedBody("eori" -> "")

          val eventualResult = controller(givenADeclarationJourneyIsPersistedWithStub(journey)).onSubmit()(request)
          val result = contentAsString(eventualResult)

          status(eventualResult) mustBe 400
          result must include(messageApi(s"eoriNumber.$traderOrAgent.$importOrExport.error.required"))
        }
      }

      s"return a form error if API EROI validation fails for $traderOrAgent $importOrExport" in {
        givenTheUserIsAuthenticatedAndAuthorised()
        val client = app.injector.instanceOf[HttpClient]
        val connector = new MibConnector(client, "some url") {
          override def checkEoriNumber(eori: String)(implicit hc: HeaderCarrier): Future[CheckResponse] =
            Future.successful(CheckResponse("123", valid = false, None))
        }
        val controller = new EoriNumberController(
          component,
          stubProvider(completedDeclarationJourney),
          stubRepo(completedDeclarationJourney),
          view,
          connector)

        val result = controller.onSubmit()(
          buildPost(routes.EoriNumberController.onSubmit().url, aSessionId)
            .withFormUrlEncodedBody(("eori", "GB123467800022"))
        )

        status(result) mustBe 400
        contentAsString(result) must include(messages("eoriNumber.error.notFound"))
        contentAsString(result) must include("GB123467800022")
      }
    }
  }
}
