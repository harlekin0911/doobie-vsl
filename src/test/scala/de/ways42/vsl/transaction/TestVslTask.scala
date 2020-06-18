package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import de.ways42.vsl.transaction.VslTask
import doobie.implicits.toConnectionIOOps


class TestVslTask  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val vs = VslTask( xa)
  
  val lmp = vs.getAktiveVertraegeMitAktVersicherungen().runSyncUnsafe()
  
  test( "VS-AktiveVertraegeMitAktivenVersicherungen") {
    val r = vs.getAktiveVertraegeMitAktVersicherungen().runSyncUnsafe().size
		println ( "Anzahl nicht terminierte: " + r) 
	  assert(  r == 246523)
  }
    

}
