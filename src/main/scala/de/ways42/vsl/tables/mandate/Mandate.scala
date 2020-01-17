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

object Mandate {
  
  case class Table (
      
    MANDATOR             : Long, 
    HISTNR               : Long, 
    DOP                  : Timestamp, 
    DOR                  : Option[Timestamp], 
    IND                  : Date,   
    MANDATE_ID           : Long, // BIGINT 
    MANDATE_EXT_REF      : Option[String],
    CREDITOR_ID          : String,
    MANDATE_STATUS       : Long, 
    MANDATE_TYPE         : Long, 
    DIRECT_DEBIT_TYPE    : Long, 
    PRENOTE_DAYS         : Long, 
    PAYER_IBAN           : String,
    PAYER_BIC            : String,
    FIRST_DEBIT_DATE     : Option[Date],
    LAST_DEBIT_DATE      : Option[Date],
    DOC_EXT_REF          : Option[String],
    SIGNED_DATE          : Option[Date],
    REASON_FOR_AMENDMENT : Option[Long], 
    REASON_FOR_CHANGE    : Option[Long], 
    NOTICE               : String,
    MM_BU_CODE1          : String,
    MM_BU_CODE2          : String,
    ACC_HOLDER_EXT_REF   : String,
    ACC_HOLDER_EXT_KEY   : String,
    ACC_HOLDER_BANK_NR   : Long, 
    ACC_HOLDER_ADRESS_NR : Long, 
    SIGNATORY_EXT_REF    : String,
    SIGNATORY_EXT_KEY    : String,
    SIGNATORY_ADRESS_NR  : Long, 
    SIGNED_ADDRESS       : Option[String],
    CANCEL_SIGNED_DATE   : Option[Date],
    USERID               : String,
    TERMINATED_FLAG      : Long, 
    DEPOSIT_LOCATION     : Long      
    )
  
  val attributes = Array[String] (
		  "MANDATOR", 
		  "HISTNR", 
		  "DOP", 
		  "DOR", 
		  "IND",   
		  "MANDATE_ID", // BIGINT 
		  "MANDATE_EXT_REF",
		  "CREDITOR_ID",
		  "MANDATE_STATUS", 
		  "MANDATE_TYPE", 
		  "DIRECT_DEBIT_TYPE", 
		  "PRENOTE_DAYS", 
		  "PAYER_IBAN",
		  "PAYER_BIC",
		  "FIRST_DEBIT_DATE",
		  "LAST_DEBIT_DATE",
		  "DOC_EXT_REF",
		  "SIGNED_DATE",
		  "REASON_FOR_AMENDMENT", 
		  "REASON_FOR_CHANGE", 
		  "NOTICE",
		  "MM_BU_CODE1",
		  "MM_BU_CODE2",
		  "ACC_HOLDER_EXT_REF",
		  "ACC_HOLDER_EXT_KEY",
		  "ACC_HOLDER_BANK_NR", 
		  "ACC_HOLDER_ADRESS_NR", 
		  "SIGNATORY_EXT_REF",
		  "SIGNATORY_EXT_KEY",
		  "SIGNATORY_ADRESS_NR", 
		  "SIGNED_ADDRESS",
		  "CANCEL_SIGNED_DATE",
		  "USERID",
		  "TERMINATED_FLAG", 
  "DEPOSIT_LOCATION" )
  
  lazy val attrStr = attributes.mkString(",")

  def selectAkt( mandate_id : Long) : Query0[Table] = {
    val s = Fragment.const( "select ")
    val a = Fragment.const( attrStr)
    val f = Fragment.const( " from Mandate.MM_Mandate")
    val w = fr"where MANDATE_ID = $mandate_id"
    (s ++ a ++ f ++ w).query[Table]

  }

}