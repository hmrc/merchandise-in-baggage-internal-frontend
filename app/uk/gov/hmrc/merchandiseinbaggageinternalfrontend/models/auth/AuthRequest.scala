/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.models.auth

import play.api.mvc.{Request, Session, WrappedRequest}
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments, InsufficientEnrolments}
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

    enrolments.find(_.key == Admin.key) match {
      case Some(_) => AuthRequest(Admin, credentials.providerId, request)
      case None => throw new InsufficientEnrolments()
    }
  }
}
