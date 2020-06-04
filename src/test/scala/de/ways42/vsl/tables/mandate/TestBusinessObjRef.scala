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


object TestBusinessObjectRef extends AnyFunSuite {
  val xa : Transactor.Aux[IO, Unit] = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
	test ( "selectById") {
			assert( BusinessObjectRef.selectById(2229, 1).transact(xa).unsafeRunSync.get.BUSINESS_OBJ_REFERENCE_ID == 2229)
	}
	test ( "selectByMandateId") {
			assert( BusinessObjectRef.selectByMandateId(313038).transact(xa).unsafeRunSync.head.BUSINESS_OBJ_REFERENCE_ID == 313038)
  }	
}
