/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.util

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}
import play.api.mvc.{PathBindable, QueryStringBindable}
import scala.reflect.runtime.universe.{TypeTag, typeOf}

object ValueClassBinder {

  def valueClassBinder[A: Reads](fromAtoString: A => String)(implicit stringBinder: PathBindable[String]): PathBindable[A] = {

    def parseString(str: String) =
      JsString(str).validate[A] match {
        case JsSuccess(a, _) => Right(a)
        case JsError(error) => Left(s"No valid value in path: $str. Error: $error")
      }

    new PathBindable[A] {
      override def bind(key: String, value: String): Either[String, A] =
        stringBinder.bind(key, value).right.flatMap(parseString)

      override def unbind(key: String, a: A): String =
        stringBinder.unbind(key, fromAtoString(a))
    }
  }

  def bindableA[A: TypeTag : Reads](fromAtoString: A => String): QueryStringBindable[A] = new QueryStringBindable.Parsing[A](
    parse = JsString(_).as[A],
    fromAtoString,
    {
      case (key: String, e: Exception) => s"Cannot parse param $key as ${typeOf[A].typeSymbol.name.toString}"
    }
  )
}
