/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core

sealed trait BusinessError extends Exception
case object InvalidDeclarationRequest extends BusinessError
case object DeclarationNotFound extends BusinessError
case object InvalidResponse extends BusinessError