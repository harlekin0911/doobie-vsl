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
import doobie.implicits.javasql._
import doobie.implicits.javatime._

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
  
  lazy val maxHistNr = Fragment.const( "histnr = (select max(histnr) from mandate.mm_business_object_reference bor2 where bor1.business_obj_reference_id = bor2.business_obj_reference_id)")
  
  def maxInd( field:String) = Fragment.const( "ind = (select max(ind) from mandate.mm_business_object_reference bor2 where bor1." + field + " = bor2." + field + ")")
                                        
	def selectAll() : ConnectionIO[List[BusinessObjectRef]] = {
    Fragment.const( "select " + attrStr + " from Mandate.MM_Business_Object_Reference where business_obj_reference_id order by business_obj_reference_id asc").
    query[BusinessObjectRef].to[List]
  }
  def selectAktAll() : ConnectionIO[List[BusinessObjectRef]] = {
    ( Fragment.const( "select " + attrStr + " from Mandate.MM_Business_Object_Reference b1") ++
      Fragment.const( "where  histnr = (select max(histnr) from mandate.MM_Business_Object_Reference b2" + 
                                        " where b1.business_obj_reference_id = b2.business_obj_reference_id)")
    ).query[BusinessObjectRef].to[List]
  }

  def selectById( bor_id : Long, hnr : Long) : ConnectionIO[Option[BusinessObjectRef]] = {
    (Fragment.const( "select " + attrStr + " from Mandate.MM_Business_Object_Reference ") ++
     fr"where business_obj_reference_id = $bor_id and histnr = $hnr"
    ).query[BusinessObjectRef].option
  }
  
  def selectAktById( bor_id : Long) : ConnectionIO[Option[BusinessObjectRef]] = {
    (Fragment.const( "select " + attrStr + " from Mandate.mm_business_object_reference bor1 ") ++
     Fragments.whereAnd( fr"business_obj_reference_id = $bor_id", maxHistNr)
    ).query[BusinessObjectRef].option
  }

  def selectByMandateId( mandate_id : Long) : ConnectionIO[List[BusinessObjectRef]] = {
    (Fragment.const( "select "+ attrStr + " from mandate.mm_business_object_reference ") ++
     fr"where mandate_id = $mandate_id order by ind desc"
    ).query[BusinessObjectRef].to[List]
  }

  def selectAktByMandateId( mandate_id : Long) : ConnectionIO[Option[BusinessObjectRef]] = {
    (Fragment.const(       "select "+ attrStr + " from mandate.mm_business_object_reference bor1 ") ++
     Fragments.whereAnd( fr"mandate_id = $mandate_id", maxInd( "mandate_id"))
    ).query[BusinessObjectRef].option
  }
  def selectByBusinessObjExtRef( businessObjExtRef : String) : ConnectionIO[List[BusinessObjectRef]] = {
    (Fragment.const( "select "+ attrStr + " from Mandate.MM_Business_Object_Reference ") ++
     fr"where BUSINESS_OBJ_EXT_REF = $businessObjExtRef  order by ind desc"
    ).query[BusinessObjectRef].to[List]
  }

  def selectAktByBusinessObjExtRef( businessObjExtRef : String) : ConnectionIO[Option[BusinessObjectRef]] = {
    (Fragment.const( "select "+ attrStr + " from Mandate.MM_Business_Object_Reference bor1 ") ++
     Fragments.whereAnd( fr"BUSINESS_OBJ_EXT_REF = $businessObjExtRef", maxInd( "business_obj_ext_ref"))
    ).query[BusinessObjectRef].option
  }

}