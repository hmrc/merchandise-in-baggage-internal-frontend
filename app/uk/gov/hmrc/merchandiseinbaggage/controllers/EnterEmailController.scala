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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.EnterEmailForm.form
import uk.gov.hmrc.merchandiseinbaggage.navigation.EnterEmailRequest
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.EnterEmailView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnterEmailController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  navigator: Navigator,
  view: EnterEmailView
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(routes.TravellerDetailsController.onPageLoad())

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    val preparedForm = form.fill(request.declarationJourney.maybeEmailAddress)

    Ok(view(preparedForm, request.declarationType, backButtonUrl))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.declarationType, backButtonUrl))),
        email => {
          val updated = request.declarationJourney.copy(maybeEmailAddress = email)
          navigator.nextPage(EnterEmailRequest(updated, repo.upsert, updated.declarationRequiredAndComplete))
        }.map(Redirect)
      )
  }
}
