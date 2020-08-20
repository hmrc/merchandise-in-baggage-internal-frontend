/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.{Environment, Mode}
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class StrideAuthConnector @Inject()(val http: HttpClient,
                                    environment: Environment,
                                    servicesConfig: ServicesConfig) extends PlayAuthConnector {
  val mode:            Mode   = environment.mode
  lazy val serviceUrl: String = servicesConfig.baseUrl("auth")
}
