package de.ways42.vsl.tables.mandate

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

class TestMandate extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = Connect( "VSMADM", "together")

	
  test ( "Mandate-selectAktById") {
			assert( Mandate.selectAktById(313038).transact(xa).unsafeRunSync.MANDATE_ID == 313038)
	}
  test ( "Mandate-selectAllById") {
			assert( Mandate.selectAllById(313038).transact(xa).unsafeRunSync.size == 1)
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

