package de.ways42.vsl.tables.vsmadm

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

	val xa : Transactor.Aux[IO, Unit] = Connect( "VSMADM", "together")

	test( "Vsl-Select-Basic-1") {
		val q = Query( "select * from VSMADM.TVSL001")
		assert( sql"select * from VSMADM.TVSL001".query[(String,String, String)].stream.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).length == 5)
	}
	
  test( "Vsl-Select-Basic-2") {

	  val proc = HC.stream[(String, String, String, String, String, String)](
			"select * from VSMADM.TVSL001",    // statement
			().pure[PreparedStatementIO],      // prep (none)
			512                                // chunk size
			)
			
		assert( proc.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).length == 5)

  }
	
	test( "Vsl-Vtgnr") {
			assert ( Tvsl001.selectAllMaxCount( 5).transact(xa).unsafeRunSync.take(5).length == 5)
			assert ( Tvsl001.selectAktById(   "0003065903411").transact(xa).unsafeRunSync.get.LV_VTG_NR.trim() == "0003065903411")
			val c : Long = Tvsl001.selectAktAllAktive().transact(xa).unsafeRunSync.length
			assert ( c == 248017)
			val d : Long = Tvsl001.selectAktAllBeitragspflichtig().transact(xa).unsafeRunSync.length
			assert (  d == 173677)
	}
	test( "VSL-tvsl002") {
			assert ( Tvsl002.selectVtgnr( "0003065903411").transact(xa).unsafeRunSync.length == 6)
  }

  test ( "Vsl-Rolle-selectById") {
			assert( Trol001.selectById( "0050034703671", "", 89, 1).transact(xa).unsafeRunSync.length == 1 )
  }
	
  test ( "Vsl-Rolle-selectAktById") {
			assert( Trol001.selectAktById( "0050034703671", "", 89, 1).transact(xa).unsafeRunSync.get.ISTTOP_NRX.trim == "0050034703671" )
  }  
}

