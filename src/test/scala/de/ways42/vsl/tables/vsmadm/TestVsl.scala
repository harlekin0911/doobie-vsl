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
import de.ways42.vsl.Connect

class TestVsl extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( "VSMADM", "together")

	test( "Vsl") {
			tvsl001(   xa)
			tvsl001_2( xa)
	}
	
	test( "Vsl-Vtgnr") {
			Tvsl001.selectAll().stream.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).foreach(println)
			Tvsl001.selectAktById(   "0003065903411").to[List].transact(xa).unsafeRunSync.foreach(println)
			println ( "Anzahl aktiver Vertraege: " + Tvsl001.selectAktAktive().to[List].transact(xa).unsafeRunSync.length)
			println ( "Anzahl bpfl.   Vertraege: " + Tvsl001.selectAktBeitragspflichtig().to[List].transact(xa).unsafeRunSync.length)
			
			Tvsl002.selectVtgnr( "0003065903411").to[List].transact(xa).unsafeRunSync.foreach(println)

  }

  test ( "Vsl-Rolle") {
			Trol001.selectById( "0050034703671", "", 89, 1).to[List].transact(xa).unsafeRunSync.foreach(println)
  }
  
  	

	def tvsl001(xa : Transactor.Aux[IO, Unit]) = {
			//val q = Query.( "select * from VSMADM.TVSL001")
			sql"select * from VSMADM.TVSL001".query[(String,String, String)].stream.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).foreach(println)
	}

	def tvsl001_2( xa : Transactor.Aux[IO, Unit]) = {
			val proc = HC.stream[(String, String, String, String, String, String)](
					"select * from VSMADM.TVSL001",    // statement
					().pure[PreparedStatementIO],      // prep (none)
					512                                // chunk size
					)
					proc.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).foreach(println)
	}	
}

