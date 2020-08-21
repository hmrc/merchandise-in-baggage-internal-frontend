/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}

@Singleton
class AppConfig @Inject()(val config: Configuration, val env: Environment) {

  lazy val strideRole: String = config.get[String]("stride.role")

  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

}
