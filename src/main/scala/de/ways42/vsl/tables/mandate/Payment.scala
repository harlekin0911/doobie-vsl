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


case class Payment (
		MANDATOR                  : Long,
		PAYMENT_ID                : Long,
		MANDATE_ID                : Long,
		BUSINESS_OBJ_REFERENCE_ID : Option[Long],
		PAYER_EXT_REF             : String,
		PAYER_IBAN                : String,
		PAYER_BIC                 : String,
		AMOUNT                    : Double,
		CURRENCY                  : String,
		SCHEDULED_DUE_DATE        : Option[Date],
		INPAYMENT_EXT_REF         : String,
		LAST_PAYMENT_ID           : Option[Long],
		PRENOTE_ID                : Option[Long],
		PAYMENT_MODIFICATION      : Long,
		PAYMENT_MODIFICATION_DATE : Option[Date]
	)

object Payment {
	
  val attributes = Array[String] (
  	"MANDATOR",
		"PAYMENT_ID",
		"MANDATE_ID",
		"BUSINESS_OBJ_REFERENCE_ID",
		"PAYER_EXT_REF",
		"PAYER_IBAN",
		"PAYER_BIC",
		"AMOUNT",
		"CURRENCY",
		"SCHEDULED_DUE_DATE",
		"INPAYMENT_EXT_REF",
		"LAST_PAYMENT_ID",
		"PRENOTE_ID",
		"PAYMENT_MODIFICATION",
		"PAYMENT_MODIFICATION_DATE"
	)
    
  lazy val attrStr = attributes.mkString(",")
  
  val orderByScheduledDueDate : Ordering[Payment]  = 
    new Ordering[Payment] { 
      def compare(x:Payment,y:Payment): Int = x.SCHEDULED_DUE_DATE match { 
    		case Some(dx) => y.SCHEDULED_DUE_DATE match {
    		  case Some(dy) => dx compareTo dy
    		  case None     => 0
    	  }
    		case None => y.SCHEDULED_DUE_DATE match {
    		  case Some(dy) => 1
    		  case None     => 0
    		}
      }
    }

	  
  def selectById( payment_id : Long) : Query0[Payment] = {
    val s = Fragment.const( "select ")
    val a = Fragment.const( attrStr)
    val f = Fragment.const( " from Mandate.MM_Payment")
    val w = fr"where PAYMENT_ID = $payment_id"
    (s ++ a ++ f ++ w).query[Payment]
  }
  
  def selectAllByMandateId( mandate_id : Long) : Query0[Payment] = {
    val s = Fragment.const( "select ")
    val a = Fragment.const( attrStr)
    val f = Fragment.const( " from Mandate.MM_Payment")
    val w = fr"where mandate_id = $mandate_id"
    (s ++ a ++ f ++ w).query[Payment]
  }
  
  def selectLastPaymentByMandate(mandate_id:Long) = {
    val s  = Fragment.const( "select " + attrStr +  " from MANDATE.MM_PAYMENT p1")
    val w1 = Fragment(       "where mandate_id = ?", mandate_id) 
    val w2 = Fragment.const(        "SCHEDULED_DUE_DATE = ( select max(SCHEDULED_DUE_DATE) from MANDATE.MM_PAYMENT p2 where p1.mandate_id = p2.mandate_id)")
    (s ++ w1 ++ w2).query[Payment]
  }

  def selectLastPaymentAlle() = {
    (Fragment.const( "select " + attrStr +  " from MANDATE.MM_PAYMENT p1") ++
     Fragment.const( "where SCHEDULED_DUE_DATE = ( select max(SCHEDULED_DUE_DATE) from MANDATE.MM_PAYMENT p2 where p1.mandate_id = p2.mandate_id)")
    ).query[Payment]
  }

}