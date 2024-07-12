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

import com.typesafe.config.Config
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import uk.gov.hmrc.economiccrimelevycalculator.models.{BandRange, Bands}

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (configuration: Config) {

  val appName: String = configuration.getString("appName")

  def bandRangeLoader(path: String, year: Int): BandRange = {
    val bandRangeList = configuration
      .getConfigList(path)
      .toList
      .map { config =>
        (
          config.getIntList("years"),
          BandRange(
            config.getLong("from"),
            config.getLong("to"),
            config.getLong("amount")
          )
        )
      }

    val latestExistingYear = bandRangeList.last._1.last

    bandRangeList.find(_._1.contains(year)) match {
      case Some(bandRange)                   => bandRange._2
      case None if year > latestExistingYear => bandRangeList.last._2
      case _                                 => throw new IllegalArgumentException(s"The provided tax year $year is not supported")
    }
  }

  def defaultBands(year: Int): Bands = {
    val small     = bandRangeLoader("bands.small", year)
    val medium    = bandRangeLoader("bands.medium", year)
    val large     = bandRangeLoader("bands.large", year)
    val veryLarge = bandRangeLoader("bands.veryLarge", year)

    Bands(
      small = small,
      medium = medium,
      large = large,
      veryLarge = veryLarge
    )
  }
}
