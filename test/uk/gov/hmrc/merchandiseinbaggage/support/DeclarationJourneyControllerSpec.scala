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

package uk.gov.hmrc.merchandiseinbaggage.support

import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DefaultDB
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.auth.StrideAuthAction
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.{AddressLookupFrontendConnector, MibConnector}
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyActionProvider
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, TpsPaymentsService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DeclarationJourneyControllerSpec extends BaseSpecWithApplication with PropertyBaseTables {
  implicit lazy val appConf: AppConfig = injector.instanceOf[AppConfig]

  lazy val controllerComponents: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  lazy val strideAuth: StrideAuthAction = injector.instanceOf[StrideAuthAction]

  lazy val repo = injector.instanceOf[DeclarationJourneyRepository]
  lazy val defaultBuilder = injector.instanceOf[DefaultActionBuilder]
  lazy val actionProvider = injector.instanceOf[DeclarationJourneyActionProvider]
  lazy val addressLookupConnector = injector.instanceOf[AddressLookupFrontendConnector]
  lazy val calculationService = injector.instanceOf[CalculationService]
  lazy val tpsPaymentsService = injector.instanceOf[TpsPaymentsService]
  lazy val mibConnector = injector.instanceOf[MibConnector]

  private lazy val db: () => DefaultDB = app.injector.instanceOf[ReactiveMongoComponent].mongoConnector.db

  lazy val stubRepo: DeclarationJourney => DeclarationJourneyRepository = declarationJourney =>
    new DeclarationJourneyRepository(db) {
      override def insert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] = Future.successful(declarationJourney)
      override def findBySessionId(sessionId: SessionId): Future[Option[DeclarationJourney]] = Future.successful(Some(declarationJourney))
      override def upsert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] = Future.successful(declarationJourney)
  }

  lazy val stubProvider: DeclarationJourney => DeclarationJourneyActionProvider = declarationJourney =>
    new DeclarationJourneyActionProvider(defaultBuilder, stubRepo(declarationJourney), strideAuth)

  def givenADeclarationJourneyIsPersistedWithStub(declarationJourney: DeclarationJourney): DeclarationJourney =
    stubRepo(declarationJourney).findBySessionId(declarationJourney.sessionId).futureValue.get

  def buildPost(url: String, sessionId: SessionId = SessionId("123")): FakeRequest[AnyContentAsEmpty.type] =
    buildRequest(POST, url, sessionId)

  def buildGet(url: String, sessionId: SessionId = SessionId("123")): FakeRequest[AnyContentAsEmpty.type] =
    buildRequest(GET, url, sessionId)

  private def buildRequest(httpVerbs: String, url: String, sessionId: SessionId): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(httpVerbs, url)
      .withSession(SessionKeys.sessionId -> sessionId.value)
      .withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}
