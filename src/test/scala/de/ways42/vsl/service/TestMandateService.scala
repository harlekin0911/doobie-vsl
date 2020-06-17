package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import de.ways42.vsl.connection.Connect
import de.ways42.vsl.tables.mandate.Payment
import de.ways42.vsl.tables.mandate.Mandate


class TestMandateService  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  val m = MandateService.getNichtTerminierteMandateUndLetztesPayment().transact(xa).unsafeRunSync()
  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).unsafeRunSync()._2.size == 1)
 	}
    
  test( "MS-NichtTerminierteMandatet") {
    val mm = m.size
    println ( "Anzahl Mandate mit aktiven Status: " + mm) 
	  assert(  mm == 246332)
  }
  test( "MS-NichtTerminierteMandateOhnemPayment") {
    val c =   MandateService.getEntryOhnePayment(m).size
    println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + c) 
	  assert(  c == 12912)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val c =  MandateService.getEntryWithPayment(m).size
    println ( "Anzahl Mandate mit aktiven Status und Payments: " + c) 
	  assert(  c == 233420)
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val c =  MandateService.getEntryNotTerminatedAbgelaufen(m).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 56848)
  }
  test( "MS-NichtTerminierteAbgelaufeneMitPayment") {
    val c =  MandateService.getEntryAbgelaufenWithPayment(m).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 43936)
  }
  test( "MS-NichtTerminierteAbgelaufeneOhnePayment") {
    val c =  MandateService.getEntryAbgelaufenOhnePayment(m).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 12912)
  }
  
}
class TestMandateService2  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  val m = MandateService.getNichtTerminierteMandateUndLetztesPayment().map( (m:Map[Long, (Mandate,Option[Payment])]) => MandateService.seperate(m)).transact(xa).unsafeRunSync()

  
 
  test( "MS-NichtTerminierteMandateOhnemPayment") {
    val c =   m.get(MandateService.NtNoPayment).map( _.size).getOrElse(0)
    println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + c) 
	  assert(  c == 12912)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val c =  m.get(MandateService.NtPayment).map( _.size).getOrElse(0)
    println ( "Anzahl Mandate mit aktiven Status und Payments: " + c) 
	  assert(  c == 233420)
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val c =  m.get(MandateService.NtOod).map( _.size).getOrElse(0)
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 56848)
  }
  test( "MS-NichtTerminierteAbgelaufeneMitPayment") {
    val c =  m.get(MandateService.NtOodPayment).map( _.size).getOrElse(0)
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 43936)
  }
  test( "MS-NichtTerminierteAbgelaufeneOhnePayment") {
    val c =  m.get(MandateService.NtOodNoPayment).map( _.size).getOrElse(0)
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 12912)
  }

}
