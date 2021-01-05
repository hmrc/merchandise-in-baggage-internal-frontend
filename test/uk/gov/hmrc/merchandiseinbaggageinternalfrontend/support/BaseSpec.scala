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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.{AppConfig, MongoConfiguration}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.connectors.{AddressLookupFrontendConnector, MibConnector}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.DeclarationJourneyActionProvider
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service.{CalculationService, TpsPaymentsService}

trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach

trait BaseSpecWithApplication
    extends BaseSpec with GuiceOneAppPerSuite with WireMockSupport with MongoConfiguration with ScalaFutures with CoreTestData {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  lazy val injector: Injector = app.injector
  lazy val component: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val strideAuth: StrideAuthAction = injector.instanceOf[StrideAuthAction]
  implicit lazy val appConf: AppConfig = injector.instanceOf[AppConfig]
  lazy val messageApi: Map[String, String] = app.injector.instanceOf[MessagesApi].messages("default")
  lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

  lazy val repo = injector.instanceOf[DeclarationJourneyRepository]
  lazy val actionProvider = injector.instanceOf[DeclarationJourneyActionProvider]
  lazy val addressLookupConnector = injector.instanceOf[AddressLookupFrontendConnector]
  lazy val calculationService = injector.instanceOf[CalculationService]
  lazy val tpsPaymentsService = injector.instanceOf[TpsPaymentsService]
  lazy val mibConnector = injector.instanceOf[MibConnector]

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(configMap).build()

  private val configMap: Map[String, Any] = Map[String, Any](
    "application.router"                                 -> "testOnlyDoNotUseInAppConf.Routes",
    "microservice.services.auth.port"                    -> WireMockSupport.port,
    "microservice.services.address-lookup-frontend.port" -> WireMockSupport.port,
    "microservice.services.currency-conversion.port"     -> WireMockSupport.port,
    "microservice.services.merchandise-in-baggage.port"  -> WireMockSupport.port,
    "microservice.services.tps-payments-backend.port"    -> WireMockSupport.port
  )

  def buildPost(url: String, sessionId: SessionId = SessionId("123")): FakeRequest[AnyContentAsEmpty.type] =
    buildRequest(POST, url, sessionId)

  def buildGet(url: String, sessionId: SessionId = SessionId("123")): FakeRequest[AnyContentAsEmpty.type] =
    buildRequest(GET, url, sessionId)

  private def buildRequest(httpVerbs: String, url: String, sessionId: SessionId): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(httpVerbs, url)
      .withSession(SessionKeys.sessionId -> sessionId.value)
      .withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def givenADeclarationJourneyIsPersisted(declarationJourney: DeclarationJourney): DeclarationJourney =
    repo.insert(declarationJourney).futureValue

  override def beforeEach(): Unit = repo.deleteAll().futureValue
}
