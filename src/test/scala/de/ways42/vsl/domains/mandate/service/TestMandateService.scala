package de.ways42.vsl.domains.mandate.service


import org.scalatest.funsuite.AnyFunSuite

import cats._
import cats.effect._
import de.ways42.vsl.TestResults
import de.ways42.vsl.connection.Connect
import de.ways42.vsl.domains.mandate.domain.MandateAktDom
import de.ways42.vsl.domains.mandate.domain.MandateAktDom._
import doobie._
import doobie.implicits._


class TestMandateService  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  import TestResults.Mandate.Aktive._
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  val m = MandateService.getNichtTerminierteMandateUndLetztesPayment().transact(xa).unsafeRunSync()
  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).unsafeRunSync()._2.size == 1)
 	}

  test( "MS-NichtTerminierteMandatet") {
    val mm = m.size
	  assert(  mm == alle)
  }
  test( "MS-NichtTerminierteMandateOhnePayment") {
    val c =   m.filter( _._2.mandateHasNoPayment).size
	  assert(  c == ohnePayment)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val c =  m.filter( ! _._2.mandateHasNoPayment) .size
	  assert(  c == mitPayment )
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val c =  m.filter( _._2.abgelaufen).size
	  assert(  c == abgelaufene)
  }
  test( "MS-NichtTerminierteAbgelaufeneMitPayment") {
    val c =  m.filter( _._2.abgelaufenMitPayment).size
	  assert(  c == abgelaufeneMitPayment)
  }
  test( "MS-NichtTerminierteAbgelaufeneOhnePayment") {
    val c =  m.filter( _._2.abgelaufenOhnePayment).size
	  assert(  abgelaufeneOhnePayment == c )
  }
  test( "MS-TestResults") {
	  assert(  alle == ohnePayment + mitPayment )
	  assert(  abgelaufene == abgelaufeneOhnePayment + abgelaufeneMitPayment )
  }
  
}
class TestMandateService2  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  val m = MandateService.getNichtTerminierteMandateUndLetztesPayment().map( (m:Map[Long, MandateAktDom]) => MandateAktDom.Qual.seperate(m)).transact(xa).unsafeRunSync()

  import TestResults.Mandate.Aktive._

 
  test( "MS-AktiveMandateOhnemPayment") {
    val c =   m.get(MandateAktDom.Qual.NtNoPayment).map( _.size).getOrElse(0)
	  assert(  c == ohnePayment)
  }
  test( "MS-AktiveMandateMitPayment") {
    val c =  m.get(MandateAktDom.Qual.NtPayment).map( _.size).getOrElse(0)
	  assert(  c == mitPayment)
  }
  test( "MS-AktiveAbgelaufene") {
    val c =  m.get(MandateAktDom.Qual.NtOod).map( _.size).getOrElse(0)
	  assert(  c == abgelaufene)
  }
  test( "MS-AktiveAbgelaufeneMitPayment") {
    val c =  m.get(MandateAktDom.Qual.NtOodPayment).map( _.size).getOrElse(0)
	  assert(  c == abgelaufeneMitPayment)
  }
  test( "MS-AktiveAbgelaufeneOhnePayment") {
    val c =  m.get(MandateAktDom.Qual.NtOodNoPayment).map( _.size).getOrElse(0)
	  assert(  c == ohnePayment)
  }

}
