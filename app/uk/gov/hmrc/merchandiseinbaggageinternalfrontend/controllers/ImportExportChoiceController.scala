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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms.ImportExportChoiceForm._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{DeclarationJourney, SessionId}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ImportExportChoice

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
        importExport => {
          repo
            .upsert(DeclarationJourney(SessionId(request.session(SessionKeys.sessionId)), importExport))
            .map { _ =>
              Redirect(routes.GoodsDestinationController.onPageLoad())
            }
        }
      )
  }
}
