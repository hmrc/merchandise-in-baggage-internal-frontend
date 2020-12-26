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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.connectors

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.currencyconversion.ConversionRatePeriod

import java.time.LocalDate.now
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CurrencyConversionConnector @Inject()(httpClient: HttpClient, @Named("currencyConversionBaseUrl") baseUrl: String) {

  def getConversionRate(code: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ConversionRatePeriod]] =
    httpClient.GET[Seq[ConversionRatePeriod]](s"$baseUrl/currency-conversion/rates/${now()}?cc=$code")

}
