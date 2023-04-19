/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevycalculator.controllers

import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.economiccrimelevycalculator.base.SpecBase
import uk.gov.hmrc.economiccrimelevycalculator.models.{CalculateLiabilityRequest, CalculatedLiability, EclAmount}
import uk.gov.hmrc.economiccrimelevycalculator.services.CalculateLiabilityService
import uk.gov.hmrc.economiccrimelevycalculator.generators.CachedArbitraries._

import scala.concurrent.Future

class CalculateLiabilityControllerSpec extends SpecBase {

  val mockCalculateLiabilityService: CalculateLiabilityService = mock[CalculateLiabilityService]

  val controller = new CalculateLiabilityController(
    cc,
    fakeAuthorisedAction,
    mockCalculateLiabilityService
  )

  "calculateLiability" should {
    "return 200 OK the calculated liability JSON" in forAll {
      (
        calculateLiabilityRequest: CalculateLiabilityRequest,
        calculatedLiability: CalculatedLiability,
        amountDue: BigDecimal
      ) =>
        val updatedWithValidAmountDue = calculatedLiability.copy(amountDue = EclAmount(amount = amountDue))

        when(mockCalculateLiabilityService.calculateLiability(any())).thenReturn(updatedWithValidAmountDue)

        val result: Future[Result] =
          controller.calculateLiability()(
            fakeRequestWithJsonBody(Json.toJson(calculateLiabilityRequest))
          )

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(updatedWithValidAmountDue)
    }
  }

}
