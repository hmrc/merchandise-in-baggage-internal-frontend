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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.MIBBackendServiceConf
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.{DeclarationIdResponse, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{Declaration, DeclarationId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

trait MIBBackendService extends MIBBackendServiceConf {

  def addDeclaration(httpClient: HttpClient, requestBody: DeclarationRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[DeclarationIdResponse] =
    httpClient.POST[DeclarationRequest, DeclarationIdResponse](s"$mibBackendBaseUri${mibBackendServiceConf.url}", requestBody)

  def declarationById(httpClient: HttpClient, declarationId: DeclarationId)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Declaration] =
    httpClient.GET[Declaration](s"$mibBackendBaseUri${mibBackendServiceConf.url}/${declarationId.value}")
}
