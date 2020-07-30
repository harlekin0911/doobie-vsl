package de.ways42.vsl.domains.mandate.service


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import de.ways42.vsl.connection.Connect
import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.Mandate
import de.ways42.vsl.domains.mandate.domain.MandateAktDom
import de.ways42.vsl.domains.mandate.domain.MandateAktDom._


class TestMandateService  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  val m = MandateService.getNichtTerminierteMandateUndLetztesPayment().transact(xa).unsafeRunSync()
  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).unsafeRunSync()._2.size == 1)
 	}

  test( "MS-NichtTerminierteMandatet") {
    val mm = m.size
    println ( "Anzahl Mandate mit aktiven Status: " + mm) 
	  assert(  246825 < mm && mm < 246835)
  }
  test( "MS-NichtTerminierteMandateOhnePayment") {
    val c =   m.filter( _._2.mandateHasNoPayment).size
    println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + c) 
	  assert(  13265 < c && c < 13275)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val c =  m.filter( ! _._2.mandateHasNoPayment) .size
    println ( "Anzahl Mandate mit aktiven Status und Payments: " + c) 
	  assert(  c == 233559)
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val c =  m.filter( _._2.abgelaufen).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  c > 58460 && c < 58490)
  }
  test( "MS-NichtTerminierteAbgelaufeneMitPayment") {
    val c =  m.filter( _._2.abgelaufenMitPayment).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  45190 < c && c < 45220)
  }
  test( "MS-NichtTerminierteAbgelaufeneOhnePayment") {
    val c =  m.filter( _._2.abgelaufenOhnePayment).size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  13270 < c && c < 13280)
  }
  
}
class TestMandateService2  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  val m = MandateService.getNichtTerminierteMandateUndLetztesPayment().map( (m:Map[Long, MandateAktDom]) => MandateAktDom.Qual.seperate(m)).transact(xa).unsafeRunSync()

  
 
  test( "MS-NichtTerminierteMandateOhnemPayment") {
    val c =   m.get(MandateAktDom.Qual.NtNoPayment).map( _.size).getOrElse(0)
    //println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + c) 
	  assert(  13265 < c && c < 13275)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val c =  m.get(MandateAktDom.Qual.NtPayment).map( _.size).getOrElse(0)
    //println ( "Anzahl Mandate mit aktiven Status und Payments: " + c) 
	  assert(  c == 233559)
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val c =  m.get(MandateAktDom.Qual.NtOod).map( _.size).getOrElse(0)
    //println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  58460 < c && c < 58490)
  }
  test( "MS-NichtTerminierteAbgelaufeneMitPayment") {
    val c =  m.get(MandateAktDom.Qual.NtOodPayment).map( _.size).getOrElse(0)
    //println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  45190 < c && c < 45230)
  }
  test( "MS-NichtTerminierteAbgelaufeneOhnePayment") {
    val c =  m.get(MandateAktDom.Qual.NtOodNoPayment).map( _.size).getOrElse(0)
    //println ( "Anzahl abgelaufene nicht terminierte Mandate: " + c) 
	  assert(  13260 <  c && c < 13280)
  }

}
