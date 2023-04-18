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

import uk.gov.hmrc.economiccrimelevycalculator.config.AppConfig
import uk.gov.hmrc.economiccrimelevycalculator.models.Band._
import uk.gov.hmrc.economiccrimelevycalculator.models.{Band, Bands, CalculateLiabilityRequest, CalculatedLiability, EclAmount}

import javax.inject.Inject

class CalculateLiabilityService @Inject() (appConfig: AppConfig) {

  private def calculateBand(relevantApLength: Int, ukRevenue: Long, amlRegulatedActivityLength: Int): (Bands, Band) = {
    val smallBand     = appConfig.defaultBands.small.apportion(relevantApLength, amlRegulatedActivityLength)
    val mediumBand    = appConfig.defaultBands.medium.apportion(relevantApLength, amlRegulatedActivityLength)
    val largeBand     = appConfig.defaultBands.large.apportion(relevantApLength, amlRegulatedActivityLength)
    val veryLargeBand =
      appConfig.defaultBands.veryLarge
        .apportion(relevantApLength, amlRegulatedActivityLength)
        .copy(to = appConfig.defaultBands.veryLarge.to)

    val bands: Bands = Bands(
      smallBand,
      mediumBand,
      largeBand,
      veryLargeBand,
      apportioned = smallBand.apportioned(appConfig.defaultBands.small) | mediumBand.apportioned(
        appConfig.defaultBands.medium
      ) | largeBand.apportioned(appConfig.defaultBands.large) | veryLargeBand.apportioned(
        appConfig.defaultBands.veryLarge
      )
    )

    val band = ukRevenue match {
      case n if n >= smallBand.from && n < smallBand.to   => Small
      case n if n >= mediumBand.from && n < mediumBand.to => Medium
      case n if n >= largeBand.from && n < largeBand.to   => Large
      case n if n >= veryLargeBand.from                   => VeryLarge
      case _                                              => throw new IllegalStateException(s"Revenue $ukRevenue does not fall into any of the bands")
    }

    (bands, band)
  }

  private def determineAmountDue(band: Band, bands: Bands): EclAmount =
    band match {
      case Small     => EclAmount(bands.small.amount, bands.small.amount != appConfig.defaultSmallAmount)
      case Medium    => EclAmount(bands.medium.amount, bands.medium.amount != appConfig.defaultMediumAmount)
      case Large     => EclAmount(bands.large.amount, bands.large.amount != appConfig.defaultLargeAmount)
      case VeryLarge => EclAmount(bands.veryLarge.amount, bands.veryLarge.amount != appConfig.defaultVeryLargeAmount)
    }

  def calculateLiability(calculateLiabilityRequest: CalculateLiabilityRequest): CalculatedLiability = {
    val (bands, band) = calculateBand(
      calculateLiabilityRequest.relevantApLength,
      calculateLiabilityRequest.ukRevenue,
      calculateLiabilityRequest.amlRegulatedActivityLength
    )

    val amountDue = determineAmountDue(band, bands)

    CalculatedLiability(amountDue, bands, band)
  }

}
