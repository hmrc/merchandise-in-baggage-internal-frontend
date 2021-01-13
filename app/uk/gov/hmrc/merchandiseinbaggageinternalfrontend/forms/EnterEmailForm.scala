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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms

import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, Forms}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms.mappings.Mappings
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.Email

object EnterEmailForm extends Mappings {

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private val emailAddress: Constraint[Option[String]] = Constraint[Option[String]]("constraint.email") {
    case None => Valid
    case Some(email) =>
      emailRegex
        .findFirstMatchIn(email)
        .map(_ => Valid)
        .getOrElse(Invalid("enterEmail.error.invalid"))
  }

  val form: Form[Option[Email]] = Form(
    mapping(
      "email" -> optional(Forms.text).verifying(emailAddress)
    )(p => p.map(Email(_)))(mayBeEmail => Some(mayBeEmail.map(_.email)))
  )
}
