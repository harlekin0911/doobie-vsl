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


class TestBusinessObjectRef extends AnyFunSuite {
  val xa : Transactor.Aux[IO, Unit] = Connect( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "VSMADM", "together")
  
	test ( "selectById") {
			assert( BusinessObjectRef.selectById(2229, 1).transact(xa).unsafeRunSync.get.BUSINESS_OBJ_REFERENCE_ID == 2229)
	}
	test ( "selectByMandateId") {
			assert( BusinessObjectRef.selectByMandateId(313038).transact(xa).unsafeRunSync.head.BUSINESS_OBJ_REFERENCE_ID == 313038)
  }	
	test ( "selectByMandateIdAkt") {
			assert( BusinessObjectRef.selectAktByMandateId(313038).transact(xa).unsafeRunSync.size == 1 )
  }	
	test ( "check-multiple-mandate_id-in-business-object-ref") {
	  val lbor : List[BusinessObjectRef]= BusinessObjectRef.selectAll.transact(xa).unsafeRunSync()
	  val e:Map[Long,List[BusinessObjectRef]] = lbor.groupBy(_.MANDATE_ID)
	  val twoOrMore =e.filter( _._2.groupBy(_.BUSINESS_OBJ_REFERENCE_ID).size > 1)
	  assert( twoOrMore.size == 0)
//	  //e.foldLeft(0)((acc,t) => 
//	  e.foreach( x => println( "size=" + x._1 + " Anzahl= " + x._2.size ))
//	  //e.get(3).map( _.foreach( x => println( "mandate_id=" + x._1)))
//	  val f = e.map( e.get(_).map( _.filter( _._2.groupBy(_.BUSINESS_OBJ_REFERENCE_ID).size > 1)))   
//	  f.foreach( _.foreach( _.foreach(x => println( "diff borid mandate_id=" + x._1))))
//		assert( e.get(3).map(_.size).getOrElse(0) > 1 )
  }
	test ( "Check-multiple-mandate-for-object-ext-ref") {
	  val lbor : List[BusinessObjectRef]= BusinessObjectRef.selectAll.transact(xa).unsafeRunSync()
	  val e:Map[String,List[BusinessObjectRef]] = lbor.groupBy(_.BUSINESS_OBJ_EXT_REF)
	  val twoOrMore =e.filter( _._2.groupBy(_.MANDATE_ID).size > 1)
	  assert( twoOrMore.size == 7404)  
	}
	test ( "Check-multiple-business-obj-ref-id-for-object-ext-ref") {
	  val lbor : List[BusinessObjectRef]= BusinessObjectRef.selectAll.transact(xa).unsafeRunSync()
	  val e:Map[String,List[BusinessObjectRef]] = lbor.groupBy(_.BUSINESS_OBJ_EXT_REF)
	  val twoOrMore =e.filter( _._2.groupBy(_.BUSINESS_OBJ_REFERENCE_ID).size > 1)
	  assert( twoOrMore.size == 7404)  
	}
}
