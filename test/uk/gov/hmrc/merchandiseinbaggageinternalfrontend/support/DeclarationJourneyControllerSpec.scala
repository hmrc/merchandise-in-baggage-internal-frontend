/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support

import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DefaultDB
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.connectors.{AddressLookupFrontendConnector, MibConnector}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.DeclarationJourneyActionProvider
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service.{CalculationService, TpsPaymentsService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DeclarationJourneyControllerSpec extends BaseSpecWithApplication {
  lazy val injector: Injector = app.injector
  implicit lazy val appConf: AppConfig = injector.instanceOf[AppConfig]

  lazy val component: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val strideAuth: StrideAuthAction = injector.instanceOf[StrideAuthAction]
  lazy val messageApi: Map[String, String] = injector.instanceOf[MessagesApi].messages("default")
  lazy val messages: Messages = injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

  lazy val repo = injector.instanceOf[DeclarationJourneyRepository]
  lazy val defaultBuilder = injector.instanceOf[DefaultActionBuilder]
  lazy val actionProvider = injector.instanceOf[DeclarationJourneyActionProvider]
  lazy val addressLookupConnector = injector.instanceOf[AddressLookupFrontendConnector]
  lazy val calculationService = injector.instanceOf[CalculationService]
  lazy val tpsPaymentsService = injector.instanceOf[TpsPaymentsService]
  lazy val mibConnector = injector.instanceOf[MibConnector]

  def buildPost(url: String, sessionId: SessionId = SessionId("123")): FakeRequest[AnyContentAsEmpty.type] =
    buildRequest(POST, url, sessionId)

  def buildGet(url: String, sessionId: SessionId = SessionId("123")): FakeRequest[AnyContentAsEmpty.type] =
    buildRequest(GET, url, sessionId)

  private def buildRequest(httpVerbs: String, url: String, sessionId: SessionId): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(httpVerbs, url)
      .withSession(SessionKeys.sessionId -> sessionId.value)
      .withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  private lazy val db: () => DefaultDB = app.injector.instanceOf[ReactiveMongoComponent].mongoConnector.db

  lazy val stubRepo: DeclarationJourney => DeclarationJourneyRepository = declarationJourney =>
    new DeclarationJourneyRepository(db) {
      override def insert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] = Future.successful(declarationJourney)
      override def findBySessionId(sessionId: SessionId): Future[Option[DeclarationJourney]] = Future.successful(Some(declarationJourney))
      override def upsert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] = Future.successful(declarationJourney)
  }

  def givenADeclarationJourneyIsPersisted(declarationJourney: DeclarationJourney): DeclarationJourney =
    stubRepo(declarationJourney).findBySessionId(declarationJourney.sessionId).futureValue.get

  lazy val stubProvider: DeclarationJourney => DeclarationJourneyActionProvider = declarationJourney =>
    new DeclarationJourneyActionProvider(defaultBuilder, stubRepo(declarationJourney), strideAuth)
}
