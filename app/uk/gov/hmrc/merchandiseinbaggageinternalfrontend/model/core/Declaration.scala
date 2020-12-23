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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.util.ValueClassBinder.{bindableA, valueClassBinder}


case class TraderName(value: String)
object TraderName {
  implicit val format: Format[TraderName] = implicitly[Format[String]].inmap(TraderName(_), _.value)
}


case class CsgTpsProviderId(value: String)
object CsgTpsProviderId {
  implicit val format: Format[CsgTpsProviderId] = implicitly[Format[String]].inmap(CsgTpsProviderId(_), _.value)
}

case class ChargeReference(value: String)
object ChargeReference {
  implicit val format: Format[ChargeReference] = implicitly[Format[String]].inmap(ChargeReference(_), _.value)
}

case class DeclarationId(value: String)
object DeclarationId {
  implicit val format: Format[DeclarationId] = implicitly[Format[String]].inmap(DeclarationId(_), _.value)
  implicit val binder: PathBindable[DeclarationId] = valueClassBinder(_.value)
  implicit val pathBinder: QueryStringBindable[DeclarationId] = bindableA(_.toString)
}

case class Declaration(declarationId: DeclarationId, name: TraderName, amount: AmountInPence,
                       csgTpsProviderId: CsgTpsProviderId, reference: ChargeReference)

object Declaration {
  val id = "declarationId"
  implicit val format: Format[Declaration] = Json.format
}
