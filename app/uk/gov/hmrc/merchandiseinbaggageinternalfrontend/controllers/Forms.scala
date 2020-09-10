/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, _}

trait Forms {
  def declarationForm(formIdentifier: String): Form[DeclarationData] =
    Form(
      mapping(
        formIdentifier -> text
      )(DeclarationData.apply)(DeclarationData.unapply)
    )
}

object Forms {
  val declarationFormIdentifier = "declarationForm"
}

case class DeclarationData(data: String)
