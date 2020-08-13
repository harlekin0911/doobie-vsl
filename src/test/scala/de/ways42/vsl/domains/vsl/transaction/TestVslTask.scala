package de.ways42.vsl.domains.vsl.transaction


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import doobie.implicits.toConnectionIOOps
import de.ways42.vsl.domains.vsl.domain.VslDom._
import de.ways42.vsl.connection.hikari.HcTransactor
import de.ways42.vsl.domains.vsl.domain.VslDom
import de.ways42.vsl.TestResults

class TestVslTaskAlleVslDom  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val lmp = VslTask( xa).getAllVslDom().runSyncUnsafe()
  
  test( "VslTask-Alle") {
    val r = lmp.size
	  assert(  r ==  TestResults.Vertrag.alle)
  }
  
  // Bpfl ------------------------------------------------------------------------------
  
  test("VslTask-AlleVslDom-Bpfl-nur-Vertrag") {
    val r = lmp.filter( x => x._2.tvsl001.LV_VERTR_STAT_CD == 0).size
	  assert(  r == ( TestResults.Vertrag.Alle.bpflNurVertrag + TestResults.Vertrag.Alle.bpfl)) // == 158980)
  }

  test("VslTask-AlleVslDom-Bpfl-Vers") {
    val r = lmp.filter( x => x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD == 0).isDefined).size
	  assert(  r == ( TestResults.Vertrag.Alle.bpflVers ))
  }

  test("VslTask-AlleVslDom-Bpfl") {
    val r = lmp.filter( x => x._2.istBpfl).size
	  assert(  r == TestResults.Vertrag.Alle.bpfl)
  }
  test("VslTask-AlleVslDom-Bpfl-1") {
    val r = lmp.filter( x => x._2.tvsl001.istBpfl).size
	  assert(  r == TestResults.Vertrag.Alle.bpflVertrag)
  }
  test("VslTask-AlleVslDom-Bpfl-2") {
    val r = lmp.filter( x => x._2.tvsl001.LV_VERTR_STAT_CD == 0).size
	  assert(  r == TestResults.Vertrag.Alle.bpflVertrag)
  }
    
  test("VslTask-AlleVslDom-Bpfl-nur-an-Vertrag") {
    val r = lmp.filter( x => x._2.istBpflNurVertrag).size
	  assert(  r ==  TestResults.Vertrag.Alle.bpflNurVertrag)
  }
  test("VslTask-AlleVslDom-Bpfl-nur-an-Vertrag-2") {
    val r = lmp.filter( x => x._2.tvsl001.LV_VERTR_STAT_CD == 0 && x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD == 0).isEmpty).size
	  assert(  r ==  TestResults.Vertrag.Alle.bpflNurVertrag)
  }
  test("VslTask-AlleVslDom-Bpfl-nur-an-Vers") {
    val r = lmp.filter( x => x._2.istBpflNurVers).size
	  assert(  r == TestResults.Vertrag.Alle.bpflNurVers)
  }

  // Bfr --------------------------------------------------------------------------
  
  test("VslTask-AlleVslDom-Bfr-Vertrag") {
    val r = lmp.filter( x => x._2.tvsl001.istBfr ).size
	  assert(  r == TestResults.Vertrag.Alle.bfrVertrag )
  }
  test("VslTask-AlleVslDom-Bfr-Vertrag-2") {
    val r = lmp.filter( x => 0 < x._2.tvsl001.LV_VERTR_STAT_CD  && x._2.tvsl001.LV_VERTR_STAT_CD < 60 ).size
	  assert(  r == TestResults.Vertrag.Alle.bfrVertrag )
  }
  test("VslTask-AlleVslDom-Bfr-Vertrag-nicht-alle-Versicherungen") {
    val r = lmp.filter( x => x._2.tvsl001.istBfr && x._2.mtvsl002.find( ! _._2.istBfr).isDefined).size
	  assert(  r == TestResults.Vertrag.Alle.bfrNurVertrag)
  }
  test("VslTask-AlleVslDom-Bfr-Vertrag-nicht-alle-Versicherungen-3") {
    val r = lmp.filter( x => x._2.tvsl001.istBfr && x._2.mtvsl002.find( x => x._2.istBpfl || x._2.isReserve).isDefined).size
	  assert(  r == TestResults.Vertrag.Alle.bfrNurVertrag)
  }
  test("VslTask-AlleVslDom-Bfr-Vertrag-nicht-alle-Versicherungen-2") {
    val r = lmp.filter( x => x._2.tvsl001.istBfr && x._2.mtvsl002.find(  _._2.istBpfl).isDefined).size
	  assert(  r == TestResults.Vertrag.Alle.bpflNurVers)
  }
  
  test("VslTask-AlleVslDom-Bfr-an-Vers") {
    val r = lmp.filter( x =>
      x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD == 0).isEmpty &&
      x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD >= 60).isEmpty).size
	  assert(  r == TestResults.Vertrag.Alle.bfrAlleVersicherungen  ) 
  }
  test("VslTask-AlleVslDom-Bfr-alle-Versicherungen") {
    val r = lmp.filter( x => x._2.mtvsl002.find( ! _._2.istBfr).isEmpty).size
	  assert(  r == TestResults.Vertrag.Alle.bfrAlleVersicherungen)
  }

  test("VslTask-AlleVslDom-Bfr") {
    val r = lmp.filter( x => x._2.istBfr).size
    
	  assert(  r == TestResults.Vertrag.Alle.bfr)
  }
  
  test("VslTask-AlleVslDom-Bfr-nicht-Vertrag-alle-Versicherungen") {
    val r = lmp.filter( x => ! x._2.tvsl001.istBfr && x._2.mtvsl002.find( ! _._2.istBfr).isEmpty).size
	  assert(  r == TestResults.Vertrag.Alle.bfrNurAlleVersicherungen)
  }
  
  test ("TestResults.Vertrag.Alle.bfr") {
    import TestResults.Vertrag.Alle._
    assert(  bfr  == bfrVertrag +  bpflNurVertrag )
  }

  // Reserve ------------------------------------------------------------------------------
  // Reserve  == Vertrag in Reserve, es muss keine Versicherung in Reserve sein == Vertrag in Reserve

  test("VslTask-AlleVslDom-Reserve") {
    val r = lmp.filter( x => x._2.isReserve).size
	  assert(  r == TestResults.Vertrag.Alle.reserve)
  }

  test("VslTask-AlleVslDom-Reserve-an-Vertrag") {
    val r = lmp.filter( x => x._2.tvsl001.LV_VERTR_STAT_CD >= 60).size
	  assert(  r == TestResults.Vertrag.Alle.reserve)
  }

  test("VslTask-AlleVslDom-Reserve-an-Vertrag-Reserve-an-Vers") {
    val r = lmp.filter( x => 
      x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD >= 60).isDefined && 
      x._2.tvsl001.LV_VERTR_STAT_CD >= 60).size
	  assert(  r == TestResults.Vertrag.Alle.reserveVertragUndVers )
  }
  test("VslTask-AlleVslDom-Reserve-An-Vers") {
    val r = lmp.filter( x => x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD < 60).isEmpty ).size 
	  assert(  r == TestResults.Vertrag.Alle.alleVersInReserve ) 
  }
  
  test("VslTask-AlleVslDom-Reserve-An-Vertrag-Nicht-an-Vers") {
    val r = lmp.filter( x => 
      x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD >= 60).isEmpty && 
      x._2.tvsl001.LV_VERTR_STAT_CD >= 60).size
	  assert(  r == TestResults.Vertrag.Alle.reserve - TestResults.Vertrag.Alle.reserveVertragUndVers)
  }
  test("VslTask-AlleVslDom-Reserve-An-Vers-Nicht-an-Vertrag") {
    val r = lmp.filter( x => 
      x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD >= 60).isDefined && 
      x._2.tvsl001.LV_VERTR_STAT_CD < 60).size
	  assert(  r == TestResults.Vertrag.Alle.reserveNurVers)//TestResults.Vertrag.Alle.reserve)
  }
  test("VslTask-AlleVslDom-Reserve-Alle-Vers-Nicht-an-Vertrag") {
    val r = lmp.filter( x => 
      x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD < 60).isEmpty && 
      x._2.tvsl001.LV_VERTR_STAT_CD < 60).size
	  assert(  r == 0)
  }
  
  test("VslTask-AlleVslDom-Reserve-an-Vertrag-Reserve-alle-Vers") {
    val r = lmp.filter( x => 
      x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD < 60).isEmpty && 
      x._2.tvsl001.LV_VERTR_STAT_CD >= 60).size
	  assert(  r == TestResults.Vertrag.Alle.alleVersInReserve)
  }

  test("VslTask-AlleVslDom-Reserve-an-Vertrag-nicht-an-Vers") {
    val r = lmp.filter( x => 
      x._2.mtvsl002.find( _._2.LV_VERS_STAT_CD < 60).isDefined && 
      x._2.tvsl001.LV_VERTR_STAT_CD >= 60).size
	  assert(  r == TestResults.Vertrag.Alle.reserve - TestResults.Vertrag.Alle.alleVersInReserve)
  }
    
  test("VS-TestResults.Vertrag.Alle") {
	  assert(  TestResults.Vertrag.alle == 
	    TestResults.Vertrag.Alle.bpfl +
	    //TestResults.Vertrag.Alle.bpflNurVertrag + 
	    //TestResults.Vertrag.Alle.bpflNurVers + 
	    TestResults.Vertrag.Alle.bfr + 
	    TestResults.Vertrag.Alle.reserve)
  }
  test("VS-TestResults.Vertrag.Alle-2") {
	  assert(  TestResults.Vertrag.alle == 
	    //TestResults.Vertrag.Alle.bpfl +
	    TestResults.Vertrag.Alle.bpfl + 
	    TestResults.Vertrag.Alle.bpflNurVertrag +
	    //TestResults.Vertrag.Alle.bpflNurVers + 
	    TestResults.Vertrag.Alle.bfrVertrag + 
	    TestResults.Vertrag.Alle.reserve)
  }
}


