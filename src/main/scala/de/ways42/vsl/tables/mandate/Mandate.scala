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
import de.ways42.vsl.service.TimeService


  
case class Mandate (
      
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
    ) {
  
  /**
   * Mandate terminieren
   */
  def setTerminated() : Mandate = {
    val dop = TimeService.getTimestamp()
    copy( HISTNR = HISTNR + 1, DOP = dop,  IND = new Date(dop.getTime()), TERMINATED_FLAG = 1)
  }
  
  /**
   * Mandate auf abgelaufen setzen
   */
  def setAbgelaufen() : Mandate = {
    val dop = TimeService.getTimestamp()
    copy( HISTNR = HISTNR + 1, DOP = dop,  IND = new Date(dop.getTime()), REASON_FOR_CHANGE = 3L.some, MANDATE_STATUS = 5,  TERMINATED_FLAG = 1)
  }

}

  object Mandate {
    
  
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
  
    /**
   * Insert  the entry
   */
  def insert( r1:Mandate) : ConnectionIO[Int] = 
    Update[Mandate]("insert into Mandate.MM_Mandate values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").run(r1) 

  def delete( mandate_id:Long) : ConnectionIO[Int] = 
    Update[Long]("delete from Mandate.MM_Mandate where mandate_id = ?").run(mandate_id) 

  def selectAktById( mandate_id : Long) : ConnectionIO[Option[Mandate]] = {
    (Fragment.const( "select " + attrStr + " from Mandate.MM_Mandate m1") ++ 
     fr"where MANDATE_ID = $mandate_id and histnr = (select max(histnr) from mandate.mm_mandate m2 where m1.mandate_id = m2.mandate_id)"
    ).query[Mandate].option
  }
    
  def selectAllById( mandate_id : Long) : ConnectionIO[List[Mandate]] = {
    (Fragment.const( "select " + attrStr + " from Mandate.MM_Mandate m1") ++
     fr"where MANDATE_ID = $mandate_id order by histnr desc"
    ).query[Mandate].to[List]
  }

    
  def selectAktAll() : ConnectionIO[List[Mandate]] = {
    ( Fragment.const( "select " + attrStr + " from Mandate.MM_Mandate m1") ++
      Fragment.const( "where  histnr = (select max(histnr) from mandate.mm_mandate m2 where m1.mandate_id = m2.mandate_id)")
    ).query[Mandate].to[List]
  }

  def selectAktAllNotTerminated() : ConnectionIO[List[Mandate]] = {
    ( Fragment.const( "select " + attrStr + " from Mandate.MM_Mandate m1") ++
      Fragment.const( "where  histnr = (select max(histnr) from mandate.mm_mandate m2 where m1.mandate_id = m2.mandate_id) and m1.terminated_flag = 0")
    ).query[Mandate].to[List]
  }
  def selectAktAllTerminated() : ConnectionIO[List[Mandate]] = {
    ( Fragment.const( "select " + attrStr + " from Mandate.MM_Mandate m1") ++
      Fragment.const( "where  histnr = (select max(histnr) from mandate.mm_mandate m2 where m1.mandate_id = m2.mandate_id) and m1.terminated_flag = 1")
    ).query[Mandate].to[List]
  }
  
  /**
   * Nur das Mandate terminieren, nicht die Verbindung zum BusinessObjectRef
   */
  def terminateAkt( mandate_id : Long) : ConnectionIO[Option[Mandate]] = {
        selectAktById( mandate_id).flatMap({
      case Some(t1) => insert( t1.setTerminated()).flatMap( _ => selectAktById( mandate_id))
      case None     => Option.empty[Mandate].pure[ConnectionIO]
    })
  }

  /**
   * Mandate in der DB auf abgelaufen setzen 
   */
  def outDateAkt( mandate_id : Long) : ConnectionIO[Option[Mandate]] = {
        selectAktById( mandate_id).flatMap({
      case Some(t1) => insert( t1.setAbgelaufen()).flatMap( _ => selectAktById( mandate_id))
      case None     => Option.empty[Mandate].pure[ConnectionIO]
    })
  }
}