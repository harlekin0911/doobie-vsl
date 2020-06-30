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
	  assert(  e._1 == 13270)
  }
  test( "MS-Aktive-Mit-Payments") {
    println ( "Anzahl Mandate mit aktiven Status und Payments: "            + e._2) 
    assert(  e._2 == 233559)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene") {
    println ( "Anzahl abgelaufene nicht terminierte Mandate: "              + e._3) 
	  assert(  e._3 == 57171)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene-Ohne-Payment") {
    println ( "Anzahl abgelaufene nicht terminierte Mandate ohne Payment: " + e._4) 
	  assert(  e._4 == 13270)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene-Mit-Payment") {
	  println ( "Anzahl abgelaufene nicht terminierte Mandate mit Payment: "  + e._5) 
	  assert(  e._5 == 43901)
  }
}
class TestMandateTask2  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  test( "MS-AllAktMandateDomain-bottom-up") {
  
    //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
    val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
    val ms = MandateTask( xa)
    val mmd = ms.getAllMandateExtDomAkt
    val s = mmd.runSyncUnsafe().size
    assert( s == 302956)
  }
}
class TestMandateTask3  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  test( "MS-AllAktMandateDomain-top-down") {
    //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
    val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
    val ms = MandateTask( xa)
    val mmd = ms.getAllMandateDomainAkt.runSyncUnsafe()
    val s = mmd.size
    val emptyBord = mmd.filter( emd => emd._2.mmed.filter( _._2.md.isEmpty).size > 0)
    emptyBord.map( x => println("Vertrag: " + x._1 + ", BusinessObjectRef: " + x._2.mmed.mkString(",")))
    val outOfDate     = mmd.filter(x => x._2.mmed.filter(y => y._2.isOutOfDate).size > 0).size
    val outOfDateTerm = mmd.filter(x => x._2.mmed.filter(y => y._2.isOutOfDate && y._2.isTerminated).size > 0).size
    assert( s == 302957 && emptyBord.size == 3 && outOfDate == 111838 && outOfDateTerm == 61235 )
  }
}

