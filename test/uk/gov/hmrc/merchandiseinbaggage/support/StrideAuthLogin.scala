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

package uk.gov.hmrc.merchandiseinbaggage.support

import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.merchandiseinbaggage.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import com.typesafe.config.ConfigFactory

trait StrideAuthLogin extends BeforeAndAfterEach { this: Suite =>

  override def beforeEach() {
    super.beforeEach()
    if (StrideAuthLogin.internal) givenTheUserIsAuthenticatedAndAuthorised()
  }
}

object StrideAuthLogin {
  private lazy val internal: Boolean =
    ConfigFactory.load.getString("appName").contains("internal")
}
