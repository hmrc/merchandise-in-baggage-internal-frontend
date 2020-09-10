/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import pureconfig.ConfigSource
import pureconfig.generic.auto._ // Do not remove this

@Singleton
class AppConfig @Inject()(val config: Configuration, val env: Environment) {

  lazy val strideRole: String = config.get[String]("stride.role")

  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())
}


trait MIBBackendServiceConf {
  lazy val mibBackendServiceConf: MIBBackEndServiceConfiguration =
    ConfigSource.default.at("declaration").loadOrThrow[MIBBackEndServiceConfiguration]
  import mibBackendServiceConf._
  lazy val mibBackendBaseUri = s"$protocol://$host:$port"
}

case class MIBBackEndServiceConfiguration(protocol: String, port: Int, host: String, url: String)