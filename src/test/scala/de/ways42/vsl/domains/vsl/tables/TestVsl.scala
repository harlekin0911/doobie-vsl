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

class TestVsl extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( 
	    "com.ibm.db2.jcc.DB2Driver", 
	    "jdbc:db2://172.17.4.39:50001/vslt01", 
	    "VSMADM", 
	    "together")

	test( "Vsl-Select-Basic-1") {
		val q = Query( "select * from VSMADM.TVSL001")
		assert( sql"select * from VSMADM.TVSL001".query[(String,String, String)].stream.take(5).compile.toList.transact(xa).unsafeRunSync.take(5).length == 5)
	}
	
  test( "Vsl-Select-Basic-2") {

	  val proc = HC.stream[(String, String, String, String, String, String)](
			"select * from VSMADM.TVSL001",    // statement
			().pure[PreparedStatementIO],      // prep (none)
			512                                // chunk size
			)
			
		assert( proc.take(5).compile.toList.transact(xa).unsafeRunSync.take(5).length == 5)

  }
	
	test( "Vsl-Vtgnr-AKT-aktive") {
			assert ( Tvsl001.selectAllMaxCount( 5).transact(xa).unsafeRunSync.take(5).length == 5)
			assert ( Tvsl001.selectAktById(   "0003065903411").transact(xa).unsafeRunSync.get.LV_VTG_NR.trim() == "0003065903411")
			val c : Long = Tvsl001.selectAktAllAktive().transact(xa).unsafeRunSync.length
			assert ( c == 239743)
	}
	test( "Vsl-Vtgnr-AKT-empty") {
			assert ( Tvsl001.selectAktById(   "1234567890123").transact(xa).unsafeRunSync.isEmpty == true)
	}
	test( "Vsl-Vtgnr-AKT-beitragspflichtige") {
			val d : Long = Tvsl001.selectAktAllBeitragspflichtig().transact(xa).unsafeRunSync.length
			assert (  d == 167150)
	}
}

class TestTvsl002 extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( 
	    "com.ibm.db2.jcc.DB2Driver", 
	    "jdbc:db2://172.17.4.39:50001/vslt01", 
	    "VSMADM", 
	    "together")

	test( "VSL-tvsl002-vtg-alle") {
			assert ( Tvsl002.selectVtgnr( "0003065903411").transact(xa).unsafeRunSync.length == 6)
  }
	test( "VSL-tvsl002-vtg-akt") {
			assert ( Tvsl002.selectAktZuVertrag("0003065903411").transact(xa).unsafeRunSync.length == 2)
  }
	test( "VSL-tvsl003-akt-empty") {
			assert ( Tvsl002.selectAktZuVertrag("1234567890123").transact(xa).unsafeRunSync.length == 0)
  }
}

