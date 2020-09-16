/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.MIBBackendServiceConf
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.{DeclarationIdResponse, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{Declaration, DeclarationId}

import scala.concurrent.{ExecutionContext, Future}

trait MIBBackendService extends MIBBackendServiceConf {

  def addDeclaration(httpClient: HttpClient, requestBody: DeclarationRequest)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationIdResponse] =
    httpClient.POST[DeclarationRequest, DeclarationIdResponse](s"$mibBackendBaseUri${mibBackendServiceConf.url}/declarations", requestBody)

  def declarationById(httpClient: HttpClient, declarationId: DeclarationId)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
    httpClient.GET[Declaration](s"$mibBackendBaseUri${mibBackendServiceConf.url}/declarations/${declarationId.value}")
}
