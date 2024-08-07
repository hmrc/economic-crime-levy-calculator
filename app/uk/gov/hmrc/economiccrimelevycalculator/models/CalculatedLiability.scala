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

package uk.gov.hmrc.economiccrimelevycalculator.models

import play.api.libs.json._
import uk.gov.hmrc.economiccrimelevycalculator.utils.ApportionmentUtils

import scala.math.BigDecimal.RoundingMode

sealed trait Band

object Band {
  case object Small extends Band
  case object Medium extends Band
  case object Large extends Band
  case object VeryLarge extends Band

  implicit val format: Format[Band] = new Format[Band] {
    override def reads(json: JsValue): JsResult[Band] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "Small"     => JsSuccess(Small)
          case "Medium"    => JsSuccess(Medium)
          case "Large"     => JsSuccess(Large)
          case "VeryLarge" => JsSuccess(VeryLarge)
          case s           => JsError(s"$s is not a valid Band")
        }
      case e: JsError          => e
    }

    override def writes(o: Band): JsValue = JsString(o.toString)
  }
}

final case class EclAmount(amount: BigDecimal, apportioned: Boolean = false)

object EclAmount {
  implicit val format: OFormat[EclAmount] = Json.format[EclAmount]
}

final case class BandRange(from: Long, to: Long, amount: BigDecimal) {
  def apportion(relevantApLength: Int, amlRegulatedActivityLength: Int): BandRange = {
    val apportionBandRange: BigDecimal => BigDecimal = ApportionmentUtils.apportionBasedOnDays(
      _,
      days = relevantApLength,
      scale = 0,
      roundingMode = RoundingMode.UP
    )

    val apportionBandAmount: BigDecimal => BigDecimal = ApportionmentUtils.apportionBasedOnDays(
      _,
      days = amlRegulatedActivityLength,
      scale = 2,
      roundingMode = RoundingMode.DOWN
    )

    BandRange(
      from = apportionBandRange(from).longValue,
      to = apportionBandRange(to).longValue,
      amount = apportionBandAmount(amount)
    )
  }

  def apportioned(defaultBandRange: BandRange): Boolean = from != defaultBandRange.from | to != defaultBandRange.to
}

object BandRange {
  implicit val format: OFormat[BandRange] = Json.format[BandRange]
}

final case class Bands(
  small: BandRange,
  medium: BandRange,
  large: BandRange,
  veryLarge: BandRange,
  apportioned: Boolean = false
)

object Bands {
  implicit val format: OFormat[Bands] = Json.format[Bands]
}

final case class CalculatedLiability(amountDue: EclAmount, bands: Bands, calculatedBand: Band)

object CalculatedLiability {
  implicit val format: OFormat[CalculatedLiability] = Json.format[CalculatedLiability]
}
