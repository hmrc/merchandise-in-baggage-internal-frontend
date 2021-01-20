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

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.TpsPaymentsBackendConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentCalculations
import uk.gov.hmrc.merchandiseinbaggage.model.tpspayments.{PaymentSpecificData, TpsId, TpsPaymentsItem, TpsPaymentsRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TpsPaymentsService @Inject()(connector: TpsPaymentsBackendConnector)(implicit ec: ExecutionContext, appConfig: AppConfig) {

  def createTpsPayments(pid: String, declaration: Declaration, paymentDue: PaymentCalculations)(
    implicit hc: HeaderCarrier): Future[TpsId] = {
    val request = TpsPaymentsRequest(
      pid = pid,
      payments = Seq(
        TpsPaymentsItem(
          chargeReference = declaration.mibReference.value,
          customerName = declaration.nameOfPersonCarryingTheGoods.toString,
          amount = paymentDue.totalTaxDue.inPounds,
          paymentSpecificData = PaymentSpecificData(
            declaration.mibReference.value,
            paymentDue.totalVatDue.inPounds,
            paymentDue.totalDutyDue.inPounds
          )
        )
      ),
      navigation = appConfig.tpsNavigation
    )

    connector.createTpsPayments(request)
  }

}
