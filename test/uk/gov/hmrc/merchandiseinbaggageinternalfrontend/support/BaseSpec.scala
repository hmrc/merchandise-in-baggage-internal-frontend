/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.POST
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.{AppConfig, MongoConfiguration}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.DeclarationJourneyActionProvider
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository

import scala.concurrent.duration._
import scala.concurrent.Await

trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach

trait BaseSpecWithApplication
    extends BaseSpec with GuiceOneAppPerSuite with WireMockSupport with MongoConfiguration with ScalaFutures with CoreTestData {
  lazy val injector: Injector = fakeApplication().injector
  lazy val component: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val strideAuth: StrideAuthAction = injector.instanceOf[StrideAuthAction]
  implicit lazy val appConf: AppConfig = injector.instanceOf[AppConfig]
  lazy val messageApi: Map[String, String] = app.injector.instanceOf[MessagesApi].messages("default")
  lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

  lazy val repo = injector.instanceOf[DeclarationJourneyRepository]
  lazy val actionProvider = injector.instanceOf[DeclarationJourneyActionProvider]

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(configMap).build()

  private val configMap: Map[String, Any] = Map[String, Any](
    "application.router"              -> "testOnlyDoNotUseInAppConf.Routes",
    "microservice.services.auth.port" -> WireMockSupport.port
  )

  def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def givenADeclarationJourneyIsPersisted(declarationJourney: DeclarationJourney): DeclarationJourney =
    repo.insert(declarationJourney).futureValue

  override def beforeEach(): Unit = {
    super.beforeEach()
    Await.ready(repo.deleteAll(), 5 seconds)
  }
}
