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
  
  val lmp = ms.getAllMandatesWithPayments()
  val mmp = ms.getMapMandateWithLatestPayment( lmp)
     
  val t = for {
      a <- ms.getNichtTerminierteMandateOhnePayment(mmp)
      b <- ms.getNichtTerminierteMandateMitPayment(mmp)
      c <- ms.getNichtTerminierteAbgelaufeneMandate(mmp)
    } yield (a.size,b.size,c.size)


  
  //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).runSyncUnsafe()._2.size == 1)
 	}
    
  test( "MS-NichtTerminierteMandateMitLetztemPayment") {
    val r = ms.getNichtTerminierteMandateUndLetztesPayment().runSyncUnsafe()
		println ( "Anzahl nicht terminierte: " + r.size) 
	  assert(  r.size >= 12830)
  }
    

  test( "MS-Complete") {
    
    val e = t.runSyncUnsafe()
    
	  println ( "Anzahl Mandate mit aktiven Status ohne Payments: " + e._1) 
	  assert(  e._1 == 12912)
    
    println ( "Anzahl Mandate mit aktiven Status und Payments: " + e._2) 
	  assert(  e._2 == 233420)
  
    println ( "Anzahl abgelaufene nicht terminierte Mandate: " + e._3) 
	  assert(  e._3 == 189484)

  }
}
