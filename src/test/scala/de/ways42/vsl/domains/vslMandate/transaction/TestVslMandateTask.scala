package de.ways42.vsl.domains.vslMandate.transaction


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import de.ways42.vsl.connection.hikari.HcTransactor
import de.ways42.vsl.connection.hikari.HcTaskResource
import de.ways42.vsl.TestResults



class TestVslMandateTask  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val vs = VslMandateTask( xa)
  
  lazy val lmp = vs.getAllAktive().runSyncUnsafe()
  
  test ( "VslMandateDom-omdom-is-empty") {
    val s = lmp.filter( x => x._2.omdom.isEmpty)
    //println( s.head)
    val ss = s.size
    assert( ss == TestResults.VslMandate.Aktive.ohneMandate)
  }
  test ( "VslMandateDom-omrd-is-empty") {
    val s = lmp.filter( x => x._2.omrd.isEmpty)
    //println( s.head)
    val ss = s.size
    assert( ss == TestResults.VslMandate.Aktive.ohneVertrag )// vorher 13363)    
  }
  test ( "VslMandateDom-non-empty") {
    val s = lmp.filter( x => x._2.omdom.isDefined && x._2.omrd.isDefined && x._2.omdom.get.anzahlMandate() != x._2.omrd.get.anzahlMandate())
    //println( s.head)
    val ss = s.size
    assert( ss == 11369)
  }
  test( "VslMandateDom-AktiveVertraegeMitAktivenVersicherungen") {
    val r = lmp.size
		//println ( "Anzahl nicht terminierte: " + r) 
	  assert(  r == TestResults.VslMandate.Aktive.alle)
  }
  
  test("VslMandateDom-Beitragspflichtige-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpfl).size
		//println ( "Anzahl beitragspflichtige: " + r) 
	  assert(  r == TestResults.VslMandate.Aktive.bpfl)
  }
    
  test("VslMandateDom-BeitragspflichtigeNurVertrag-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpflNurVertrag).size
		//println ( "Anzahl beitragspflichtigeNurVertrag: " + r) 
	  assert(  r == TestResults.VslMandate.Aktive.bpflNurVertrag)
  }
  test("VslMandateDom-BeitragspflichtigeNurVers-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpflNurVers).size
		//println ( "Anzahl beitragspflichtigeNurVers: " + r) 
	  assert(  r == TestResults.VslMandate.Aktive.bpflNurVers)
  }

}

class TestVslMandateTask2  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  //import monix.execution.Scheduler.Implicits.global
  import monix.eval.Task
  
	implicit val sc = monix.execution.Scheduler.io( "Monix-Pool")
  val xap = HcTaskResource("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)

	val t = (s:String ) => xap.use { xa => VslMandateTask(xa).getSingle(s)}
	
  test( "VslMandateDom-Single-0003065903411") {
    val v = t("0003065903411").runSyncUnsafe()
	  assert( v.omrd.isEmpty == false && v.omdom.isEmpty == false )
	}
  
  test( "VslMandateDom-Single-1234567890123") {
    val v = t("1234567890120").runSyncUnsafe()
    //println( v)
    assert( v.omrd.isEmpty == true)
  }
  test( "VslMandateDom-Single-0050024689411") {
    val v = t("0050024689411").runSyncUnsafe()
    assert( v.omdom.get.mmed.size == 19)
  }
                                       
}

class TestVslMandateTask3  extends AnyFunSuite  { 
  
  import monix.eval.Task
  
  
  test( "VslMandate-All") {
    implicit val (xas,ss,ds) = HcTransactor( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)
  
    val vt  = xas.map( xa => VslMandateTask(xa)).flatMap(_.getAll()).runSyncUnsafe()
    val vtSize  = vt.size
    
    // ohne vertrag
    val mes = vt.filter( x => x._2.omdom.isEmpty == true) 
    val es   = mes.size
    
    // ohne mandate
    val mnm = vt.filter( x => x._2.omrd.isEmpty == true)  
    val nm = mnm.size

    val aufrecht = vt.filter( x => x._2.isAufrecht)
    val aufrechtSize = aufrecht.size
    
    val bfr = vt.filter( x => x._2.istBfr)
    val bfrSize = bfr.size

    val bfrAufrecht = bfr.filter( _._2.isAufrecht)
    val bfrAufrechtSize = bfrAufrecht.size
    
    val bfrNotValid = bfr.filter( _._2.validateMandate())
    val bfrNotValidSize = bfrNotValid.size

    val reserve = vt.filter( _._2.isReserve)
    val reserveSize = reserve.size
    
    assert(  
        vtSize           == TestResults.VslMandate.alle         && 
        es               == 155514 && //vorher 154275  && 
        nm               == 12142   && 
        aufrechtSize     == TestResults.Vertrag.Aufrecht.alle     &&
        bfrSize          == TestResults.Vertrag.Aufrecht.bfr   && 
        bfrAufrechtSize  == TestResults.Vertrag.Aufrecht.bfr    && 
        bfrNotValidSize  == 0       && 
        reserveSize      == 202824)  
    
    
    //println( mes.take(5).mkString(";"))
    
    ds.close()
    ss.shutdown()
  }
} 

