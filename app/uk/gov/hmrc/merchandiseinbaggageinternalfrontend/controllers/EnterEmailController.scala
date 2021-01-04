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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms.EnterEmailForm.form
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.EnterEmailView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnterEmailController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: EnterEmailView
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    backToCheckYourAnswersIfCompleteElse(routes.TravellerDetailsController.onPageLoad())

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    val preparedForm = request.declarationJourney.maybeEmailAddress.fold(form)(form.fill)

    Ok(view(preparedForm, request.declarationType, backButtonUrl))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.declarationType, backButtonUrl))),
        email =>
          persistAndRedirect(request.declarationJourney.copy(maybeEmailAddress = Some(email)), routes.JourneyDetailsController.onPageLoad())
      )
  }
}
