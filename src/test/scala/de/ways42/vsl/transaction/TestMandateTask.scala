package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import de.ways42.vsl.transaction.MandateTask
import doobie.implicits.toConnectionIOOps

//import de.ways42.vsl.connection.hikari.HCPoolTask


class TestMandateTask  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val ms = MandateTask( xa)
  
  val lmp = ms.getAllMandatesWithPayments()
  val mmp = ms.getMapMandateWithLatestPayment( lmp)
     
  val t = for {
      _ <-  monix.eval.Task.unit;  m = 1;  h  = 2
      a <- ms.getNichtTerminierteMandateOhnePayment(mmp)
      b <- ms.getNichtTerminierteMandateMitPayment(mmp)
      c <- ms.getNichtTerminierteAbgelaufeneMandate(mmp)
      d <- ms.getNichtTerminierteAbgelaufeneMandateOhnePayment(mmp)
      e <- ms.getNichtTerminierteAbgelaufeneMandateWithPayment(mmp)
    } yield (a.size,b.size,c.size, d.size, e.size)

  val e = t.runSyncUnsafe()
  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).runSyncUnsafe()._2.size == 1)
 	}
    
  test( "MS-NichtTerminierteMandateMitLetztemPayment") {
    val r = ms.getNichtTerminierteMandateUndLetztesPayment().runSyncUnsafe()
		println ( "Anzahl nicht terminierte: " + r.size) 
	  assert(  r.size >= 12830)
  }
    

  test( "MS-Complete") {
  }
    
  test( "MS-Aktive-Ohne Payment") {
	  println ( "Anzahl Mandate mit aktiven Status ohne Payments: "           + e._1) 
	  assert(  e._1 == 12912)
  }
  test( "MS-Aktive-Mit-Payments") {
    println ( "Anzahl Mandate mit aktiven Status und Payments: "            + e._2) 
    assert(  e._2 == 233420)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene") {
    println ( "Anzahl abgelaufene nicht terminierte Mandate: "              + e._3) 
	  assert(  e._3 == 56848)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene-Ohne-Payment") {
    println ( "Anzahl abgelaufene nicht terminierte Mandate ohne Payment: " + e._4) 
	  assert(  e._4 == 12912)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene-Mit-Payment") {
	  println ( "Anzahl abgelaufene nicht terminierte Mandate mit Payment: "  + e._5) 
	  assert(  e._5 == 43936)
  }
}
