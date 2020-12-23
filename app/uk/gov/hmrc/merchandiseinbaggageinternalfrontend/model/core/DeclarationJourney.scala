/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core

import java.text.NumberFormat.getCurrencyInstance
import java.time.{LocalDateTime, ZoneOffset}
import java.util.Locale.UK
import java.util.UUID.randomUUID

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, OFormat}

case class SessionId(value: String)

object SessionId {
  implicit val format: Format[SessionId] = implicitly[Format[String]].inmap(SessionId(_), _.value)

  def apply(): SessionId = SessionId(randomUUID().toString)
}


case class CategoryQuantityOfGoods(category: String, quantity: String)

object CategoryQuantityOfGoods {
  implicit val format: OFormat[CategoryQuantityOfGoods] = Json.format[CategoryQuantityOfGoods]
}

case class AmountInPence(value: Long) {
  val inPounds: BigDecimal = (BigDecimal(value) / 100).setScale(2)
  val formattedInPounds: String = getCurrencyInstance(UK).format(inPounds)
  val formattedInPoundsUI: String = formattedInPounds.split("\\.00")(0) //TODO not sure if necessary different formats
}

object AmountInPence {
  implicit val format: Format[AmountInPence] = implicitly[Format[Long]].inmap(AmountInPence(_), _.value)

  def fromBigDecimal(in: BigDecimal): AmountInPence = AmountInPence((in * 100).toLong)
}


case class DeclarationJourney(sessionId: SessionId,
                              declarationType: DeclarationType,
                              createdAt: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
                             )

object DeclarationJourney extends MongoDateTimeFormats {
  implicit val format: OFormat[DeclarationJourney] = Json.format[DeclarationJourney]

  val id = "sessionId"
}

