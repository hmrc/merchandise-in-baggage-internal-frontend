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

package uk.gov.hmrc.merchandiseinbaggage.support

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository

trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach

trait BaseSpecWithApplication
    extends BaseSpec with GuiceOneServerPerSuite with WireMockSupport with MongoConfiguration with ScalaFutures with CoreTestData {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  implicit val headerCarrier = HeaderCarrier()

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(configMap).build()

  override implicit lazy val app = fakeApplication()
  lazy val injector: Injector = app.injector

  lazy val declarationJourneyRepository: DeclarationJourneyRepository = injector.instanceOf[DeclarationJourneyRepository]

  lazy val messageApi: Map[String, String] = injector.instanceOf[MessagesApi].messages("default")
  implicit lazy val messages: Messages = injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

  private val configMap: Map[String, Any] = Map[String, Any](
    "application.router"                                 -> "testOnlyDoNotUseInAppConf.Routes",
    "microservice.services.auth.port"                    -> WireMockSupport.port,
    "microservice.services.address-lookup-frontend.port" -> WireMockSupport.port,
    "microservice.services.merchandise-in-baggage.port"  -> WireMockSupport.port,
    "microservice.services.tps-payments-backend.port"    -> WireMockSupport.port
  )

  override def beforeEach(): Unit = declarationJourneyRepository.deleteAll().futureValue
}
