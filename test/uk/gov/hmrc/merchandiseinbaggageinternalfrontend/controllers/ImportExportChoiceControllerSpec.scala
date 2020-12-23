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

import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ImportExportChoice
import scala.concurrent.ExecutionContext.Implicits.global

class ImportExportChoiceControllerSpec extends BaseSpecWithApplication {

  val view = app.injector.instanceOf[ImportExportChoice]
  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val controller = new ImportExportChoiceController(component, view, repo)

  "return 200 with radio button" in {
    val request = FakeRequest(GET, routes.ImportExportChoiceController.onPageLoad.url).withSession((SessionKeys.sessionId, "123"))
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

    val eventualResult = controller.onPageLoad(request)
    status(eventualResult) mustBe 200
    contentAsString(eventualResult) must include(messageApi("importExportChoice.header"))
    contentAsString(eventualResult) must include(messageApi("importExportChoice.MakeExport"))
    contentAsString(eventualResult) must include(messageApi("importExportChoice.MakeImport"))
    contentAsString(eventualResult) must include(messageApi("site.continue"))
  }
}
