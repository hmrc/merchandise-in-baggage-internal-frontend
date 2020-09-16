/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.DeclarationIdResponse
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{Declaration, DeclarationId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.BaseSpecWithWireMock

import scala.concurrent.ExecutionContext.Implicits.global

class MIBBackendServiceSpec extends BaseSpecWithWireMock with CoreTestData with ScalaFutures {

  private val httpClient: HttpClient = injector.instanceOf[HttpClient]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  "makes a http DeclarationRequest to MIB-BE" in new MIBBackendService {
    val declarationRequest = aDeclarationRequest
    val declarationIdResponse: DeclarationIdResponse = DeclarationIdResponse(DeclarationId("123"))

    mibBackendMockServer
      .stubFor(post(urlPathEqualTo(s"${mibBackendServiceConf.url}"))
        .withRequestBody(equalToJson(Json.toJson(declarationRequest).toString, true, false))
        .willReturn(okJson(Json.toJson(declarationIdResponse).toString).withStatus(201))
      )

    addDeclaration(httpClient, declarationRequest).futureValue mustBe DeclarationIdResponse(DeclarationId("123"))
  }

  "makes a http Declaration request to MIB-BE to find by id" in new MIBBackendService {
    val declarationId = DeclarationId("123")
    val stubbedDeclaration: Declaration = aDeclaration.copy(declarationId = declarationId)

    mibBackendMockServer
      .stubFor(get(urlPathEqualTo(s"${mibBackendServiceConf.url}/123"))
        .willReturn(okJson(Json.toJson(stubbedDeclaration).toString).withStatus(200))
      )

    declarationById(httpClient, declarationId).futureValue mustBe stubbedDeclaration
  }
}
