/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{AmountInPence, ChargeReference, CsgTpsProviderId, TraderName}

case class DeclarationRequest(traderName: TraderName, amount: AmountInPence, csgTpsProviderId: CsgTpsProviderId, chargeReference: ChargeReference)

object DeclarationRequest {
  implicit val format: Format[DeclarationRequest] = Json.format
}
