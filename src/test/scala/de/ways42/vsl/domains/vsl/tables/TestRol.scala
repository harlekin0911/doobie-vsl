package de.ways42.vsl.domains.vsl.tables

//import de.ways42.vsl.tables.Tables.TVSL001

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream

import java.time.LocalDate
import java.time.LocalDate
import java.sql.Date

import org.scalatest.funsuite.AnyFunSuite //TestSuite
import de.ways42.vsl.connection.Connect
import de.ways42.vsl.TestResults

class TestRol extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( 
	    "com.ibm.db2.jcc.DB2Driver", 
	    //"jdbc:db2://172.17.4.39:50001/vslt01:driverType=4;fullyMaterializeLobData=true;fullyMaterializeInputStreams=true;progressiveStreaming=2;progresssiveLocators=2;"
	    "jdbc:db2://172.17.4.39:50001/vslt01",
	    "VSMADM", 
	    "together")

  test ( "Vsl-Rolle-selectById") {
			assert( Trol001.selectById( "0050034703671", "", 89, 1).transact(xa).unsafeRunSync.length == 2 )
  }
	
  test ( "Vsl-Rolle-selectAktById") {
			assert( Trol001.selectAktById( "0050034703671", "", 89, 1).transact(xa).unsafeRunSync.get.ISTTOP_NRX.trim == "0050034703671" )
  } 
    
  test ( "Vsl-Rolle-selectAktById-all-rang") {
			assert( Trol001.selectAktById( "0050034703671",  89).transact(xa).unsafeRunSync.size == 2 )
  }  
  test ( "Vsl-Rolle-selectAktById-1") {
			assert( Trol001.selectAktById( "0070010919631", "", 89, 1).transact(xa).unsafeRunSync.isEmpty == false )
  } 
  test ( "Vsl-Rolle-selectAktById-2") {
			assert( Trol001.selectAktById( "0050013907811", "", 89, 1).transact(xa).unsafeRunSync.isEmpty == false )
  } 
  test ( "Vsl-Rolle-selectAktById-all-rang-3") {
			assert( Trol001.selectAktById( "0070010919631",  89).transact(xa).unsafeRunSync.size == 3 )
  }  
  test ( "Vsl-Rolle-selectAktById-Empty") {
			assert( Trol001.selectAktById( "1234567890123", "", 89, 1).transact(xa).unsafeRunSync.isEmpty == true )
  }  
  test ( "Vsl-Rolle-selectAktAll") {
    val s = Trol001.selectAktAll( 89).transact(xa).unsafeRunSync.size
			assert( s == TestResults.Rolle.all )
  }  
  test ( "Vsl-Rolle-selectAktAllAktive") {
    val s = Trol001.selectAktAllAktive( 89).transact(xa).unsafeRunSync.size
			assert( s == TestResults.Rolle.aktive )
  }  
  
}

class TestRolInsert extends AnyFunSuite {
  
	val xa : Transactor.Aux[IO, Unit] = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")

	test( "Insert-Rolle") {
    val t = Trol001( "0000000000001", "", 89, 1, 20200307, 113301, 20200305, "001250372", "001001000000000000000000", "0901" , 2, "DEE00000499276;0")
    assert( Trol001.insert(t).transact(xa).unsafeRunSync == 1)
  }
  test( "Terminate_akt") {
    val t = Trol001.terminateAkt( "0000000000001", "", 89, 1)
    assert( t.transact(xa).unsafeRunSync.get.RSTAT_CD == 2)
  }
}

class TestRolRemove extends AnyFunSuite {
	
  val xa : Transactor.Aux[IO, Unit] = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")

	test ( "Delete-Rolle") {
			assert( Trol001.delete( "0000000000001", "", 89, 1).transact(xa).unsafeRunSync == 2 )
  }  
}

