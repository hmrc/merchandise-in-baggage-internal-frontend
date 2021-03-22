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

import com.google.inject.{Inject, Singleton}
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.declarationNotFoundMessage
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.DeclarationConfirmationController
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, Declaration, DeclarationId, DeclarationType}
import uk.gov.hmrc.merchandiseinbaggage.service.{CalculationService, TpsPaymentsService}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersAmendHandler @Inject()(
  actionProvider: DeclarationJourneyActionProvider,
  calculationService: CalculationService,
  tpsPaymentsService: TpsPaymentsService,
  amendImportView: CheckYourAnswersAmendImportView,
  amendExportView: CheckYourAnswersAmendExportView)(implicit val ec: ExecutionContext, val appConfig: AppConfig) {

  def onPageLoad(declarationType: DeclarationType, amendment: Amendment, declarationId: DeclarationId)(
    implicit hc: HeaderCarrier,
    request: Request[_],
    messages: Messages): Future[Result] =
    declarationType match {
      case Import => onPageLoadImport(amendment, declarationId)
      case Export => onPageLoadExport(amendment, declarationId)
    }

  private def onPageLoadImport(
    amendment: Amendment,
    declarationId: DeclarationId)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    for {
      calculationResults       <- calculationService.paymentCalculations(amendment.goods.importGoods)
      maybeOriginalDeclaration <- calculationService.findDeclaration(declarationId)
    } yield {
      maybeOriginalDeclaration.fold(actionProvider.invalidRequest(declarationNotFoundMessage)) { originalDeclaration =>
        originalDeclaration.maybeTotalCalculationResult.fold(actionProvider.invalidRequest(declarationNotFoundMessage)) {
          originalCalculationResults =>
            if ((calculationResults.totalGbpValue.value + originalCalculationResults.totalGbpValue.value) > originalDeclaration.goodsDestination.threshold.value) {
              Redirect(routes.GoodsOverThresholdController.onPageLoad())
            } else Ok(amendImportView(form, amendment, calculationResults))
        }
      }
    }

  private def onPageLoadExport(
    amendment: Amendment,
    declarationId: DeclarationId)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    calculationService.findDeclaration(declarationId).map { maybeOriginalDeclaration =>
      maybeOriginalDeclaration.fold(actionProvider.invalidRequest(declarationNotFoundMessage)) { originalDeclaration =>
        val originalGbpValue = originalDeclaration.declarationGoods.goods.map(_.purchaseDetails.numericAmount).sum
        val amendGbpValue = amendment.goods.goods.map(_.purchaseDetails.numericAmount).sum
        if ((originalGbpValue + amendGbpValue) > originalDeclaration.goodsDestination.threshold.inPounds) {
          Redirect(routes.GoodsOverThresholdController.onPageLoad())
        } else Ok(amendExportView(form, amendment))
      }
    }

  def onSubmit(declarationId: DeclarationId, newAmendment: Amendment)(
    implicit hc: HeaderCarrier,
    request: DeclarationJourneyRequest[_]): Future[Result] =
    calculationService.findDeclaration(declarationId).flatMap { maybeOriginalDeclaration =>
      maybeOriginalDeclaration.fold(Future.successful(actionProvider.invalidRequest(declarationNotFoundMessage))) { originalDeclaration =>
        originalDeclaration.declarationType match {
          case Export =>
            persistAndRedirect(newAmendment, originalDeclaration)
          case Import =>
            persistAndRedirectToPayments(newAmendment, originalDeclaration)
        }
      }
    }

  private def persistAndRedirect(amendment: Amendment, originalDeclaration: Declaration)(implicit hc: HeaderCarrier): Future[Result] = {
    val amendedDeclaration = originalDeclaration.copy(amendments = originalDeclaration.amendments :+ amendment)
    calculationService.amendDeclaration(amendedDeclaration).map(_ => Redirect(DeclarationConfirmationController.onPageLoad()))
  }

  private def persistAndRedirectToPayments(amendment: Amendment, originalDeclaration: Declaration)(
    implicit request: DeclarationJourneyRequest[_],
    hc: HeaderCarrier): Future[Result] =
    calculationService.paymentCalculations(amendment.goods.importGoods).flatMap { calculationResults =>
      val amendmentRef = originalDeclaration.amendments.size + 1
      val updatedAmendment =
        amendment.copy(reference = amendmentRef, maybeTotalCalculationResult = Some(calculationResults.totalCalculationResult))

      val updatedDeclaration = originalDeclaration.copy(amendments = originalDeclaration.amendments :+ updatedAmendment)

      for {
        _ <- calculationService.amendDeclaration(updatedDeclaration)
        _ <- tpsPaymentsService.createTpsPayments(request.pid, originalDeclaration, calculationResults)
      } yield Redirect(routes.DeclarationConfirmationController.onPageLoad()) //TODO not sure about navigation. Ask
    }
}
