/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.models.auth.AuthRequest
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController


@Singleton
class AuthFrontendController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  implicit def hcWithProvider[A](implicit authRequest: AuthRequest[A]): HeaderCarrier =
    hc(authRequest.request).withExtraHeaders(
      "Provider-ID" -> authRequest.providerId,
      "Role"        -> authRequest.role.toString
    )
}
