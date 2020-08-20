/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object AuthWireMockResponses {

  val expectedDetail = "SessionRecordNotFound"

  def notAuthorised: StubMapping = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail="$expectedDetail"""")
      )
    )
  }

  def authorised(authProvider: String, strideUserId: String): StubMapping = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .withRequestBody(
        equalToJson(
          s"""
             |{
             |  "authorise": [
             |    {
             |     "identifiers":[],
             |     "state":"Activated",
             |     "enrolment":"mib"
             |    },
             |    {
             |      "authProviders": [
             |        "$authProvider"
             |      ]
             |    }
             |  ]
             |}
           """.stripMargin, true, true))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(
          s"""
             |{
             |  "optionalCredentials":{
             |    "providerId": "$strideUserId",
             |    "providerType": "$authProvider"
             |  }
             |}
       """.stripMargin)))

  }

  def failsWith(error: String): StubMapping = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail=\"$error\"""")
      )
    )
  }

}
