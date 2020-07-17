package de.ways42.vsl.domains.mandate.transaction


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import doobie.implicits.toConnectionIOOps
import de.ways42.vsl.domains.mandate.domain.MandateAktDom
import de.ways42.vsl.domains.mandate.service.MandateService

//import de.ways42.vsl.connection.hikari.HCPoolTask


class TestMandateTask  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val ms = MandateTask( xa)
  
  val mmp = ms.getMapMandateWithLatestPayment()
  
  import monix.eval.Task

  val t = Task.parZip3(   
      mmp.map( MandateAktDom.getNichtTerminierteMandateOhnePayment(_).size),
      mmp.map( MandateAktDom.getNichtTerminierteMandateMitPayment(_).size),
      mmp.map( MandateAktDom.getNichtTerminierteAbgelaufeneMandate(_).size)).flatMap( x =>( Task.parZip2(
      mmp.map( MandateAktDom.getNichtTerminierteAbgelaufeneMandateOhnePayment(_).size),
      mmp.map( MandateAktDom.getNichtTerminierteAbgelaufeneMandateWithPayment(_).size))
      ).map ( y => (x._1, x._2,x._3, y._1, y._2)))
      
  val tt = for {
      _ <-  monix.eval.Task.unit;  m = 1;  h  = 2
      a <- mmp.map( MandateAktDom.getNichtTerminierteMandateOhnePayment(_))
      b <- mmp.map( MandateAktDom.getNichtTerminierteMandateMitPayment(_))
      c <- mmp.map( MandateAktDom.getNichtTerminierteAbgelaufeneMandate(_))
      d <- mmp.map( MandateAktDom.getNichtTerminierteAbgelaufeneMandateOhnePayment(_))
      e <- mmp.map( MandateAktDom.getNichtTerminierteAbgelaufeneMandateWithPayment(_))
    } yield ( a.size, b.size, c.size, d.size, e.size)

  val e = t.runSyncUnsafe()

  
  test( "MS-getMandateWithPayments") {
    assert( MandateService.getMandateWithPayments( 22317).transact(xa).runSyncUnsafe()._2.size == 1)
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
	  assert(  e._3 == 58460)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene-Ohne-Payment") {
    println ( "Anzahl abgelaufene nicht terminierte Mandate ohne Payment: " + e._4) 
	  assert(  e._4 == 13270)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene-Mit-Payment") {
	  println ( "Anzahl abgelaufene nicht terminierte Mandate mit Payment: "  + e._5) 
	  assert(  e._5 == 45190)
  }
}
class TestMandateTask0  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val ms = MandateTask( xa)
  
  //val lmp = ms.getAllMandatesWithPayments()
  val mmp = ms.getMapMandateWithLatestPayment().runSyncUnsafe().size
  
  test( "BuildMandatesWithPayments") {
	  println ( "Anzahl Mandate mit Payment: "  + mmp) 
	  assert(  mmp == 246829)
  }
     
}
class TestMandateTask1  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  test( "MS-NichtTerminierteMandateMitLetztemPayment") {
  
    //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
    val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
    val ms = MandateTask( xa)
    val r = ms.getMapMandateWithLatestPayment().runSyncUnsafe()
		println ( "Anzahl nicht terminierte: " + r.size) 
	  assert(  r.size >= 12830)
  }
}

class TestMandateTask2  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  test( "MS-AllAktMandateDomain-bottom-up") {
  
    //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
    val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
    val ms = MandateTask( xa)
    val mmd = ms.getAllMandateDomainAktBottomUp
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
    val mmd = ms.getAllMandateDomainAktTopDown().runSyncUnsafe()
    val s = mmd.size
    val emptyBord = mmd.filter( emd => emd._2.mmed.filter( _._2.md.isEmpty).size > 0)
    emptyBord.map( x => println("Vertrag: " + x._1 + ", BusinessObjectRef: " + x._2.mmed.mkString(",")))
    val ebs = emptyBord.size
    val outOfDate     = mmd.filter(x => x._2.mmed.filter(y => y._2.isOutOfDate).size > 0).size
    val outOfDateTerm = mmd.filter(x => x._2.mmed.filter(y => y._2.isOutOfDate && y._2.isTerminated).size > 0).size
    assert( s == 302957 && ebs == 4 && outOfDate == 113166 && outOfDateTerm == 61282 )
  }
}

