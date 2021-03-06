/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.testuser.connectors

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.testuser.helpers.GeneratorProvider
import uk.gov.hmrc.testuser.helpers.stubs.DesSimulatorStub
import uk.gov.hmrc.testuser.models.ServiceKeys._
import uk.gov.hmrc.testuser.repository.TestUserRepository

import scala.concurrent.Future

class DesSimulatorConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with WithFakeApplication {

  trait Setup extends GeneratorProvider {
    val repository = mock[TestUserRepository]
    when(repository.identifierIsUnique(any[String])).thenReturn(Future(true))

    val testIndividual = await(generator.generateTestIndividual(Seq(MTD_INCOME_TAX, SELF_ASSESSMENT, NATIONAL_INSURANCE)))
    val testOrganisation = await(generator.generateTestOrganisation(Seq(MTD_INCOME_TAX, SELF_ASSESSMENT, NATIONAL_INSURANCE, CORPORATION_TAX)))

    implicit val hc = HeaderCarrier()

    val underTest = new DesSimulatorConnector(
      fakeApplication.injector.instanceOf[HttpClient],
      fakeApplication.injector.instanceOf[Configuration],
      fakeApplication.injector.instanceOf[Environment],
      fakeApplication.injector.instanceOf[ServicesConfig]
    ) {
      override lazy val serviceUrl: String = DesSimulatorStub.url
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    DesSimulatorStub.server.start()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    DesSimulatorStub.server.resetMappings()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    DesSimulatorStub.server.stop()
  }

  "createIndividual" should {
    "create a test individual" in new Setup {
      DesSimulatorStub.willSuccessfullyCreateTestIndividual()

      val result = await(underTest.createIndividual(testIndividual))
      result shouldBe testIndividual
    }

    "fail when the DesSimulator returns an error" in new Setup {
      DesSimulatorStub.willFailWhenCreatingTestIndividual()

      intercept[Upstream5xxResponse] {
        await(underTest.createIndividual(testIndividual))
      }
    }
  }

  "createOrganisation" should {
    "create a test organisation" in new Setup {
      DesSimulatorStub.willSuccessfullyCreateTestOrganisation()

      val result = await(underTest.createOrganisation(testOrganisation))
      result shouldBe testOrganisation
    }

    "fail when the DesSimulator returns an error" in new Setup {
      DesSimulatorStub.willFailWhenCreatingTestOrganisation()

      intercept[Upstream5xxResponse] {
        await(underTest.createOrganisation(testOrganisation))
      }
    }
  }
}
