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
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms.ImportExportChoiceForm._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ImportExportChoice

@Singleton
class ImportExportChoiceController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                             view: ImportExportChoice
                                            )(implicit appConf: AppConfig)
  extends FrontendBaseController {

  val onPageLoad = Action { implicit request =>
    Ok(view(form, routes.ImportExportChoiceController.onPageLoad))
  }

  val onSubmit = Action { implicit request =>
    Ok("xxx")
  }
}