class TestVslTask  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val vs = VslTask( xa)
  
  lazy val lmp = vs.getAllAktiveVslDom().runSyncUnsafe()
  
  test( "VS-AktiveVertraegeMitAktivenVersicherungen") {
    val r = lmp.size
		//println ( "Anzahl nicht terminierte: " + r) 
	  assert(  r ==  TestResults.Vertrag.Aufrecht.alle)
  }
  
  test("VS-Beitragsfrei-Vericherungen") {
    val r = lmp.filter( x => x._2.istBfr).size
		//println ( "Anzahl beitragsfreie: " + r) 
	  assert(  r == TestResults.Vertrag.Aufrecht.bfr)
  }
  test("VS-Beitragspflichtige-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpfl).size
		//println ( "Anzahl beitragspflichtige: " + r) 
	  assert(  r == TestResults.Vertrag.Aufrecht.bpfl)
  }
    
  test("VS-BeitragspflichtigeNurVertrag-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpflNurVertrag).size
		//println ( "Anzahl beitragspflichtigeNurVertrag: " + r) 
	  assert(  r ==  TestResults.Vertrag.Aufrecht.bpflNurVertrag)
  }
  test("VS-BeitragspflichtigeNurVers-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpflNurVers).size
		//println ( "Anzahl beitragspflichtigeNurVers: " + r) 
	  assert(  r == TestResults.Vertrag.Aufrecht.bpflNurVers)
  }

  test("VS-Reserve") {
    val r = lmp.filter( x => x._2.isReserve).size
		//println ( "Anzahl auf Reserve: " + r) 
	  assert(  r == TestResults.Vertrag.Aufrecht.reserve)
  }
  test("VS-TestResults.Vertrag.Aufrecht") {
	  assert(  TestResults.Vertrag.Aufrecht.alle == TestResults.Vertrag.Aufrecht.bpfl + TestResults.Vertrag.Aufrecht.bfr + TestResults.Vertrag.Aufrecht.reserve)
  }
}
class TestVslTask2  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  //import monix.execution.Scheduler.Implicits.global
  import monix.eval.Task
  
  
  test( "Load-Single-Vertrag") {
    implicit val (xas,ss,ds) = HcTransactor( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 5)
  
    val vt  = xas.map( xa => VslTask(xa)).flatMap(_.getSingleVslDom("0003065903411")).runSyncUnsafe()
    val vt2 = xas.map( xa => VslTask(xa)).flatMap(_.getSingleVslDom("1234567890123")).runSyncUnsafe()

    assert( vt.isEmpty == false && vt2.isEmpty == true)
      
    ds.close()
    ss.shutdown()
  }
}
class TestVslTask3  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  //import monix.execution.Scheduler.Implicits.global
  import monix.eval.Task
  
  
  test( "Load-Aktive-All-MandateRefDom") {
    implicit val (xas,ss,ds) = HcTransactor( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 5)
  
    val vt  = xas.map( xa => VslTask(xa)).flatMap(_.getAllAktiveMandateRefDom()).runSyncUnsafe()

    val s  = vt.size
    
    val mes = vt.filter( x => x._2.vsldom.isEmpty == true) // mandate ohne vertrag
    val es   = mes.size
    
    val nm = vt.filter( x => x._2.lr1 == Nil).size // vertraege ohne mandate

    assert( 
        s  == TestResults.VertragUndRolle.Aufrecht.alle && 
        es == TestResults.VertragUndRolle.Aufrecht.ohneVertrag  && 
        nm == TestResults.VertragUndRolle.Aufrecht.ohneMandat)   // trim komplett beim Aufbau MandateRefDom
    
    
    //println( mes.take(5).mkString(";"))
    
    ds.close()
    ss.shutdown()
  }
} 
class TestVslTask4  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  //import monix.execution.Scheduler.Implicits.global
  import monix.eval.Task
  
  
  test( "Load-All-MandateRefDom") {
    implicit val (xas,ss,ds) = HcTransactor( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together", 5)
  
    val vt  = xas.map( xa => VslTask(xa)).flatMap(_.getAllMandateRefDom()).runSyncUnsafe()

    val s  = vt.size
    val mes = vt.filter( x => x._2.vsldom.isEmpty == true) // mandate ohne vertrag
    
    val es   = mes.size
    val nm = vt.filter( x => x._2.lr1 == Nil).size // vertraege ohne mandate

    val vr = vt.filter( x => x._2.lr1 != Nil && x._2.vsldom.isDefined).size // vertraege ohne mandate
    assert( 
        s  == TestResults.VertragUndRolle.alle && 
        es == TestResults.VertragUndRolle.ohneVertrag  && 
        nm == TestResults.VertragUndRolle.ohneMandat  && 
        vr == TestResults.VertragUndRolle.beides)   // trim komplett beim Aufbau MandateRefDom
 
    assert( TestResults.VertragUndRolle.alle  == 
      TestResults.VertragUndRolle.ohneVertrag + 
      TestResults.VertragUndRolle.ohneMandat + 
      TestResults.VertragUndRolle.beides)
    //println( mes.take(5).mkString(";"))
      
    ds.close()
    ss.shutdown()
  }  
}

