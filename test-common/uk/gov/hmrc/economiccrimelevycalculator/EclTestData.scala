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

package uk.gov.hmrc.economiccrimelevycalculator

import org.scalacheck.{Arbitrary, Gen}

import scala.math.BigDecimal.RoundingMode

trait EclTestData {

  private val minAmountDue = 0
  private val maxAmountDue = 250000

  implicit val arbValidAmountDue: Arbitrary[BigDecimal] = Arbitrary {
    Gen.chooseNum[Double](minAmountDue, maxAmountDue).map(BigDecimal.apply(_).setScale(2, RoundingMode.DOWN))
  }

  def alphaNumericString: String = Gen.alphaNumStr.retryUntil(_.nonEmpty).sample.get

  val testInternalId: String = alphaNumericString

}
