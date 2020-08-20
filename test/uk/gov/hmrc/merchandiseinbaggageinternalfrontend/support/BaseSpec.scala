/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ErrorTemplate

trait BaseSpec extends AnyWordSpec with Matchers

trait BaseSpecWithApplication extends BaseSpec with GuiceOneAppPerSuite with WireMockSupport {
  lazy val injector: Injector = app.injector
  lazy val messagesApi = app.injector.instanceOf[MessagesApi]
  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  lazy val errorHandlerTemplate = injector.instanceOf[ErrorTemplate]
  lazy val strideAuthAction = injector.instanceOf[StrideAuthAction]
  implicit lazy val appConfig = injector.instanceOf[AppConfig]
  implicit def messages[A](fakeRequest: FakeRequest[A]): Messages = messagesApi.preferred(fakeRequest)
  implicit val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}
