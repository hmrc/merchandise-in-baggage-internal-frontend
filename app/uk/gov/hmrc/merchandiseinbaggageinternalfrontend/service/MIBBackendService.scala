/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.MIBBackendServiceConf
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationId
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import scala.concurrent.{ExecutionContext, Future}

trait MIBBackendService extends MIBBackendServiceConf {

  def addDeclaration(httpClient: HttpClient, requestBody: DeclarationRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient.POST(s"$mibBackendBaseUri${mibBackendServiceConf.url}/declarations", Json.toJson(requestBody))

  def declarationById(httpClient: HttpClient, declarationId: DeclarationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient.GET(s"$mibBackendBaseUri${mibBackendServiceConf.url}/declarations/${declarationId.value}")
}
