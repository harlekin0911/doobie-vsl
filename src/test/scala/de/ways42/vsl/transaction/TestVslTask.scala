package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import de.ways42.vsl.transaction.VslTask
import doobie.implicits.toConnectionIOOps
import de.ways42.vsl.tables.vsmadm.VslDom._


class TestVslTask  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val vs = VslTask( xa)
  
  val lmp = vs.getAktiveVertraegeMitAktVersicherungen().runSyncUnsafe()
  
  test( "VS-AktiveVertraegeMitAktivenVersicherungen") {
    val r = lmp.size
		println ( "Anzahl nicht terminierte: " + r) 
	  assert(  r == 240146)
  }
  
  test("VS-Beitragspflichtige-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpfl).size
		println ( "Anzahl beitragspflichtige: " + r) 
	  assert(  r == 158983)
  }
    
  test("VS-BeitragspflichtigeNurVertrag-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpflNurVertrag).size
		println ( "Anzahl beitragspflichtigeNurVertrag: " + r) 
	  assert(  r == 10)
  }
  test("VS-BeitragspflichtigeNurVers-Vericherungen") {
    val r = lmp.filter( x => x._2.istBpflNurVers).size
		println ( "Anzahl beitragspflichtigeNurVers: " + r) 
	  assert(  r == 8169)
  }

}
