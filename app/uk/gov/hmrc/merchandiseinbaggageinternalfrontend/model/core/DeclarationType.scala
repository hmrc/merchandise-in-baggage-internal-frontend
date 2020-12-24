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

import enumeratum.EnumEntry
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait DeclarationType extends EnumEntry with EnumEntryRadioItemSupport {
  val messageKey = s"${DeclarationType.baseMessageKey}.${entryName.toLowerCase}"
  implicit val format: Format[DeclarationType] = EnumFormat(DeclarationType)
}

object DeclarationType extends Enum[DeclarationType] {
  override val baseMessageKey: String = "declarationType"
  override val values: immutable.IndexedSeq[DeclarationType] = findValues

  case object Import extends DeclarationType
  case object Export extends DeclarationType
}

object DeclarationTypes extends Enum[DeclarationType] with RadioSupport[DeclarationType] {
  override val baseMessageKey: String = "importExportChoice"
  override val values: immutable.IndexedSeq[DeclarationType] = findValues

  case object MakeImport extends DeclarationType

  case object MakeExport extends DeclarationType
}
