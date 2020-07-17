package de.ways42.vsl.domains.mandate.service


import org.scalatest.funsuite.AnyFunSuite 

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import de.ways42.vsl.connection.Connect
import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.Mandate
import de.ways42.vsl.domains.mandate.domain.MandateAktDom
import de.ways42.vsl.domains.mandate.domain.MandateAktDom._
import de.ways42.vsl.domains.vsl.service.VslService


class TestVslService  extends AnyFunSuite  { 
  
  val xa = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01:allowNextOnExhaustedResultSet=1;", "VSMADM", "together")
  
  
  test( "VS-GetSingleVertrag-beides") {
    val (o1,l2) = VslService.getVertragWithVersicherung( "0003065903411")
    val a = o1.transact(xa).unsafeRunSync()
    val b = l2.transact(xa).unsafeRunSync()
    assert(  a.isEmpty == false && b.size == 4)

 	}
  test( "VS-GetSingleVertrag-1-opt-vertrag") {
    val (o1,l2) = VslService.getVertragWithVersicherung( "1234567890123")
    val a = o1.transact(xa).unsafeRunSync()
    assert(  a.isEmpty == true )

 	}
  test( "VS-GetSingleVertrag-2-list-vers") {
    val (o1,l2) = VslService.getVertragWithVersicherung( "1234567890123")
    val b = l2.transact(xa).unsafeRunSync()
    assert(  b.size == 0)

 	}
}
