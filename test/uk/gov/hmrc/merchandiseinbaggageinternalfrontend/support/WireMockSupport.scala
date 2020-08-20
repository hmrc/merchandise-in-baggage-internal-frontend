/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait WireMockSupport extends BeforeAndAfterAll with BeforeAndAfterEach {
  self: Suite =>

  implicit val wireMockServer: WireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(WireMockSupport.port))

  val wireMockHost = "localhost"

  val wireMockBaseUrlAsString = s"http://$wireMockHost:$WireMockSupport.port"

  WireMock.configureFor(WireMockSupport.port)

  override def beforeEach(): Unit = WireMock.reset()

  override protected def beforeAll(): Unit = wireMockServer.start()

  override protected def afterAll(): Unit = wireMockServer.stop()
}

object WireMockSupport {
  val port: Int = 11111
}
