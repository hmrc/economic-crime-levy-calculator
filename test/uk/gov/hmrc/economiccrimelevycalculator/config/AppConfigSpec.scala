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

package uk.gov.hmrc.economiccrimelevycalculator.config

import com.typesafe.config.ConfigException
import uk.gov.hmrc.economiccrimelevycalculator.base.SpecBase
import uk.gov.hmrc.economiccrimelevycalculator.models.{BandRange, Bands}

class AppConfigSpec extends SpecBase {

  ".bandRangeLoader" should {

    "return the correct band range when a previous year is provided" in {
      appConfig.bandRangeLoader("bands.veryLarge", 2022) shouldBe BandRange(1000000000, Long.MaxValue, 250000)
    }

    "return the correct band range when a later year is provided" in {
      appConfig.bandRangeLoader("bands.veryLarge", 2024) shouldBe BandRange(1000000000, Long.MaxValue, 500000)
    }

    "return the latest band range when a year after the latest year is provided" in {
      appConfig.bandRangeLoader("bands.veryLarge", 2048) shouldBe BandRange(1000000000, Long.MaxValue, 500000)
    }

    "throw an exception when a year prior to the start of ECL is provided" in {
      intercept[IllegalArgumentException](appConfig.bandRangeLoader("bands.veryLarge", 2021))
    }

    "throw an exception when an invalid config path is provided" in {
      intercept[ConfigException](appConfig.bandRangeLoader("bands.veryMedium", 2022))
    }
  }

  ".defaultBands" should {

    "return the correct bands when a previous year is provided" in {
      appConfig.defaultBands(2022) shouldBe Bands(
        BandRange(0, 10200000, 0),
        BandRange(10200000, 36000000, 10000),
        BandRange(36000000, 1000000000, 36000),
        BandRange(1000000000, Long.MaxValue, 250000)
      )
    }

    "return the correct bands when a later year is provided" in {
      appConfig.defaultBands(2024) shouldBe Bands(
        BandRange(0, 10200000, 0),
        BandRange(10200000, 36000000, 10000),
        BandRange(36000000, 1000000000, 36000),
        BandRange(1000000000, Long.MaxValue, 500000)
      )
    }

    "return the latest bands when a year after the latest year is provided" in {
      appConfig.defaultBands(2048) shouldBe Bands(
        BandRange(0, 10200000, 0),
        BandRange(10200000, 36000000, 10000),
        BandRange(36000000, 1000000000, 36000),
        BandRange(1000000000, Long.MaxValue, 500000)
      )
    }

    "throw an exception when a year prior to the start of ECL is provided" in {
      intercept[IllegalArgumentException](appConfig.defaultBands(2021))
    }
  }
}
