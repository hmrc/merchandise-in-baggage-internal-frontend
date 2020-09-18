/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers

import com.github.tomakehurst.wiremock.client.WireMock.{status => _}
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.controllers.Forms._
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.api.{DeclarationIdResponse, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{Declaration, DeclarationId, DeclarationNotFound}
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.service.MIBBackendService
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.support.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.views.html.{DeclarationFoundTestOnlyPage, DeclarationTestOnlyPage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DeclarationTestOnlyControllerSpec extends BaseSpecWithApplication with CoreTestData {

  private val view = injector.instanceOf[DeclarationTestOnlyPage]
  private val foundView = injector.instanceOf[DeclarationFoundTestOnlyPage]
  private val httpClient = injector.instanceOf[HttpClient]

  "ready html page is served which contains copy showing it is a test-only page and a form with which I an enter and submit a declaration" in {
    val request = declarationRequestGET()
    val controller = new DeclarationTestOnlyController(component, httpClient, view, foundView)
    val result = controller.declarations()(request)

    status(result) mustBe 200
    contentAsString(result) mustBe view(controller.declarationForm(declarationFormIdentifier))(request).toString
  }

  "on submit a declaration will be persisted and redirected to /test-only/declaration/:id" in new MIBBackendService {
    setUp { (declaration, _, declarationIdResponse, requestBody) =>
      val postRequest = buildPost(routes.DeclarationTestOnlyController.onSubmit().url)
      val controller = stubController(Future.successful(declaration), requestBody, declarationIdResponse)
      val result = controller.onSubmit()(postRequest)

      status(result) mustBe 303
      redirectLocation(result).get mustBe s"${routes.DeclarationTestOnlyController.findDeclaration(DeclarationId("123")).url}"
    }
  }

  "on findDeclaration a declaration will be retrieved from MIB backend and show data result" in new MIBBackendService {
    setUp { (stubbedDeclaration, _, idResponse, _) =>
      val getRequest = findDeclarationRequestGET(idResponse.id)
      val controller = stubController(Future.successful(stubbedDeclaration), Json.obj(), idResponse)
      val result = controller.findDeclaration(idResponse.id)(getRequest)

      status(result) mustBe 200
      contentAsString(result) mustBe foundView(stubbedDeclaration)(getRequest).toString
    }
  }

  "I navigate or am redirected to /test-only/declarations/123. Then: A 'not found' message is served." in new MIBBackendService {
    setUp { (_, _, idResponse, _) =>
      val getRequest = findDeclarationRequestGET(idResponse.id)
      val controller = stubController(Future.failed(DeclarationNotFound), Json.obj(), idResponse)
      val result = controller.findDeclaration(idResponse.id)(getRequest)

      status(result) mustBe 404
      contentAsString(result) must include("Declaration Not Found")
    }
  }


  private def setUp(fn: (Declaration, DeclarationRequest, DeclarationIdResponse, JsValue) => Any): Any = {
    val declarationRequest = aDeclarationRequest
    val declarationIdResponse: DeclarationIdResponse = DeclarationIdResponse(DeclarationId("123"))
    val stubbedDeclaration: Declaration = aDeclaration
    val requestBody = Json.toJson(declarationRequest)

    fn(stubbedDeclaration, declarationRequest, declarationIdResponse, requestBody)
  }

  private def stubController(stub: Future[Declaration], requestBody: JsValue,
                             declarationIdResponse: DeclarationIdResponse): DeclarationTestOnlyController =
    new DeclarationTestOnlyController(component, httpClient, view, foundView) {
      override def declarationById(httpClient: HttpClient, declarationId: DeclarationId)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] = stub

      override protected def bindForm(implicit request: Request[_]): Form[DeclarationData] =
        new Forms {}.declarationForm(declarationFormIdentifier)
          .bind(Map(declarationFormIdentifier -> requestBody.toString))

      override def addDeclaration(httpClient: HttpClient, requestBody: DeclarationRequest)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationIdResponse] =
        Future.successful(declarationIdResponse)
    }
}
