/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object MockStrideAuth {
  val expectedDetail = "SessionRecordNotFound"

  def givenTheUserIsNotAuthenticated(): StubMapping =
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail="$expectedDetail"""")
      )
    )

  def givenTheUserIsAuthenticatedAndAuthorised(): StubMapping =
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
             |        "PrivilegedApplication"
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
             |    "providerId": "userId",
             |    "providerType": "PrivilegedApplication"
             |  }
             |}
       """.stripMargin)))

  def givenTheUserHasNoCredentials(): StubMapping =
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
             |        "PrivilegedApplication"
             |      ]
             |    }
             |  ]
             |}
           """.stripMargin, true, true))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody("{}")))


  def givenAuthFailsWith(error: String): StubMapping =
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate", s"""MDTP detail=\"$error\"""")
      )
    )
}
