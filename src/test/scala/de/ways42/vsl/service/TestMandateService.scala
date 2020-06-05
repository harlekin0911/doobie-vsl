package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import de.ways42.vsl.connection.Connect


class TestMandateService  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).unsafeRunSync()._2.size == 1)
 	}
    
  test( "MS-NichtTerminierteMandatet") {
    val m = MandateService.getNichtTerminierteMandateUndLetztesPayment().transact(xa).unsafeRunSync().size
    println ( "Anzahl Mandate mit aktiven Status: " + m) 
	  assert(  m == 246332)
  }
  test( "MS-NichtTerminierteMandateOhnemPayment") {
    val m = MandateService.getNichtTerminierteMandateOhnePayment().transact(xa).unsafeRunSync().size
    println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + m) 
	  assert(  m == 12912)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val m = MandateService.getNichtTerminierteMandateMitPayment().transact(xa).unsafeRunSync().size
    println ( "Anzahl Mandate mit aktiven Status und Payments: " + m) 
	  assert(  m == 233420)
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val m = MandateService.getNichtTerminierteAbgelaufeneMandate().transact(xa).unsafeRunSync().size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + m) 
	  assert(  m == 189498)
  }
  
}
