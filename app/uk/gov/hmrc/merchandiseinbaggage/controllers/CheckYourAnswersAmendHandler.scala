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
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.{DeclarationConfirmationController, _}
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResults, OverThreshold, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, Declaration, DeclarationId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
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

  //TODO make BE do ThresholdCheck for amend too so we will need one pattern match
  def onPageLoad(
    declarationJourney: DeclarationJourney,
    amendment: Amendment)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    declarationJourney.declarationType match {
      case Import => onPageLoadImport(amendment, declarationJourney)
      case Export => onPageLoadExport(amendment, declarationJourney)
    }

  private def onPageLoadImport(
    amendment: Amendment,
    declarationJourney: DeclarationJourney)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    calculationService
      .isAmendPlusOriginalOverThresholdImport(declarationJourney)
      .fold(actionProvider.invalidRequest(declarationNotFoundMessage)) { res =>
        res.thresholdCheck match {
          case OverThreshold   => Redirect(GoodsOverThresholdController.onPageLoad())
          case WithinThreshold => Ok(amendImportView(form, amendment, res.results))
        }
      }

  private def onPageLoadExport(
    amendment: Amendment,
    declarationJourney: DeclarationJourney)(implicit hc: HeaderCarrier, request: Request[_], messages: Messages): Future[Result] =
    calculationService
      .isAmendPlusOriginalOverThresholdExport(declarationJourney)
      .fold(actionProvider.invalidRequest(declarationNotFoundMessage)) { amendCalculationResult =>
        amendCalculationResult.thresholdCheck match {
          case OverThreshold   => Redirect(GoodsOverThresholdController.onPageLoad())
          case WithinThreshold => Ok(amendExportView(form, amendment))
        }
      }

  def onSubmit(declarationId: DeclarationId, pid: String, newAmendment: Amendment)(
    implicit hc: HeaderCarrier,
    request: Request[_]): Future[Result] =
    calculationService.findDeclaration(declarationId).flatMap { maybeOriginalDeclaration =>
      maybeOriginalDeclaration.fold(Future.successful(actionProvider.invalidRequest(declarationNotFoundMessage))) { originalDeclaration =>
        originalDeclaration.declarationType match {
          case Export =>
            persistAndRedirect(newAmendment, originalDeclaration)
          case Import =>
            persistAndRedirectToPayments(newAmendment, pid, originalDeclaration)
        }
      }
    }

  private def persistAndRedirect(amendment: Amendment, originalDeclaration: Declaration)(implicit hc: HeaderCarrier): Future[Result] = {
    val amendedDeclaration = originalDeclaration.copy(amendments = originalDeclaration.amendments :+ amendment)
    calculationService.amendDeclaration(amendedDeclaration).map(_ => Redirect(DeclarationConfirmationController.onPageLoad()))
  }

  private def persistAndRedirectToPayments(amendment: Amendment, pid: String, originalDeclaration: Declaration)(
    implicit request: Request[_],
    hc: HeaderCarrier): Future[Result] =
    calculationService.paymentCalculations(amendment.goods.goods, originalDeclaration.goodsDestination).flatMap { calculationResponse =>
      val amendmentRef = originalDeclaration.amendments.size + 1
      val updatedAmendment =
        amendment.copy(reference = amendmentRef, maybeTotalCalculationResult = Some(calculationResponse.results.totalCalculationResult))

      val updatedDeclaration = originalDeclaration.copy(amendments = originalDeclaration.amendments :+ updatedAmendment)

      for {
        _        <- calculationService.amendDeclaration(updatedDeclaration)
        redirect <- redirectToPaymentsIfNecessary(calculationResponse.results, originalDeclaration, pid, amendmentRef)
      } yield redirect
    }

  def redirectToPaymentsIfNecessary(calculations: CalculationResults, declaration: Declaration, pid: String, amendmentRef: Int)(
    implicit rh: RequestHeader,
    hc: HeaderCarrier): Future[Result] =
    if (calculations.totalTaxDue.value == 0L) {
      Future.successful(Redirect(routes.DeclarationConfirmationController.onPageLoad()))
    } else {
      tpsPaymentsService
        .createTpsPayments(pid, Some(amendmentRef), declaration, calculations)
        .map(
          tpsId =>
            Redirect(s"${appConfig.tpsFrontendBaseUrl}/tps-payments/make-payment/mib/${tpsId.value}")
              .addingToSession("TPS_ID" -> tpsId.value))
    }
}
