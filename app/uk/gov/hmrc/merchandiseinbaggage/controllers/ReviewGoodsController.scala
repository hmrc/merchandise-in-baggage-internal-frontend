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

import cats.data.OptionT
import cats.instances.future._
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.{declarationNotFoundMessage, goodsDeclarationIncompleteMessage}
import uk.gov.hmrc.merchandiseinbaggage.forms.ReviewGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, ThresholdAllowance, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{GoodsDestination, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries}
import uk.gov.hmrc.merchandiseinbaggage.navigation.ReviewGoodsRequest
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.views.html.ReviewGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewGoodsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: ReviewGoodsView,
  calculationService: CalculationService,
  navigator: Navigator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    routes.PurchaseDetailsController.onPageLoad(request.declarationJourney.goodsEntries.entries.size)

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    import request.declarationJourney._
    validateRequest(maybeGoodsDestination, goodsEntries)
      .fold(actionProvider.invalidRequest(goodsDeclarationIncompleteMessage)) { allowance =>
        Ok(view(form, allowance, backButtonUrl, declarationType, journeyType))
      }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    import request.declarationJourney._
    validateRequest(maybeGoodsDestination, goodsEntries)
      .foldF(actionProvider.invalidRequestF(goodsDeclarationIncompleteMessage)) { thresholdAllowance =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, thresholdAllowance, backButtonUrl, declarationType, journeyType))),
            redirectTo
          )
      }
  }

  private def validateRequest(maybeGoodsDestination: Option[GoodsDestination], goodsEntries: GoodsEntries)(
    implicit request: DeclarationJourneyRequest[_]): OptionT[Future, ThresholdAllowance] =
    for {
      entries     <- OptionT.fromOption(goodsEntries.declarationGoodsIfComplete)
      destination <- OptionT.fromOption(maybeGoodsDestination)
      calculation <- OptionT.liftF(calculationService.paymentCalculations(entries.goods, destination))
    } yield ThresholdAllowance(entries, calculation, destination)

  private def redirectTo(declareMoreGoods: YesNo)(implicit request: DeclarationJourneyRequest[_]): Future[Result] =
    (for {
      check <- checkThresholdIfAmending(request.declarationJourney)
      call <- OptionT.liftF(
               navigator.nextPage(
                 ReviewGoodsRequest(
                   declareMoreGoods,
                   request.declarationJourney,
                   check.thresholdCheck,
                   repo.upsert
                 )
               ))
    } yield call).fold(actionProvider.invalidRequest(declarationNotFoundMessage))(Redirect)

  private def checkThresholdIfAmending(declarationJourney: DeclarationJourney)(
    implicit hc: HeaderCarrier): OptionT[Future, CalculationResponse] =
    declarationJourney.amendmentIfRequiredAndComplete
      .fold(OptionT.pure[Future](CalculationResponse(CalculationResults(Seq.empty), WithinThreshold))) { _ =>
        calculationService.amendPlusOriginalCalculations(declarationJourney)
      }
}
