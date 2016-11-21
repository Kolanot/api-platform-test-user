/*
 * Copyright 2016 HM Revenue & Customs
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

package unit.uk.gov.hmrc.testuser.repository

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import uk.gov.hmrc.domain._
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.testuser.models._
import uk.gov.hmrc.testuser.repository.TestUserMongoRepository
import scala.concurrent.ExecutionContext.Implicits.global

class TestUserRepositorySpec extends UnitSpec with BeforeAndAfterEach with BeforeAndAfterAll with MongoSpecSupport {

  private val repository = new TestUserMongoRepository
  val testIndividual = TestIndividual("individualUser", "password", SaUtr("1555369052"), Nino("CC333333C"))
  val testOrganisation = TestOrganisation("organisationUser", "password", SaUtr("1555369052"), EmpRef("555","EIA000"),
    CtUtr("1555369053"), Vrn("999902541"))

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override protected def afterAll() {
    await(repository.drop)
  }

  "createUser" should {

    "create a test individual in the repository" in {

      val result = await(repository.createUser(testIndividual))

      result shouldBe testIndividual
      await(repository.findById(testIndividual._id)) shouldBe Some(testIndividual)
    }

    "create a test organisation in the repository" in {

      val result = await(repository.createUser(testOrganisation))

      result shouldBe testOrganisation
      await(repository.findById(testOrganisation._id)) shouldBe Some(testOrganisation)
    }
  }

  "fetchByUsername" should {

    "return an individual when the individual exists for the username" in {

      await(repository.createUser(testIndividual))

      val result = await(repository.fetchByUsername(testIndividual.username))

      result shouldBe Some(testIndividual)
    }

    "return an organisation when the organisation exists for the username" in {

      await(repository.createUser(testOrganisation))

      val result = await(repository.fetchByUsername(testOrganisation.username))

      result shouldBe Some(testOrganisation)
    }

    "return None when no user matches the username" in {

      val result = await(repository.fetchByUsername("unknown"))

      result shouldBe None
    }
  }
}