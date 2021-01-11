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

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.MockStrideAuth.givenTheUserIsAuthenticatedAndAuthorised
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.ImportExportChoice

import scala.concurrent.ExecutionContext.Implicits.global

class ImportExportChoiceControllerSpec extends DeclarationJourneyControllerSpec {

  val view = injector.instanceOf[ImportExportChoice]
  val controller = new ImportExportChoiceController(component, view, actionProvider, repo)

  "onPageLoad" should {
    "return 200 with radio button" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ImportExportChoiceController.onPageLoad.url)

      val eventualResult = controller.onPageLoad(request)
      val result = contentAsString(eventualResult)
      status(eventualResult) mustBe 200
      result must include(messageApi("declarationType.header"))
      result must include(messageApi("declarationType.title"))
      result must include(messageApi("declarationType.Import"))
      result must include(messageApi("declarationType.Export"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> Export.toString)

      val eventualResult = controller.onSubmit(request)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe Some(routes.GoodsDestinationController.onPageLoad().url)
    }

    "return 400 with required form error" in {
      givenTheUserIsAuthenticatedAndAuthorised()
      val request = buildGet(routes.ImportExportChoiceController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "")

      val eventualResult = controller.onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("declarationType.header"))
      result must include(messageApi("declarationType.title"))
      result must include(messageApi("declarationType.error.required"))
    }
  }
}
