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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import pureconfig.ConfigSource
import pureconfig.generic.auto._ // Do not remove this
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfigSource.configSource

@Singleton
class AppConfig @Inject()(val config: Configuration, val env: Environment) extends MIBBackendServiceConf with MongoConfiguration {

  val serviceIdentifier = "mib"

  lazy val strideRole: String = config.get[String]("stride.role")

  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  val feedbackUrl: String = {
    val url = configSource("microservice.services.feedback-frontend.url").loadOrThrow[String]
    s"$url/$serviceIdentifier"
  }
}

trait MIBBackendServiceConf {
  lazy val mibBackendServiceConf: MIBBackEndServiceConfiguration =
    ConfigSource.default.at("declaration").loadOrThrow[MIBBackEndServiceConfiguration]
  import mibBackendServiceConf._
  lazy val mibBackendBaseUri = s"$protocol://$host:$port"
}

case class MIBBackEndServiceConfiguration(protocol: String, port: Int, host: String, url: String)

trait MongoConfiguration {
  lazy val mongoConf: MongoConf = configSource("mongodb").loadOrThrow[MongoConf]
}

final case class MongoConf(uri: String, host: String = "localhost", port: Int = 27017, collectionName: String = "declaration")

object AppConfigSource {
  val configSource: String => ConfigSource = ConfigSource.default.at
}
