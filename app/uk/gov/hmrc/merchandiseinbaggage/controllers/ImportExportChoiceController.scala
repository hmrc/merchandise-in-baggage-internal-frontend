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

import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.ImportExportChoiceForm._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.core.ImportExportChoices.{AddToExisting, MakeExport, MakeImport}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.ImportExportChoice

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImportExportChoiceController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  view: ImportExportChoice,
  actionProvider: DeclarationJourneyActionProvider,
  val repo: DeclarationJourneyRepository)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyUpdateController {

  val onPageLoad = actionProvider.initJourneyAction { implicit request =>
    Ok(view(form))
  }

  val onSubmit = actionProvider.initJourneyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future successful BadRequest(view(formWithErrors)),
        choice => {
          val (declarationType, journeyType) = choice match {
            case MakeImport    => (Import, New)
            case MakeExport    => (Export, New)
            case AddToExisting => (Import, Amend) //defaults to Import, will be set correctly in the next page
          }

          repo
            .upsert(DeclarationJourney(SessionId(request.session(SessionKeys.sessionId)), declarationType, journeyType))
            .map { _ =>
              journeyType match {
                case New   => Redirect(routes.GoodsDestinationController.onPageLoad()).addingToSession("journeyType"    -> "new")
                case Amend => Redirect(routes.RetrieveDeclarationController.onPageLoad()).addingToSession("journeyType" -> "amend")
              }
            }
        }
      )
  }
}
