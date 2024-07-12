/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.economiccrimelevycalculator.models.{BandRange, Bands, CalculatedLiability, EclAmount}
import uk.gov.hmrc.economiccrimelevycalculator.utils.ApportionmentUtils

class CalculateLiabilityISpec extends ISpecBase {

  s"POST ${routes.CalculateLiabilityController.calculateLiability.url}" should {
    "return the calculated liability JSON based on the AP length, AML regulated activity length, UK revenue and tax year" when {

      "the revenue is small" in {
        stubAuthorised()

        val smallRevenue = 1
        val exampleYear  = 2022

        val requestJson = Json.obj(
          "amlRegulatedActivityLength" -> ApportionmentUtils.yearInDays,
          "relevantApLength"           -> ApportionmentUtils.yearInDays,
          "ukRevenue"                  -> smallRevenue,
          "year"                       -> exampleYear
        )

        lazy val result = callRoute(
          FakeRequest(routes.CalculateLiabilityController.calculateLiability).withJsonBody(requestJson)
        )

        val expectedAmountDue = 0

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
            calculatedBand = Small
          )
        )
      }

      "the revenue is medium" in {
        stubAuthorised()

        val mediumRevenue = 10200000
        val exampleYear   = 2022

        val requestJson = Json.obj(
          "amlRegulatedActivityLength" -> ApportionmentUtils.yearInDays,
          "relevantApLength"           -> ApportionmentUtils.yearInDays,
          "ukRevenue"                  -> mediumRevenue,
          "year"                       -> exampleYear
        )

        lazy val result = callRoute(
          FakeRequest(routes.CalculateLiabilityController.calculateLiability).withJsonBody(requestJson)
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

      "the revenue is large" in {
        stubAuthorised()

        val largeRevenue = 36000000
        val exampleYear  = 2022

        val requestJson = Json.obj(
          "amlRegulatedActivityLength" -> ApportionmentUtils.yearInDays,
          "relevantApLength"           -> ApportionmentUtils.yearInDays,
          "ukRevenue"                  -> largeRevenue,
          "year"                       -> exampleYear
        )

        lazy val result = callRoute(
          FakeRequest(routes.CalculateLiabilityController.calculateLiability).withJsonBody(requestJson)
        )

        val expectedAmountDue = 36000

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
            calculatedBand = Large
          )
        )
      }

      "the revenue is very large and the tax year is prior to 2024" in {
        stubAuthorised()

        val veryLargeRevenue = 1000000000
        val exampleYear      = 2022

        val requestJson = Json.obj(
          "amlRegulatedActivityLength" -> ApportionmentUtils.yearInDays,
          "relevantApLength"           -> ApportionmentUtils.yearInDays,
          "ukRevenue"                  -> veryLargeRevenue,
          "year"                       -> exampleYear
        )

        lazy val result = callRoute(
          FakeRequest(routes.CalculateLiabilityController.calculateLiability).withJsonBody(requestJson)
        )

        val expectedAmountDue = 250000

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
            calculatedBand = VeryLarge
          )
        )
      }

      "the revenue is very large and the tax year is 2024 or beyond" in {
        stubAuthorised()

        val veryLargeRevenue = 1000000000
        val exampleYear      = 2024

        val requestJson = Json.obj(
          "amlRegulatedActivityLength" -> ApportionmentUtils.yearInDays,
          "relevantApLength"           -> ApportionmentUtils.yearInDays,
          "ukRevenue"                  -> veryLargeRevenue,
          "year"                       -> exampleYear
        )

        lazy val result = callRoute(
          FakeRequest(routes.CalculateLiabilityController.calculateLiability).withJsonBody(requestJson)
        )

        val expectedAmountDue = 500000

        val expectedBands = Bands(
          small = BandRange(0, 10200000, 0),
          medium = BandRange(10200000, 36000000, 10000),
          large = BandRange(36000000, 1000000000, 36000),
          veryLarge = BandRange(1000000000, Long.MaxValue, 500000)
        )

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(
          CalculatedLiability(
            amountDue = EclAmount(amount = expectedAmountDue),
            bands = expectedBands,
            calculatedBand = VeryLarge
          )
        )
      }
    }
  }

}
