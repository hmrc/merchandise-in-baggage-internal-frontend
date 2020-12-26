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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms.PurchaseDetailsForm.form
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.PurchaseDetails
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service.CurrencyService
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.{PurchaseDetailsExportView, PurchaseDetailsImportView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaseDetailsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  importView: PurchaseDetailsImportView,
  exportView: PurchaseDetailsExportView,
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(index: Int)(implicit request: DeclarationGoodsRequest[_]) =
    checkYourAnswersOrReviewGoodsElse(routes.SearchGoodsCountryController.onPageLoad(index), index)

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      request.declarationJourney.declarationType match {
        case Import =>
          val preparedForm = request.goodsEntry.maybePurchaseDetails.fold(form)(p => form.fill(p.purchaseDetailsInput))

          Future successful Ok(importView(preparedForm, idx, category, backButtonUrl(idx)))
        case Export =>
          val preparedForm = request.goodsEntry.maybePurchaseDetails.fold(form)(p => form.fill(p.purchaseDetailsInput))

          Future successful Ok(exportView(preparedForm, idx, category, backButtonUrl(idx)))
      }
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async {
    implicit request: DeclarationGoodsRequest[AnyContent] =>
      withGoodsCategory(request.goodsEntry) { category =>
        request.declarationJourney.declarationType match {
          case Import =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future successful BadRequest(importView(formWithErrors, idx, category, backButtonUrl(idx))),
                purchaseDetailsInput =>
                  CurrencyService
                    .getCurrencyByCode(purchaseDetailsInput.currency)
                    .fold(actionProvider.invalidRequestF(s"currency [${purchaseDetailsInput.currency}] not found")) { currency =>
                      val updatedGoodsEntry =
                        request.goodsEntry.copy(maybePurchaseDetails = Some(PurchaseDetails(purchaseDetailsInput.price, currency)))

                      val updatedDeclarationJourney =
                        request.declarationJourney.copy(
                          goodsEntries = request.declarationJourney.goodsEntries.patch(idx, updatedGoodsEntry))

                      repo.upsert(updatedDeclarationJourney).map { _ =>
                        Redirect(routes.ReviewGoodsController.onPageLoad())
                      }
                  }
              )
          case Export =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future successful BadRequest(exportView(formWithErrors, idx, category, backButtonUrl(idx))),
                purchaseDetailsInput =>
                  CurrencyService
                    .getCurrencyByCode(purchaseDetailsInput.currency)
                    .fold(actionProvider.invalidRequestF(s"currency [${purchaseDetailsInput.currency}] not found")) { currency =>
                      persistAndRedirect(
                        request.goodsEntry.copy(maybePurchaseDetails = Some(PurchaseDetails(purchaseDetailsInput.price, currency))),
                        idx,
                        routes.ReviewGoodsController.onPageLoad()
                      )
                  }
              )
        }
      }
  }
}