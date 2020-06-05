package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import de.ways42.vsl.transaction.MandateTask
import doobie.implicits.toConnectionIOOps

//import de.ways42.vsl.connection.hikari.HCPoolTask


class TestMandateTask  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val ms = MandateTask( xa)
  
  val mmp = ms.getAllMandatesWithPayments()

  
  //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).runSyncUnsafe()._2.size == 1)
 	}
    
  test( "MS-NichtTerminierteMandateMitLetztemPayment") {
    val r = ms.getNichtTerminierteMandateUndLetztesPayment().runSyncUnsafe()
		println ( "Anzahl nicht terminierte: " + r.size) 
	  assert(  r.size >= 12830)
  }
    
  test( "MS-NichtTerminierteMandatet") {
    val m = ms.getNichtTerminierteMandateMitPayment(mmp).runSyncUnsafe().size
    println ( "Anzahl Mandate mit aktiven Status: " + m) 
	  assert(  m == 246332)
  }
  test( "MS-NichtTerminierteMandateOhnemPayment") {
    val m = ms.getNichtTerminierteMandateOhnePayment(mmp).runSyncUnsafe().size
    println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + m) 
	  assert(  m == 12912)
  }
  test( "MS-NichtTerminierteMandateMitPayment") {
    val m = ms.getNichtTerminierteMandateMitPayment(mmp).runSyncUnsafe().size
    println ( "Anzahl Mandate mit aktiven Status und Payments: " + m) 
	  assert(  m == 233420)
  }
  test( "MS-NichtTerminierteAbgelaufene") {
    val m = ms.getNichtTerminierteAbgelaufeneMandate(mmp).runSyncUnsafe().size
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + m) 
	  assert(  m == 189498)
  }

}
