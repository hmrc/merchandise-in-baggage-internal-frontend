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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.{goodsDeclarationIncompleteMessage, goodsDestinationUnansweredMessage}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationGoods, GoodsDestination, PaymentCalculations}
import uk.gov.hmrc.merchandiseinbaggage.model.currencyconversion.ConversionRatePeriod
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.views.html.PaymentCalculationView

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

@Singleton
class PaymentCalculationController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  calculationService: CalculationService,
  view: PaymentCalculationView)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends DeclarationJourneyController {

  private val backButtonUrl: Call =
    routes.ReviewGoodsController.onPageLoad()

  private def checkYourAnswersIfComplete(default: Call)(implicit request: DeclarationJourneyRequest[_]): Call =
    if (request.declarationJourney.declarationRequiredAndComplete) routes.CheckYourAnswersController.onPageLoad()
    else default

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete
      .fold(actionProvider.invalidRequestF(goodsDeclarationIncompleteMessage)) { goods =>
        request.declarationJourney.maybeGoodsDestination
          .fold(actionProvider.invalidRequestF(goodsDestinationUnansweredMessage)) { destination =>
            request.declarationJourney.declarationType match {
              case Import => handleImport(goods, destination)
              case Export => handleExport(goods, destination)
            }
          }
      }
  }

  private def handleImport(goods: DeclarationGoods, destination: GoodsDestination)(
    implicit hc: HeaderCarrier,
    req: DeclarationJourneyRequest[_]): Future[Result] =
    for {
      paymentCalculations <- calculationService.paymentCalculation(goods)
      rates               <- calculationService.getConversionRates(goods)
    } yield redirectImport(destination, paymentCalculations, rates)

  private def handleExport(goods: DeclarationGoods, destination: GoodsDestination): Future[Result] =
    if (goods.goods.map(_.purchaseDetails.numericAmount).sum > destination.threshold.inPounds)
      Future successful Redirect(routes.GoodsOverThresholdController.onPageLoad())
    else
      Future successful Redirect(routes.CustomsAgentController.onPageLoad())

  private def redirectImport(destination: GoodsDestination, paymentCalculations: PaymentCalculations, rates: Seq[ConversionRatePeriod])(
    implicit req: DeclarationJourneyRequest[_]): Result =
    if (paymentCalculations.totalGbpValue.value > destination.threshold.value)
      Redirect(routes.GoodsOverThresholdController.onPageLoad())
    else
      Ok(view(paymentCalculations, rates, checkYourAnswersIfComplete(routes.CustomsAgentController.onPageLoad()), backButtonUrl))
}
