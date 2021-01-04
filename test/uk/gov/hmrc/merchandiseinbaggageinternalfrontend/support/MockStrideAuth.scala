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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object MockStrideAuth {
  val expectedDetail = "SessionRecordNotFound"

  def givenTheUserIsNotAuthenticated(): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("WWW-Authenticate", s"""MDTP detail="$expectedDetail"""")))

  def givenTheUserIsAuthenticatedAndAuthorised(): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .withRequestBody(equalToJson(
          s"""
             |{
             |  "authorise": [
             |    {
             |     "identifiers":[],
             |     "state":"Activated",
             |     "enrolment":"digital_tps_payment_taker_call_handler"
             |    },
             |    {
             |      "authProviders": [
             |        "PrivilegedApplication"
             |      ]
             |    }
             |  ]
             |}
           """.stripMargin,
          true,
          true
        ))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(s"""
                       |{
                       |  "optionalCredentials":{
                       |    "providerId": "userId",
                       |    "providerType": "PrivilegedApplication"
                       |  }
                       |}
       """.stripMargin)))

  def givenTheUserHasNoCredentials(): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .withRequestBody(equalToJson(
          s"""
             |{
             |  "authorise": [
             |    {
             |     "identifiers":[],
             |     "state":"Activated",
             |     "enrolment":"digital_tps_payment_taker_call_handler"
             |    },
             |    {
             |      "authProviders": [
             |        "PrivilegedApplication"
             |      ]
             |    }
             |  ]
             |}
           """.stripMargin,
          true,
          true
        ))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody("{}")))

  def givenAuthFailsWith(error: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("WWW-Authenticate", s"""MDTP detail=\"$error\"""")))
}
