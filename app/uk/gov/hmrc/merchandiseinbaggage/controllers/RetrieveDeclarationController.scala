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

import cats.implicits._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.{AmendDeclarationConfiguration, AppConfig}
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.forms.RetrieveDeclarationForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, NotRequired, Paid}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{ExportGoodsEntry, GoodsEntries, ImportGoodsEntry, RetrieveDeclaration}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.utils.Utils.FutureOps
import uk.gov.hmrc.merchandiseinbaggage.views.html.RetrieveDeclarationView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveDeclarationController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  mibConnector: MibConnector,
  view: RetrieveDeclarationView
)(implicit appConfig: AppConfig, val ec: ExecutionContext)
    extends DeclarationJourneyUpdateController with AmendDeclarationConfiguration {

  override val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    if (amendFlagConf.canBeAmended) {
      Ok(view(form, routes.ImportExportChoiceController.onPageLoad(), request.declarationJourney.declarationType))
    } else Redirect(routes.CannotUseServiceController.onPageLoad().url)
  }

  override val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, routes.ImportExportChoiceController.onPageLoad(), request.declarationJourney.declarationType)).asFuture,
        validData => processRequest(validData)
      )
  }

  private def processRequest(
    validData: RetrieveDeclaration)(implicit request: DeclarationJourneyRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext) =
    mibConnector
      .findBy(validData.mibReference, validData.eori)
      .fold(
        error => Future successful InternalServerError(error), {
          case Some(declaration) if isValid(declaration) =>
            val goodsEntries = declaration.declarationType match {
              case Import => GoodsEntries(ImportGoodsEntry())
              case Export => GoodsEntries(ExportGoodsEntry())
            }
            repo.upsert(
              request.declarationJourney
                .copy(
                  declarationId = declaration.declarationId,
                  declarationType = declaration.declarationType,
                  goodsEntries = goodsEntries)) map { _ =>
              Redirect(routes.PreviousDeclarationDetailsController.onPageLoad())
            }
          case _ => Future successful Redirect(routes.DeclarationNotFoundController.onPageLoad())
        }
      )
      .flatten

  private def isValid(declaration: Declaration) =
    declaration.declarationType match {
      case Export => true
      case Import => declaration.paymentStatus.contains(Paid) || declaration.paymentStatus.contains(NotRequired)
    }
}
