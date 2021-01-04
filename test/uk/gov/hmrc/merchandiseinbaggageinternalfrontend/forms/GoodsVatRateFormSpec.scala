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

package uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms.GoodsVatRateForm.form
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.forms.behaviours.OptionFieldBehaviours
import uk.gov.hmrc.merchandiseinbaggageinternalfrontend.model.core.{GoodsVatRate, GoodsVatRates}

class GoodsVatRateFormSpec extends OptionFieldBehaviours {

  ".value" must {

    val fieldName = "value"
    val requiredKey = "goodsVatRate.error.required"

    behave like optionsField[GoodsVatRate](
      form,
      fieldName,
      validValues = GoodsVatRates.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
