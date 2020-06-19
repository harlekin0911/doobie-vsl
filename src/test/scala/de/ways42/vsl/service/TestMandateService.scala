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
import de.ways42.vsl.tables.mandate.MandateDom
import de.ways42.vsl.tables.mandate.MandateDom._


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
    val c =   m.filter( _._2.mandateHasNoPayment).size
    println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + c) 
	  assert(  c == 12912)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val c =  m.filter( ! _._2.mandateHasNoPayment) .size
    println ( "Anzahl Mandate mit aktiven Status und Payments: " + c) 
	  assert(  c == 233420)
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val c =  m.filter( _._2.abgelaufen).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 56848)
  }
  test( "MS-NichtTerminierteAbgelaufeneMitPayment") {
    val c =  m.filter( _._2.abgelaufenMitPayment).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 43936)
  }
  test( "MS-NichtTerminierteAbgelaufeneOhnePayment") {
    val c =  m.filter( _._2.abgelaufenOhnePayment).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 12912)
  }
  
}
class TestMandateService2  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  val m = MandateService.getNichtTerminierteMandateUndLetztesPayment().map( (m:Map[Long, MandateDom]) => MandateDom.Qual.seperate(m)).transact(xa).unsafeRunSync()

  
 
  test( "MS-NichtTerminierteMandateOhnemPayment") {
    val c =   m.get(MandateDom.Qual.NtNoPayment).map( _.size).getOrElse(0)
    println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + c) 
	  assert(  c == 12912)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val c =  m.get(MandateDom.Qual.NtPayment).map( _.size).getOrElse(0)
    println ( "Anzahl Mandate mit aktiven Status und Payments: " + c) 
	  assert(  c == 233420)
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val c =  m.get(MandateDom.Qual.NtOod).map( _.size).getOrElse(0)
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 56848)
  }
  test( "MS-NichtTerminierteAbgelaufeneMitPayment") {
    val c =  m.get(MandateDom.Qual.NtOodPayment).map( _.size).getOrElse(0)
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 43936)
  }
  test( "MS-NichtTerminierteAbgelaufeneOhnePayment") {
    val c =  m.get(MandateDom.Qual.NtOodNoPayment).map( _.size).getOrElse(0)
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c == 12912)
  }

}
