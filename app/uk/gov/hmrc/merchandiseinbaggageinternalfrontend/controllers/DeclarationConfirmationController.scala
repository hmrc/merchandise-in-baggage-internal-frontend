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

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.DeclarationConfirmationView

import scala.concurrent.{ExecutionContext, Future}

class DeclarationConfirmationController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  view: DeclarationConfirmationView,
  connector: MibConnector,
  val repo: DeclarationJourneyRepository,
)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    val declarationId = request.declarationJourney.declarationId
    connector.findDeclaration(declarationId).map {
      case Some(declaration) =>
        resetJourney()
        Ok(view(declaration))
      case None => actionProvider.invalidRequest(s"declaration not found for id:${declarationId.value}")
    }
  }

  private def resetJourney()(implicit request: DeclarationJourneyRequest[AnyContent]): Future[DeclarationJourney] = {
    import request.declarationJourney._
    repo.upsert(DeclarationJourney(sessionId, declarationType, declarationId = declarationId))
  }
}
