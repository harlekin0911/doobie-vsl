package de.ways42.vsl.domains.zik.tables

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

class TestZik extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")

	test ( "Test-ZIK-selectAktById") {
	  assert( Tzik012.selectAktById( "002110500524101", "1", 0).transact(xa).unsafeRunSync.get.Z_NKTO_NR  == "002110500524101")
  }
	
  test ( "Test-ZIK-selectAllById") {
	  assert( Tzik012.selectAllById( "002110500524101", "1", 0).transact(xa).unsafeRunSync.size  == 7)
  }
  test ( "Test-ZIK-selectAktByVtgnr") {
	  assert( Tzik012.selectAktByVtgnr( "0021105005241").transact(xa).unsafeRunSync.size  == 1)
  }
  
  test ( "Test-ZIK-selectAktByNkto") {
	  assert( Tzik012.selectAktByNkto( "002110500524101").transact(xa).unsafeRunSync.size  == 1)
  }
  

  test ( "Test-ZIK-selectNktoAktByNkartandUktoart") {
	  val z12 = Tzik012.selectNktoAktByNkartandUktoart( NonEmptyList("1", List("C")), NonEmptyList( 0, Nil)).transact(xa).unsafeRunSync
	  val l = z12.length
		assert( l == 441378)
	  
	  val ll = z12.filter ( _.Z_ZAHLART_CD != 1).length
		assert(  ll == 160115)			
  }	
}

