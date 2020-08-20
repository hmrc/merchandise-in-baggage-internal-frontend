/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend

import com.google.inject.{AbstractModule, Provides, Singleton}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}

class Module extends AbstractModule {

  @Provides
  @Singleton
  def authorisedFunctions(ac: AuthConnector): AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = ac
  }

  override def configure(): Unit = ()
}