class TestVslMandateTask4  extends AnyFunSuite  { 
  
  import monix.eval.Task
  
  
  test( "VslMandate-AktiveBeitragsfreiMandateAbgelaufen") {
    implicit val (xas,ss,ds) = HcTransactor( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)
  
    val vt  = xas.map( xa => VslMandateTask(xa)).flatMap(_.getAllAktive()).runSyncUnsafe()
    val vtSize = vt.size
    
    // ohne vertrag
    val mes = vt.filter( x => x._2.omdom.isEmpty == true) 
    val es   = mes.size
    
    // ohne mandate
    val mnm = vt.filter( x => x._2.omrd.isEmpty == true)  
    val nm = mnm.size
    
    val aufrecht = vt.filter( x => x._2.isAufrecht)
    val aufrechtSize = aufrecht.size
    
    val bfr = vt.filter( x => x._2.istBfr)
    val bfrSize = bfr.size

    val bfrAufrecht = bfr.filter( _._2.isAufrecht)
    val bfrAufrechtSize = bfrAufrecht.size
    
    val bfrNotValid = bfr.filter( _._2.validateMandate())
    val bfrNotValidSize = bfrNotValid.size

    val reserve = vt.filter( _._2.isReserve)
    val reserveSize = reserve.size
    
    assert(  
        vtSize           == TestResults.VslMandate.Aktive.alle  && 
        es               ==  TestResults.VslMandate.Aktive.ohneMandate  && 
        nm               == TestResults.VslMandate.Aktive.ohneVertrag   &&
        aufrechtSize     ==  TestResults.VslMandate.Aktive.aufrecht &&
        bfrSize          == TestResults.VslMandate.Aktive.bfr   && 
        bfrAufrechtSize  == TestResults.VslMandate.Aktive.bfrAufrecht  && 
        bfrNotValidSize  == TestResults.VslMandate.Aktive.bfrNotValid       && 
        reserveSize      == TestResults.VslMandate.Aktive.reserve)  
    
       
    ds.close()
    ss.shutdown()
  }
} 

class TestVslMandateTask5  extends AnyFunSuite  { 
  
  import monix.eval.Task
  
  
  test( "VslMandate-All-ohne-Mandate") {
    implicit val (xas,ss,ds) = HcTransactor( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 32)
  
    //VslMandateDom ohne Mandate
    val vt  = xas.map( xa => VslMandateTask(xa)).flatMap(_.getAll()).map( _.filter( x => x._2.omdom.isEmpty == true)).runSyncUnsafe()
    ds.close()
    ss.shutdown()

        
    val vtSize  = vt.size
    
    val aufrecht = vt.filter( x => x._2.isAufrecht)
    val aufrechtSize = aufrecht.size
    
    val bfr = vt.filter( x => x._2.istBfr)
    val bfrSize = bfr.size

    val bfrAufrecht = bfr.filter( _._2.isAufrecht)
    val bfrAufrechtSize = bfrAufrecht.size
    
    val bfrNotValid = bfr.filter( _._2.validateMandate())
    val bfrNotValidSize = bfrNotValid.size

    val reserve = vt.filter( _._2.isReserve)
    val reserveSize = reserve.size
    

    assert(  
        vtSize           == TestResults.VslMandate.AlleOhneMandate.alle    && 
        aufrechtSize     == 68658     &&
        bfrSize          == 55992   && 
        bfrAufrechtSize  == 55992   && 
        bfrNotValidSize  == 0       && 
        reserveSize      == 85617)  
        
  }
} 

