package de.ways42.vsl.tables.mandate

//import de.ways42.vsl.tables.Tables.TVSL001



import org.scalatest.funsuite.AnyFunSuite

import cats._
import cats.effect._
//TestSuite
import de.ways42.vsl.connection.Connect
import doobie._
import doobie.implicits._
import java.sql.Timestamp
import java.sql.Date


class TestMandate extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( "VSMADM", "together")

	
  test ( "Mandate-selectAktById") {
			assert( Mandate.selectAktById(313038).transact(xa).unsafeRunSync.get.MANDATE_ID == 313038)
	}
  test ( "Mandate-selectAllById") {
			assert( Mandate.selectAllById(313038).transact(xa).unsafeRunSync.size == 2)
  }
  
  test ( "Mandate-selectAktAll") {
			assert( Mandate.selectAktAll().transact(xa).unsafeRunSync.length > 250000)
  }
	test ( "Payment") {
		assert ( Payment.selectById(2229).transact(xa).unsafeRunSync.BUSINESS_OBJ_REFERENCE_ID.get == 2229 )
	}
	
	test ( "BusinessObjectRef") {
			assert( BusinessObjectRef.selectById(2229, 1).transact(xa).unsafeRunSync.get.BUSINESS_OBJ_REFERENCE_ID == 2229)
			assert( BusinessObjectRef.selectByMandateId(313038).transact(xa).unsafeRunSync.head.BUSINESS_OBJ_REFERENCE_ID == 313038)
  }
}
class TestMandateInsert extends AnyFunSuite {
  
  val xa : Transactor.Aux[IO, Unit] = Connect( "VSMADM", "together")
  
	test( "Insert-Mandate") {
    val m = Mandate( 
        18,	
        1,	
        Timestamp.valueOf("2020-05-14 07:59:57.000000"), 
        None, 
        Date.valueOf("2020-05-14"),	
        1, // Mandate_id	
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
    val m = Mandate.terminateAkt( 1)
    assert( m.transact(xa).unsafeRunSync.get.TERMINATED_FLAG == 1)
  }
}

class TestMandateDelete extends AnyFunSuite {
  val xa : Transactor.Aux[IO, Unit] = Connect( "VSMADM", "together")
  test( "Delete") {
    assert( Mandate.delete(1).transact(xa).unsafeRunSync == 2)
  }

}

