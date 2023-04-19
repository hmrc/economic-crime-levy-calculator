/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevycalculator

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevycalculator.base.ISpecBase
import uk.gov.hmrc.economiccrimelevycalculator.controllers.routes
import uk.gov.hmrc.economiccrimelevycalculator.models.Band._
import uk.gov.hmrc.economiccrimelevycalculator.models.{BandRange, Bands, CalculateLiabilityRequest, CalculatedLiability, EclAmount}
import uk.gov.hmrc.economiccrimelevycalculator.utils.ApportionmentUtils

class CalculateLiabilityISpec extends ISpecBase {

  s"POST ${routes.CalculateLiabilityController.calculateLiability.url}" should {
    "return the calculated liability JSON based on the relevant AP length, AML regulated activity length and UK revenue" in {
      stubAuthorised()

      val mediumRevenue = 10200000

      lazy val result = callRoute(
        FakeRequest(routes.CalculateLiabilityController.calculateLiability).withJsonBody(
          Json.toJson(
            CalculateLiabilityRequest(
              ApportionmentUtils.yearInDays,
              ApportionmentUtils.yearInDays,
              mediumRevenue
            )
          )
        )
      )

      val expectedAmountDue = 10000

      val expectedBands = Bands(
        small = BandRange(0, 10200000, 0),
        medium = BandRange(10200000, 36000000, 10000),
        large = BandRange(36000000, 1000000000, 36000),
        veryLarge = BandRange(1000000000, Long.MaxValue, 250000)
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(
        CalculatedLiability(
          amountDue = EclAmount(amount = expectedAmountDue),
          bands = expectedBands,
          calculatedBand = Medium
        )
      )
    }
  }

}
