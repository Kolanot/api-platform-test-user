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

package uk.gov.hmrc.testuser.services

import org.scalacheck.{Gen, Arbitrary}
import uk.gov.hmrc.domain._
import uk.gov.hmrc.testuser.models.{TestOrganisation, TestIndividual}


trait Generator {

  private val usernameGenerator = Gen.listOfN(12, Gen.numChar).map(_.mkString)
  private val passwordGenerator = Gen.listOfN(12, Gen.alphaNumChar).map(_.mkString)
  private val utrGenerator = new SaUtrGenerator()
  private val ninoGenerator = new uk.gov.hmrc.domain.Generator()
  private val employerReferenceGenerator: Gen[EmpRef] = for {
    taxOfficeNumber <- Gen.chooseNum(100, 999).map(x => x.toString)
    taxOfficeReference <- Gen.listOfN(10, Gen.alphaNumChar).map(_.mkString.toUpperCase)
  } yield EmpRef.fromIdentifiers(s"$taxOfficeNumber/$taxOfficeReference")
  private val vrnGenerator = Gen.chooseNum(666000000, 666999999)

  def generateTestIndividual() = TestIndividual(generateUsername, generatePassword, generateSaUtr, generateNino)

  def generateTestOrganisation() =
    TestOrganisation(generateUsername, generatePassword, generateSaUtr, generateEmpRef, generateCtUtr, generateVrn)

  private def generateUsername = usernameGenerator.sample.get
  private def generatePassword = passwordGenerator.sample.get
  private def generateEmpRef: EmpRef = employerReferenceGenerator.sample.get
  private def generateSaUtr: SaUtr = utrGenerator.nextSaUtr
  private def generateNino: Nino = ninoGenerator.nextNino
  private def generateCtUtr: CtUtr = CtUtr(utrGenerator.nextSaUtr.value)
  private def generateVrn: Vrn = Vrn(vrnGenerator.sample.get.toString)
}

object Generator extends Generator