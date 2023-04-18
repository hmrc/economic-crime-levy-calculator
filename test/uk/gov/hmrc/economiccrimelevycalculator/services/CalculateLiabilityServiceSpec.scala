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

package uk.gov.hmrc.economiccrimelevycalculator.services

import org.scalacheck.Gen
import uk.gov.hmrc.economiccrimelevycalculator.base.SpecBase
import uk.gov.hmrc.economiccrimelevycalculator.config.AppConfig
import uk.gov.hmrc.economiccrimelevycalculator.models.Band._
import uk.gov.hmrc.economiccrimelevycalculator.models._
import uk.gov.hmrc.economiccrimelevycalculator.utils.ApportionmentUtils.yearInDays

class CalculateLiabilityServiceSpec extends SpecBase {

  case class ExpectedBands(
    expectedSmallTo: Long,
    expectedMediumTo: Long,
    expectedLargeTo: Long,
    expectedMediumAmount: BigDecimal,
    expectedLargeAmount: BigDecimal,
    expectedVeryLargeAmount: BigDecimal,
    generatedRevenue: Long,
    expectedBand: Band,
    apportioned: Boolean
  )

  object ExpectedBands {
    def apply(
      smallTo: Long,
      mediumTo: Long,
      largeTo: Long,
      expectedMediumAmount: BigDecimal,
      expectedLargeAmount: BigDecimal,
      expectedVeryLargeAmount: BigDecimal,
      band: Band,
      apportioned: Boolean = false
    )(implicit
      appConfig: AppConfig
    ): ExpectedBands = {
      val revenue = band match {
        case Small     => Gen.chooseNum[Long](appConfig.defaultBands.small.from, smallTo - 1).sample.get
        case Medium    => Gen.chooseNum[Long](smallTo, mediumTo - 1).sample.get
        case Large     => Gen.chooseNum[Long](mediumTo, largeTo - 1).sample.get
        case VeryLarge => Gen.chooseNum[Long](largeTo, appConfig.defaultBands.veryLarge.to).sample.get
      }

      ExpectedBands(
        smallTo,
        mediumTo,
        largeTo,
        expectedMediumAmount,
        expectedLargeAmount,
        expectedVeryLargeAmount,
        revenue,
        band,
        apportioned
      )
    }
  }

  val service = new CalculateLiabilityService(appConfig)

  implicit val config: AppConfig = appConfig

  val sTo: Long            = appConfig.defaultBands.small.to
  val mTo: Long            = appConfig.defaultBands.medium.to
  val lTo: Long            = appConfig.defaultBands.large.to
  val sAmount: BigDecimal  = BigDecimal(appConfig.defaultSmallAmount)
  val mAmount: BigDecimal  = BigDecimal(appConfig.defaultMediumAmount)
  val lAmount: BigDecimal  = BigDecimal(appConfig.defaultLargeAmount)
  val vlAmount: BigDecimal = BigDecimal(appConfig.defaultVeryLargeAmount)

