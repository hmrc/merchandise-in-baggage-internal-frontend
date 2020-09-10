/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend

import java.util.UUID

import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{AmountInPence, ChargeReference, CsgTpsProviderId, Declaration, DeclarationId, Outstanding, Paid, TraderName}

trait CoreTestData {

  val aTraderName: TraderName = TraderName("name")
  val anAmount: AmountInPence = AmountInPence(1)
  val aCsgTpsProviderId: CsgTpsProviderId = CsgTpsProviderId("123")
  val aChargeReference: ChargeReference = ChargeReference("ref")

  def aDeclaration: Declaration =
    Declaration(DeclarationId(UUID.randomUUID().toString),
      aTraderName, anAmount, aCsgTpsProviderId, aChargeReference, Outstanding, None, None)

  def aDeclarationRequest: DeclarationRequest = DeclarationRequest(aTraderName, anAmount, aCsgTpsProviderId, aChargeReference)
}
