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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, _}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.LOCATION
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.adresslookup.Address

object AddressLookupFrontendStub {

  def confirmJourneyResponse(address: Address): String =
    s"""{
       |    "auditRef" : "bed4bd24-72da-42a7-9338-f43431b7ed72",
       |    "id" : "GB990091234524",
       |    "address" : ${Json.toJson(address)}
       |}""".stripMargin

  def givenInitJourney(): StubMapping =
    stubFor(
      post(urlPathEqualTo("/api/v2/init"))
        .willReturn(aResponse().withStatus(202).withHeader(LOCATION, "/blah")))

  def givenConfirmJourney(id: String, address: Address): StubMapping =
    stubFor(
      get(urlPathEqualTo("/api/confirmed"))
        .withQueryParam("id", equalTo(id))
        .willReturn(okJson(confirmJourneyResponse(address))))
}
