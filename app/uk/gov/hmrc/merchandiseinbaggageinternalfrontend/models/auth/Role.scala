/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.models.auth

import play.api.libs.json._

sealed abstract class Role(val key: String)

case object Admin extends Role("mib_admin")

object Role {
  implicit val writes: Writes[Role] = new Writes[Role] {
    override def writes(o: Role): JsValue = JsString(o.toString)
  }
}
