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

import org.scalatest.prop.{TableFor1, TableFor2}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.Call
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, GoodsDestination, JourneyType, YesNo}

trait PropertyBaseTables extends ScalaCheckPropertyChecks {

  val declarationTypesTable: TableFor1[DeclarationType] = Table("declarationType", Import, Export)

  val journeyTypesTable: TableFor1[JourneyType] = Table("journeyType", New, Amend)

  val traderYesOrNoAnswer: TableFor2[YesNo, String] = Table(
    ("answer", "trader or agent"),
    (Yes, "agent"),
    (No, "trader")
  )

  val goodsDestinationAnswer: TableFor2[GoodsDestination, String] = Table(
    ("radio button NorthernIreland/GreatBritain", "redirectTo"),
    (GreatBritain, ExciseAndRestrictedGoodsController.onPageLoad().url),
    (NorthernIreland, CannotUseServiceIrelandController.onPageLoad().url)
  )

  val valueOfWeighOfGoodsAnswer: TableFor2[YesNo, String] = Table(
    ("radio button yes/no", "redirectTo"),
    (No, CannotUseServiceController.onPageLoad().url),
    (Yes, GoodsTypeController.onPageLoad(1).url)
  )

  val customAgentYesOrNoAnswer: TableFor2[YesNo, String] = yesOrNoTable(
    AgentDetailsController.onPageLoad(),
    EoriNumberController.onPageLoad()
  )

  val vehicleRegistrationNumberAnswer: TableFor2[YesNo, String] = yesOrNoTable(
    VehicleRegistrationNumberController.onPageLoad(),
    CannotUseServiceController.onPageLoad()
  )

  val reviewGoodsAnswer: TableFor2[YesNo, String] = Table(
    ("radio button yes/no", "redirectTo"),
    (Yes, GoodsTypeController.onPageLoad(2).url),
    (No, PaymentCalculationController.onPageLoad().url)
  )

  val paymentCalculationThreshold: TableFor2[String, String] = Table(
    ("threshold", "redirectTo"),
    ("150001", GoodsOverThresholdController.onPageLoad().url),
    ("99.99", CustomsAgentController.onPageLoad().url)
  )

  val exciseAndRestrictedGoodsYesOrNoAnswer: TableFor2[YesNo, String] = yesOrNoTable(
    CannotUseServiceController.onPageLoad(),
    ValueWeightOfGoodsController.onPageLoad()
  )

  val goodsInVehicleAnswer: TableFor2[YesNo, String] = yesOrNoTable(
    VehicleSizeController.onPageLoad(),
    CheckYourAnswersController.onPageLoad()
  )

  val removeGoodsAnswer: TableFor2[YesNo, String] = yesOrNoTable(
    GoodsRemovedController.onPageLoad(),
    ReviewGoodsController.onPageLoad()
  )

  private def yesOrNoTable(yesUrl: Call, noUrl: Call): TableFor2[YesNo, String] = Table(
    ("radio button yes/no", "redirectTo"),
    (Yes, yesUrl.url),
    (No, noUrl.url)
  )
}
