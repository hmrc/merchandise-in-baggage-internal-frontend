package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.Forms._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.DeclarationTestOnlyPage
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.BaseSpecWithApplication

import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationTestOnlyControllerSpec extends BaseSpecWithApplication with CoreTestData {

  val view = injector.instanceOf[DeclarationTestOnlyPage]
  val controller = new DeclarationTestOnlyController(component, view, repository)


  "ready html page is served which contains copy showing it is a test-only page and a form with which I an enter and submit a declaration" in {
    val request = buildGet(routes.DeclarationTestOnlyController.declarations().url)
    val result = controller.declarations()(request)

    status(result) mustBe 200
    contentAsString(result) mustBe view(controller.declarationForm(declarationFormIdentifier))(request).toString
  }

  "on submit a declaration will be persisted and redirected to /declaration/:id" in {
    val declarationRequest = aDeclarationRequest
    val requestBody = Json.toJson(declarationRequest)
    val postRequest = buildPost(routes.DeclarationTestOnlyController.onSubmit().url)
    val controller = new DeclarationTestOnlyController(component, view, repository) {
      override protected def bindForm(implicit request: Request[_]): Form[DeclarationData] =
        new Forms{}.declarationForm(declarationFormIdentifier)
          .bind(Map(declarationFormIdentifier -> Json.toJson(requestBody).toString))
    }
    val result = controller.onSubmit()(postRequest)

    status(result) mustBe 303
    redirectLocation(result).get must include("/merchandise-in-baggage/declarations/")
  }
}
