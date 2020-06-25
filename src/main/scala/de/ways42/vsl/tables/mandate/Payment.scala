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

	  
  def selectById( payment_id : Long) : ConnectionIO[Payment] = {
    ( Fragment.const( "select " + attrStr + " from Mandate.MM_Payment") ++
      fr"where PAYMENT_ID = $payment_id"
    ).query[Payment].unique
  }
  
  def selectAllByMandateId( mandate_id : Long) : ConnectionIO[List[Payment]] = {
    ( Fragment.const( "select " + attrStr + " from Mandate.MM_Payment") ++
      fr"where mandate_id = $mandate_id"
    ).query[Payment].to[List]
  }
  
  //import doobie.util.param.Param
  //import doobie.util.param.Param.Elem
  def selectLastPaymentByMandate(mandate_id:Long) : ConnectionIO[Option[Payment]]= {
    //val p : Param[Long] = Param[Long]( mandate_id)
    //val e : Elem = Elem(mandate_id)
    //import doobie.util.pos.Pos
    ( Fragment.const( "select " + attrStr +  " from MANDATE.MM_PAYMENT p1") ++
//      Fragment(       "where mandate_id = ?", p::Nil) ++
            fr"where mandate_id = $mandate_id" ++
      Fragment.const(       "SCHEDULED_DUE_DATE = ( select max(SCHEDULED_DUE_DATE) from MANDATE.MM_PAYMENT p2 where p1.mandate_id = p2.mandate_id)")
    ).query[Payment].option
  }

  /**
   * Jüngstes Payment zur MandatsId für alle Mandate laden, falls vorhanden
   * BusinessObjectReferenceId ist nullable
   */
  def selectLastPaymentAlle() : ConnectionIO[List[Payment]] = {
    (Fragment.const( "select " + attrStr +  " from MANDATE.MM_PAYMENT p1") ++
     Fragment.const( "where SCHEDULED_DUE_DATE = ( select max(SCHEDULED_DUE_DATE) from MANDATE.MM_PAYMENT p2 where p1.mandate_id = p2.mandate_id)")
    ).query[Payment].to[List]
  }
  
  /**
   * Alle Payments laden
   */
  def selectPaymentAlle() : ConnectionIO[List[Payment]] = 
    Fragment.const( "select " + attrStr +  " from MANDATE.MM_PAYMENT").query[Payment].to[List]
}