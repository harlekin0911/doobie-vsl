package de.ways42.vsl.domain.mandate.tables

//import de.ways42.vsl.tables.Tables.TVSL001



import java.sql.Date
import java.sql.Timestamp

import org.scalatest.funsuite.AnyFunSuite

import cats._
import cats.effect._
//TestSuite
import de.ways42.vsl.connection.Connect
import de.ways42.vsl.domains.mandate.tables.Mandate
import doobie._
import doobie.implicits._
import de.ways42.vsl.TestResults


class TestMandate extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")

	import TestResults.Mandate._
	
  test ( "Mandate-selectAktById") {
			assert( Mandate.selectAktById(313038).transact(xa).unsafeRunSync.get.MANDATE_ID == 313038)
	}
	
  test ( "Mandate-selectAllById") {
			assert( Mandate.selectAllById(313038).transact(xa).unsafeRunSync.size == 2)
  }
  
  test ( "Mandate-selectAktAll") {
	  val s = Mandate.selectAktAll().transact(xa).unsafeRunSync.size
		assert( s  == TestResults.Mandate.alle)
  }
  
	test ( "Mandate-selectAktAllNotTerminated") {
	  val s =  Mandate.selectAktAllAktive().transact(xa).unsafeRunSync.size
		assert(   s == TestResults.Mandate.Aktive.alle)
  }
	test ( "Mandate-selectAktAllTerminated") {
	  val s =  Mandate.selectAktAllTerminated().transact(xa).unsafeRunSync.size
		assert(  s == TestResults.Mandate.terminated)
  }
	test ( "Mandate-TestResults") {
		assert(  alle == terminated + Aktive.alle)
  }
	
}

class TestMandateInsert extends AnyFunSuite {
  
  val xa : Transactor.Aux[IO, Unit] = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
	test( "Insert-Mandate") {
    val m = Mandate( 
        18,	
        1,	
        Timestamp.valueOf("2020-05-14 07:59:57.000000"), 
        None, 
        Date.valueOf("2020-05-14"),	
        739114, // Mandate_id, max 314828
        Some( "M18/101//314509"), 
        "1", 
        1, 
        2,	
        2, 
        1,	
        "DE84742601100000122416", 
        "GENODEF1SR2", 
        Some(Date.valueOf("2020-05-14")), 
        None, 
        None, 
        Some(Date.valueOf("2020-05-14")), 
        None, 
        Some(1),
        "",
        "101", 
        "",
        "000834635", 
        "000834635", 
        1,	
        1, 
        "000834635", 
        "000834635", 
        1,	
        Some("Straubing"), 
        None, 
        "SOAP_TEST_USER", 
        0,	
        0)
    assert( Mandate.insert(m).transact(xa).unsafeRunSync == 1)
  }
  test( "Terminate_akt") {
    val m = Mandate.terminateAkt( 739114)
    assert( m.transact(xa).unsafeRunSync.get.TERMINATED_FLAG == 1)
  }
}

class TestMandateRemove extends AnyFunSuite {
  val xa : Transactor.Aux[IO, Unit] = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  test( "Delete") {
    assert( Mandate.delete(739114).transact(xa).unsafeRunSync == 2)
  }

}

