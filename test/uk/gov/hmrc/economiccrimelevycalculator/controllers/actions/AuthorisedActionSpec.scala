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

package uk.gov.hmrc.economiccrimelevycalculator.controllers.actions

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import play.api.mvc.{BodyParsers, Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.economiccrimelevycalculator.base.SpecBase

import scala.concurrent.Future

class AuthorisedActionSpec extends SpecBase {

  val defaultBodyParser: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector       = mock[AuthConnector]

  val authorisedAction =
    new BaseAuthorisedAction(mockAuthConnector, defaultBodyParser)

  val testAction: Request[_] => Future[Result] = { _ =>
    Future(Ok("Test"))
  }

  val expectedRetrievals: Retrieval[Option[String]] =
    Retrievals.internalId

  "invokeBlock" should {
    "execute the block and return the result if authorised" in forAll { (internalId: String) =>
      when(mockAuthConnector.authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any()))
        .thenReturn(Future(Some(internalId)))

      val result: Future[Result] = authorisedAction.invokeBlock(fakeRequest, testAction)

      status(result)          shouldBe OK
      contentAsString(result) shouldBe "Test"
    }

    "return 401 unauthorized if there is an authorisation exception" in {
      List(
        InsufficientConfidenceLevel(),
        InsufficientEnrolments(),
        UnsupportedAffinityGroup(),
        UnsupportedCredentialRole(),
        UnsupportedAuthProvider(),
        IncorrectCredentialStrength(),
        InternalError(),
        BearerTokenExpired(),
        MissingBearerToken(),
        InvalidBearerToken(),
        SessionRecordNotFound()
      ).foreach { exception =>
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.failed(exception))

        val result: Future[Result] = authorisedAction.invokeBlock(fakeRequest, testAction)

        status(result) shouldBe UNAUTHORIZED
      }
    }

    "throw an IllegalStateException if there is no internal id" in {
      when(mockAuthConnector.authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any()))
        .thenReturn(Future(None))

      val result = intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }

      result.getMessage shouldBe "Unable to retrieve internalId"
    }
  }

}
