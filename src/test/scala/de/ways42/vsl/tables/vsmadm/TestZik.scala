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

class TestZik extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( "VSMADM", "together")

	test ( "Test-ZIK-selectAktById") {
	  assert( Tzik012.selectAktById( "002110500524101", "1", 0).option.transact(xa).unsafeRunSync.get.Z_NKTO_NR  == "002110500524101")
  }
	
  test ( "Test-ZIK-selectAllById") {
	  assert( Tzik012.selectAllById( "002110500524101", "1", 0).to[List].transact(xa).unsafeRunSync.head.Z_NKTO_NR  == "002110500524101")
  }

  test ( "Test-ZIK-selectNktoAktByNkartandUktoart") {
	  val z12 = Tzik012.selectNktoAktByNkartandUktoart( NonEmptyList("1", List("C")), NonEmptyList( 0, Nil)).to[List].transact(xa).unsafeRunSync
	  val l = z12.length
		assert( l == 441340)
	  
	  val ll = z12.filter ( _.Z_ZAHLART_CD != 1).length
		assert(  ll == 158386)			
  }	
}

