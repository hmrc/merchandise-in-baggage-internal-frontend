/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.models.auth

import play.api.mvc.{Request, Session, WrappedRequest}
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}

case class AuthRequest[A](role: Role, providerId: String, request: Request[A], extraSessionEntries: Seq[(String, String)] = Seq.empty)
  extends WrappedRequest[A](request) {

  override lazy val session: Session = Session(request.session.data ++ extraSessionEntries)
}

object AuthRequest {

  type AuthRetrievals = Option[Credentials] ~ Enrolments

  def apply[A](retrievals: AuthRetrievals)(implicit request: Request[A]): AuthRequest[A] = {
    val enrolments:  Set[Enrolment] = retrievals.b.enrolments
    val credentials: Credentials    = retrievals.a.getOrElse(throw new Exception("No credentials retrieved for user"))

    val key: String = enrolments.head.key.toLowerCase
    if (key != enrolments.last.key.toLowerCase) throw new MultipleRoleException

    val role: Role = key match {
      case _ | Admin.key => Admin
    }

    AuthRequest(role, credentials.providerId, request)
  }
}

class MultipleRoleException extends RuntimeException
