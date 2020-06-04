package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import de.ways42.vsl.transaction.MandateTask
import doobie.implicits.toConnectionIOOps

//import de.ways42.vsl.connection.hikari.HCPoolTask


class TestMandateTask  extends AnyFunSuite  { 
  
  import monix.execution.Scheduler.Implicits.global
  //CompanionImpl.Implicits.global
  val xa = Connect.usingOwnMonad( "VSMADM", "together")
  lazy val ms = MandateTask( xa)
  
  //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).runSyncUnsafe()._2.size == 1)
 	}
    
  test( "MS-NichtTerminierteMandateMitLetztemPayment") {
    val r = ms.getNichtTerminierteMandateMitLetztemPayment().runSyncUnsafe()
		println ( "Anzahl abgelaufene mit aktiven Status: " + r.size) 
	  assert(  r.size >= 12830)
  }
}
