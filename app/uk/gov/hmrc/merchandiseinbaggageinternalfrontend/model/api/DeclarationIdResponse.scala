/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationId

case class DeclarationIdResponse(id: DeclarationId)
object DeclarationIdResponse {
  implicit val format: Format[DeclarationIdResponse] = Json.format
}