  "calculateLiability" should {
    "return the correctly calculated liability based on both the length of the relevant AP and AML regulated activity" in forAll(
      Table(
        (
          "relevantApLength",
          "amlRegulatedActivityLength",
          "expectedBands",
          "expectedAmountDue"
        ),
        (yearInDays, yearInDays, ExpectedBands(sTo, mTo, lTo, mAmount, lAmount, vlAmount, Small), EclAmount(sAmount)),
        (yearInDays, yearInDays, ExpectedBands(sTo, mTo, lTo, mAmount, lAmount, vlAmount, Medium), EclAmount(mAmount)),
        (yearInDays, yearInDays, ExpectedBands(sTo, mTo, lTo, mAmount, lAmount, vlAmount, Large), EclAmount(lAmount)),
        (
          yearInDays,
          yearInDays,
          ExpectedBands(sTo, mTo, lTo, mAmount, lAmount, vlAmount, VeryLarge),
          EclAmount(vlAmount)
        ),
        (
          245,
          yearInDays,
          ExpectedBands(6846576L, 24164384L, 671232877L, mAmount, lAmount, vlAmount, Small, apportioned = true),
          EclAmount(sAmount)
        ),
        (
          182,
          yearInDays,
          ExpectedBands(5086028L, 17950685L, 498630137L, mAmount, lAmount, vlAmount, Medium, apportioned = true),
          EclAmount(mAmount)
        ),
        (
          73,
          yearInDays,
          ExpectedBands(2040000L, 7200000L, 200000000L, mAmount, lAmount, vlAmount, Large, apportioned = true),
          EclAmount(lAmount)
        ),
        (
          450,
          yearInDays,
          ExpectedBands(12575343L, 44383562L, 1232876713L, mAmount, lAmount, vlAmount, VeryLarge, apportioned = true),
          EclAmount(vlAmount)
        ),
        (
          yearInDays,
          120,
          ExpectedBands(sTo, mTo, lTo, BigDecimal(3287.67), BigDecimal(11835.61), BigDecimal(82191.78), Small),
          EclAmount(sAmount)
        ),
        (
          yearInDays,
          60,
          ExpectedBands(sTo, mTo, lTo, BigDecimal(1643.83), BigDecimal(5917.80), BigDecimal(41095.89), Medium),
          EclAmount(BigDecimal(1643.83), apportioned = true)
        ),
        (
          yearInDays,
          204,
          ExpectedBands(sTo, mTo, lTo, BigDecimal(5589.04), BigDecimal(20120.54), BigDecimal(139726.02), Large),
          EclAmount(BigDecimal(20120.54), apportioned = true)
        ),
        (
          yearInDays,
          330,
          ExpectedBands(sTo, mTo, lTo, BigDecimal(9041.09), BigDecimal(32547.94), BigDecimal(226027.39), VeryLarge),
          EclAmount(BigDecimal(226027.39), apportioned = true)
        ),
        (
          314,
          92,
          ExpectedBands(
            8774795L,
            30969864L,
            860273973L,
            BigDecimal(2520.54),
            BigDecimal(9073.97),
            BigDecimal(63013.69),
            Small,
            apportioned = true
          ),
          EclAmount(sAmount)
        ),
        (
          113,
          198,
          ExpectedBands(
            3157809L,
            11145206L,
            309589042L,
            BigDecimal(5424.65),
            BigDecimal(19528.76),
            BigDecimal(135616.43),
            Medium,
            apportioned = true
          ),
          EclAmount(BigDecimal(5424.65), apportioned = true)
        ),
        (
          284,
          300,
          ExpectedBands(
            7936439L,
            28010959L,
            778082192L,
            BigDecimal(8219.17),
            BigDecimal(29589.04),
            BigDecimal(205479.45),
            Large,
            apportioned = true
          ),
          EclAmount(BigDecimal(29589.04), apportioned = true)
        ),
        (
          91,
          256,
          ExpectedBands(
            2543014L,
            8975343L,
            249315069L,
            BigDecimal(7013.69),
            BigDecimal(25249.31),
            BigDecimal(175342.46),
            VeryLarge,
            apportioned = true
          ),
          EclAmount(BigDecimal(175342.46), apportioned = true)
        )
      )
    ) {
      (
        relevantApLength: Int,
        amlRegulatedActivityLength: Int,
        expectedBands: ExpectedBands,
        expectedAmountDue: EclAmount
      ) =>
        val result = service.calculateLiability(
          CalculateLiabilityRequest(
            amlRegulatedActivityLength = amlRegulatedActivityLength,
            relevantApLength = relevantApLength,
            ukRevenue = expectedBands.generatedRevenue
          )
        )

        val expectedSmallBand: BandRange     =
          BandRange(
            from = appConfig.defaultBands.small.from,
            to = expectedBands.expectedSmallTo,
            amount = sAmount
          )
        val expectedMediumBand: BandRange    =
          BandRange(
            from = expectedBands.expectedSmallTo,
            to = expectedBands.expectedMediumTo,
            amount = expectedBands.expectedMediumAmount
          )
        val expectedLargeBand: BandRange     =
          BandRange(
            from = expectedBands.expectedMediumTo,
            to = expectedBands.expectedLargeTo,
            amount = expectedBands.expectedLargeAmount
          )
        val expectedVeryLargeBand: BandRange =
          BandRange(
            from = expectedBands.expectedLargeTo,
            to = appConfig.defaultBands.veryLarge.to,
            amount = expectedBands.expectedVeryLargeAmount
          )

        result shouldBe CalculatedLiability(
          amountDue = expectedAmountDue,
          bands = Bands(
            small = expectedSmallBand,
            medium = expectedMediumBand,
            large = expectedLargeBand,
            veryLarge = expectedVeryLargeBand,
            apportioned = expectedBands.apportioned
          ),
          calculatedBand = expectedBands.expectedBand
        )
    }
  }

}
