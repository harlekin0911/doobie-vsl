package de.ways42.vsl.domains.mandate.transaction


import org.scalatest.funsuite.AnyFunSuite

import de.ways42.vsl.connection.Connect
import doobie.implicits.toConnectionIOOps
import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.Mandate
import de.ways42.vsl.domains.mandate.tables.BusinessObjectRef
import monix.eval.Task

//import de.ways42.vsl.connection.hikari.HCPoolTask


class TestMandateTaskSelectOnly  extends AnyFunSuite  { 
  
  //CompanionImpl.Implicits.global
  import monix.execution.Scheduler.Implicits.global
  
  //val (a,b,c) = HCPoolTask("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 3)
  val xa = Connect.usingOwnMonad( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
  lazy val ms = MandateTask( xa)
  
     
  
  test( "MS-AllAktMandateDomain") {
    val v = Task.parZip3(
      BusinessObjectRef.selectAktAll().transact(xa),
      Mandate.selectAktAll().transact(xa),
      Payment.selectLastPaymentAlle().transact(xa)).runSyncUnsafe()
    val s = ( v._1.size, v._2.size, v._3.size)  
    assert( s._1 == 312906 && s._2 < 312910 && s._2 > 312900 && s._3 == 281156)
  }
  
    
}
