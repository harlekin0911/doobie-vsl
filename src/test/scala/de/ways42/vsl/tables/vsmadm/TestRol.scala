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

class TestRol extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( "VSMADM", "together")

  test ( "Vsl-Rolle-selectById") {
			assert( Trol001.selectById( "0050034703671", "", 89, 1).transact(xa).unsafeRunSync.length == 2 )
  }
	
  test ( "Vsl-Rolle-selectAktById") {
			assert( Trol001.selectAktById( "0050034703671", "", 89, 1).transact(xa).unsafeRunSync.get.ISTTOP_NRX.trim == "0050034703671" )
  }  
  
  test( "Insert-Rolle") {
    val t = Trol001( "0000000000001", "", 89, 1, 20200307, 113301, 20200305, "001250372", "001001000000000000000000", "0901" , 2, "DEE00000499276;0")
    assert( Trol001.insert(t).transact(xa).unsafeRunSync == 1)
  }
  test( "Terminate_akt") {
    val t = Trol001.terminateAkt( "0000000000001", "", 89, 1)
    assert( t.transact(xa).unsafeRunSync.get.RSTAT_CD == 2)
  }
  test ( "Vsl-Rolle-selectAktById") {
			assert( Trol001.delete( "0000000000001", "", 89, 1).transact(xa).unsafeRunSync == 1 )
  }  
}

