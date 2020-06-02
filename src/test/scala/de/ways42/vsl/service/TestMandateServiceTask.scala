package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import de.ways42.vsl.connection.Connect
//import de.ways42.vsl.connection.hikari.HCPoolTask


class TestMandateServiceTask  extends AnyFunSuite  { 
  
  import monix.execution.Scheduler.Implicits.global
  val xa = Connect.usingOwnMonad( "VSMADM", "together")
  lazy val ms = MandateServiceTask( xa)
  
  //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
  
  test( "MS-getMandateWithPayments") {
    assert( ms.getMandateWithPayments( 22317).transact(xa).runSyncUnsafe()._2.size == 1)
 	}
    
  test( "MS-NichtTerminierteAbgelaufeneMandateMitLetztemPayment") {
    val r = ms.getNichtTerminierteAbgelaufeneMandateMitLetztemPayment().runSyncUnsafe()
		println ( "Anzahl abgelaufene mit aktiven Status: " + r.size) 
	  assert(  r.size >= 12830)
  }
}
