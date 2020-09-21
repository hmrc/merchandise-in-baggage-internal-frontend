/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.MIBBackendServiceConf
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.routes
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationId

trait BaseSpec extends AnyWordSpec with Matchers

trait BaseSpecWithApplication extends BaseSpec with GuiceOneAppPerSuite with WireMockSupport {
  lazy val injector: Injector = fakeApplication().injector
  lazy val component: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val strideAuth: StrideAuthAction = injector.instanceOf[StrideAuthAction]

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(configMap).build()

  private val configMap: Map[String, Any] = Map[String, Any](
    "application.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "microservice.services.auth.port" -> WireMockSupport.port
  )

  def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def findDeclarationRequestGET(declarationId: DeclarationId): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, routes.DeclarationTestOnlyController.findDeclaration(declarationId).url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def declarationRequestGET(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, routes.DeclarationTestOnlyController.declarations().url)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}

trait BaseSpecWithWireMock extends BaseSpecWithApplication with MIBBackendServiceConf {

  val mibBackendMockServer = new WireMockServer(mibBackendServiceConf.port)

  override def beforeEach: Unit = mibBackendMockServer.start()

  override def afterEach: Unit = mibBackendMockServer.stop()
}