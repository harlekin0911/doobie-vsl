package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import de.ways42.vsl.connection.Connect


class TestMandateService  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect( "VSMADM", "together")
  lazy val ms = MandateService(xa )
  
  test( "MS-getMandateWithPayments") {
    assert( ms.getMandateWithPayments( 22317).transact(xa).unsafeRunSync()._2.size == 1)
 	}
    
  test( "MS-NichtTerminierteAbgelaufeneMandateMitLetztemPayment") {
	  assert(  ms.getNichtTerminierteAbgelaufeneMandateMitLetztemPayment().size >= 12830)
  }
}
