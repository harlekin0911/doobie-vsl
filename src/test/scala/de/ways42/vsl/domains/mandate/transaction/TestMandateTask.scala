package de.ways42.vsl.domains.mandate.transaction


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import doobie.implicits.toConnectionIOOps
import de.ways42.vsl.domains.mandate.domain.MandateAktDom
import de.ways42.vsl.domains.mandate.service.MandateService
import de.ways42.vsl.connection.hikari.HcTaskResource
import de.ways42.vsl.TestResults


class TestMandateTask  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xap = HcTaskResource("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)

	val mmp = xap.use { xa => MandateTask(xa).getMapMandateWithLatestPayment()}
  
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
       
  test( "MS-Aktive-Ohne Payment") {
	  println ( "Anzahl Mandate mit aktiven Status ohne Payments: "           + e._1) 
	  assert(  13270 < e._1 && e._1 < 13280 )
  }
  test( "MS-Aktive-Mit-Payments") {
    println ( "Anzahl Mandate mit aktiven Status und Payments: "            + e._2) 
    assert(  e._2 == 233559)
  }
  test( "MS-Nicht-Terminierte-Abgelaufene") {
    println ( "Anzahl abgelaufene nicht terminierte Mandate: "              + e._3) 
	  assert(  e._3 == TestResults.Mandate.Aktive.abgelaufene )
  }
  test( "MS-Nicht-Terminierte-Abgelaufene-Ohne-Payment") {
    println ( "Anzahl abgelaufene nicht terminierte Mandate ohne Payment: " + e._4) 
	  assert(  13270 < e._4 && e._4 < 13280 )
  }
  test( "MS-Nicht-Terminierte-Abgelaufene-Mit-Payment") {
	  println ( "Anzahl abgelaufene nicht terminierte Mandate mit Payment: "  + e._5) 
	  assert(  45190 < e._5 && e._4 < 45230)
  }

  test( "MS-getMandateWithPayments") {
    val t = xap.use { xa => MandateService.getMandateWithPayments( 22317).transact(xa)}
    assert( t.runSyncUnsafe()._2.size == 1)
 	}
}
class TestMandateTask0  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xap = HcTaskResource("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)

	val mmp = xap.use ( xa => MandateTask(xa).getMapMandateWithLatestPayment()).runSyncUnsafe().size  
  
  test( "BuildMandatesWithPayments") {
	  println ( "Anzahl Mandate mit Payment: "  + mmp) 
	  assert( 246825 < mmp && mmp < 246835)
  }
     
}
class TestMandateTask1  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  val xap = HcTaskResource("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)
  
  test( "MS-NichtTerminierteMandateMitLetztemPayment") {
  
    val r =  xap.use( xa => MandateTask( xa).getMapMandateWithLatestPayment()).runSyncUnsafe()
		println ( "Anzahl nicht terminierte: " + r.size) 
	  assert(  r.size >= 12830)
  }
}

class TestMandateTask2  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
 
  val xap = HcTaskResource("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)

  test( "MS-AllAktMandateDomain-bottom-up") {
  
    val mmd = xap.use( xa => MandateTask( xa).getAllMandateDomainAktBottomUp)
    val s = mmd.runSyncUnsafe().size
    assert( s == 302955)
  }
}
class TestMandateTask3  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xap = HcTaskResource("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)

  test( "MS-AllAktMandateDomain-top-down") {
    val mmd = xap.use( xa => MandateTask( xa).getAktAllMandateDomain()).runSyncUnsafe()
    val s = mmd.size
    val emptyBord = mmd.filter( emd => emd._2.mmed.filter( _._2.md.isEmpty).size > 0)
    //emptyBord.map( x => println("Vertrag: " + x._1 + ", BusinessObjectRef: " + x._2.mmed.mkString(",")))
    val ebs = emptyBord.size
    val outOfDate      = mmd.filter(x => x._2.mmed.filter(y => y._2.isOutOfDate).size > 0).size
    val outOfDateTerm  = mmd.filter(x => x._2.mmed.filter(y => y._2.isOutOfDate &&   y._2.isTerminated).size > 0).size
    val outOfDateAkt   = mmd.filter(x => x._2.mmed.filter(y => y._2.isOutOfDate && ! y._2.isTerminated).size > 0).size
    val anzMandate     = mmd.foldLeft(0)( (acc,m) => acc + m._2.anzahlMandate)
    val anzMandateAkt  = mmd.foldLeft(0)( (acc,m) => acc + m._2.anzahlMandateAktive)
    val anzMandateTerm = mmd.foldLeft(0)( (acc,m) => acc + m._2.anzahlMandateTerminated)
    
    assert( 
        s == TestResults.MandateDomain.alle &&
        ebs == 4 && 
        outOfDate      == TestResults.Mandate.outOfDate     &&
        outOfDateTerm  == TestResults.Mandate.outOfDateTerm && 
        outOfDateAkt   == TestResults.Mandate.outOfDateAkt &&
        anzMandate     == TestResults.MandateDomain.anzahlMandate &&
        anzMandateAkt  == TestResults.MandateDomain.anzahlMandateAkt &&
        anzMandateTerm == TestResults.MandateDomain.anzahlMandateTerm )
  }
}

