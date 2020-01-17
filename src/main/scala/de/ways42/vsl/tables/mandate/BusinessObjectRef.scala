package de.ways42.vsl.tables.mandate

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream
import java.sql.Timestamp
import java.sql.Date

case class BusinessObjectRef (
		MANDATOR : Long,
		HISTNR : Long,
		DOP : Timestamp,
		DOR : Option[Timestamp],
		IND : Date,
		BUSINESS_OBJ_REFERENCE_ID : Long,
		MANDATE_ID : Long,
		BUSINESS_OBJ_EXT_REF : String,
		BUSINESS_OBJ_TYPE : Long,
		MM_BU_CODE1 : String,
		MM_BU_CODE2 : String,
		MM_BO_EXT_KEY : String,
		USERID : String,
		TERMINATED_FLAG : Long
	)
	
object BusinessObjectRef {

  val attributes = Array[String] (
		"MANDATOR",
		"HISTNR",
		"DOP",
		"DOR",
		"IND",
		"BUSINESS_OBJ_REFERENCE_ID",
		"MANDATE_ID",
		"BUSINESS_OBJ_EXT_REF",
		"BUSINESS_OBJ_TYPE",
		"MM_BU_CODE1",
		"MM_BU_CODE2",
		"MM_BO_EXT_KEY",
		"USERID",
		"TERMINATED_FLAG"
	)
    
  lazy val attrStr = attributes.mkString(",")
	  
  def selectById( bor_id : Long, hnr : Long) : Query0[BusinessObjectRef] = {
    val s = Fragment.const( "select ")
    val a = Fragment.const( attrStr)
    val f = Fragment.const( " from Mandate.MM_Business_Object_Reference")
    val w = fr"where BUSINESS_OBJ_REFERENCE_ID = $bor_id and HISTNR = $hnr"
    (s ++ a ++ f ++ w).query[BusinessObjectRef]
  }

  def selectByMandateId( mandate_id : Long) : Query0[BusinessObjectRef] = {
    val s = Fragment.const( "select ")
    val a = Fragment.const( attrStr)
    val f = Fragment.const( " from Mandate.MM_Business_Object_Reference")
    val w = fr"where MANDATE_ID = $mandate_id"
    (s ++ a ++ f ++ w).query[BusinessObjectRef]
  }

}