/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.retrieve.Credentials

final class AuthRequest[A](val request:     Request[A],
                                    val credentials: Credentials
                                   ) extends WrappedRequest[A](request)
