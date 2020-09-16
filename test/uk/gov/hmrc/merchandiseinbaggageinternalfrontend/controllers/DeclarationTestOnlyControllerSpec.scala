/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import com.github.tomakehurst.wiremock.client.WireMock.{status => _, _}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.Forms._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.{DeclarationIdResponse, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{Declaration, DeclarationId, DeclarationNotFound}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service.MIBBackendService
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.{BaseSpecWithApplication, BaseSpecWithWireMock}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.{DeclarationFoundTestOnlyPage, DeclarationTestOnlyPage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DeclarationTestOnlyControllerSpec extends BaseSpecWithApplication with CoreTestData {

  private val view = injector.instanceOf[DeclarationTestOnlyPage]
  private val foundView = injector.instanceOf[DeclarationFoundTestOnlyPage]
  private val httpClient = injector.instanceOf[HttpClient]
  private val controller = new DeclarationTestOnlyController(component, httpClient, view, foundView)


  "ready html page is served which contains copy showing it is a test-only page and a form with which I an enter and submit a declaration" in {
    val request = buildGet(routes.DeclarationTestOnlyController.declarations().url)
    val result = controller.declarations()(request)

    status(result) mustBe 200
    contentAsString(result) mustBe view(controller.declarationForm(declarationFormIdentifier))(request).toString
  }

  "on submit a declaration will be persisted and redirected to /test-only/declaration/:id" in new MIBBackendService {
    val declarationRequest = aDeclarationRequest
    val requestBody = Json.toJson(declarationRequest)
    val postRequest = buildPost(routes.DeclarationTestOnlyController.onSubmit().url)
    val declarationIdResponse: DeclarationIdResponse = DeclarationIdResponse(DeclarationId("123"))

    val controller = new DeclarationTestOnlyController(component, httpClient, view, foundView) {
      override protected def bindForm(implicit request: Request[_]): Form[DeclarationData] =
        new Forms{}.declarationForm(declarationFormIdentifier)
          .bind(Map(declarationFormIdentifier -> Json.toJson(requestBody).toString))

      override  def addDeclaration(httpClient: HttpClient, requestBody: DeclarationRequest)
                                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationIdResponse] =
        Future.successful(declarationIdResponse)
    }
    val result = controller.onSubmit()(postRequest)

    status(result) mustBe 303
    redirectLocation(result).get mustBe s"${routes.DeclarationTestOnlyController.findDeclaration("123").url}"
  }

  "on findDeclaration a declaration will be retrieved from MIB backend and show data result" in new MIBBackendService {
    val getRequest = buildGet(routes.DeclarationTestOnlyController.findDeclaration("123").url)
    val stubbedDeclaration: Declaration = aDeclaration
    val controller = new DeclarationTestOnlyController(component, httpClient, view, foundView) {
      override  def declarationById(httpClient: HttpClient, declarationId: DeclarationId)
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
        Future.successful(stubbedDeclaration)
    }

    val result = controller.findDeclaration("123")(getRequest)

    status(result) mustBe 200
    contentAsString(result) mustBe foundView(stubbedDeclaration)(getRequest).toString
  }

  "I navigate or am redirected to /test-only/declarations/123. Then: A 'not found' message is served." in new MIBBackendService {
    val getRequest = buildGet(routes.DeclarationTestOnlyController.findDeclaration("123").url)
    val controller = new DeclarationTestOnlyController(component, httpClient, view, foundView) {
      override  def declarationById(httpClient: HttpClient, declarationId: DeclarationId)
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
        Future.failed(DeclarationNotFound)
    }

    val result = controller.findDeclaration("123")(getRequest)

    status(result) mustBe 404
    contentAsString(result) must include("Declaration Not Found")
  }
}
