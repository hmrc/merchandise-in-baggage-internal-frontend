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

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.{AppConfig, MIBBackendServiceConf}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.testonly
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationId

trait BaseSpec extends AnyWordSpec with Matchers

trait BaseSpecWithApplication extends BaseSpec with GuiceOneAppPerSuite with WireMockSupport {
  lazy val injector: Injector = fakeApplication().injector
  lazy val component: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val strideAuth: StrideAuthAction = injector.instanceOf[StrideAuthAction]
  implicit lazy val appConf: AppConfig = injector.instanceOf[AppConfig]
  lazy val messageApi: Map[String, String] = app.injector.instanceOf[MessagesApi].messages("default")

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(configMap).build()

  private val configMap: Map[String, Any] = Map[String, Any](
    "application.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "microservice.services.auth.port" -> WireMockSupport.port
  )

  def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def findDeclarationRequestGET(declarationId: DeclarationId): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, testonly.routes.DeclarationTestOnlyController.findDeclaration(declarationId).url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def declarationRequestGET(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, testonly.routes.DeclarationTestOnlyController.declarations().url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}

trait BaseSpecWithWireMock extends BaseSpecWithApplication with MIBBackendServiceConf {

  val mibBackendMockServer = new WireMockServer(mibBackendServiceConf.port)

  override def beforeEach: Unit = mibBackendMockServer.start()

  override def afterEach: Unit = mibBackendMockServer.stop()
}