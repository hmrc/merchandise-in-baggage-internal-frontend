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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import javax.inject.Inject
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationJourneyRepository @Inject()(mongo: () => DB)
    extends ReactiveRepository[DeclarationJourney, String](
      "declarationJourney",
      mongo,
      DeclarationJourney.format,
      implicitly[Format[String]]) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(id -> Ascending), Option("primaryKey"), unique = true)
  )

  private[repositories] lazy val indices: Future[List[Index]] = collection.indexesManager.list()

  def insert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] =
    super.insert(declarationJourney).map(_ => declarationJourney)

  def findBySessionId(sessionId: SessionId): Future[Option[DeclarationJourney]] = {
    val query: (String, JsValueWrapper) = id -> JsString(sessionId.value)
    find(query).map(_.headOption)
  }

  def deleteAll(): Future[Unit] = {
    logger.warn("DeclarationJourneyRepository.deleteAll() called")

    super.removeAll().map(_ => ())
  }

  implicit val jsObjectWriter: OWrites[JsObject] = new OWrites[JsObject] {
    override def writes(o: JsObject): JsObject = o
  }

  def upsert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] =
    collection
      .update(ordered = false)
      .one(Json.obj(id -> declarationJourney.sessionId.value), declarationJourney, upsert = true)
      .map(_ => declarationJourney)
}
