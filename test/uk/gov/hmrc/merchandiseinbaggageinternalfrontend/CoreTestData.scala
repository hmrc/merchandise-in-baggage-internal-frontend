/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend

import java.util.UUID.randomUUID

import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core._

trait CoreTestData {

  val aTraderName: TraderName = TraderName("name")
  val anAmount: AmountInPence = AmountInPence(1)
  val aCsgTpsProviderId: CsgTpsProviderId = CsgTpsProviderId("123")
  val aChargeReference: ChargeReference = ChargeReference("ref")

  def aDeclaration: Declaration =
    Declaration(DeclarationId(randomUUID().toString), aTraderName, anAmount, aCsgTpsProviderId, aChargeReference)

  def aDeclarationRequest: DeclarationRequest = DeclarationRequest(aTraderName, anAmount, aCsgTpsProviderId, aChargeReference)
}
