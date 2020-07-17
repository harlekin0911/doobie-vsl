package de.ways42.vsl.domains.vsl.tables

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
import java.util.GregorianCalendar
import java.util.Calendar
import de.ways42.vsl.service.TimeService

/**
 * Primary Key: 
 *   ISTTOP_NRX, type=CHARACTER, pos=0
 *   ISTKOMP_NR, type=CHARACTER, pos=1  leer ( '')  bei rolle 89 Mandat
 *   ROLLEN_CD, type=SMALLINT, pos=2
 *   RANG_NR, type=SMALLINT, pos=3
 *   GV_DTM, type=INTEGER, pos=6
 *   VA_DTM, type=INTEGER, pos=4
 *   DF_ZT, type=INTEGER, pos=5
 */
case class Trol001 (

		ISTTOP_NRX   : String,
		ISTKOMP_NR   : String,
		ROLLEN_CD    : Int,
		RANG_NR      : Int,
		VA_DTM       : Int,
		DF_ZT        : Int,
		GV_DTM       : Int,
		HATTOP_NRX   : String,
		HATKOMP_NR   : String,
		MODGRD_CD    : String,
		RSTAT_CD     : Int,
		ROLLSPEZ_TXT : String
) {
  def setTerminated() = {
    val vadf = TimeService.vadf()
    copy( VA_DTM = vadf._1, DF_ZT = vadf._2, GV_DTM = vadf._1, RSTAT_CD = 2)
  }
}

object Trol001 {

  val unit = Trol001( "", "" , 0, 0, 0, 0, 0 , "", "" ,"",0,"")

  val attributes = Array[String] (
		"ISTTOP_NRX",
		"ISTKOMP_NR",
		"ROLLEN_CD",
		"RANG_NR",
		"VA_DTM",
		"DF_ZT",
		"GV_DTM",
		"HATTOP_NRX",
		"HATKOMP_NR",
		"MODGRD_CD",
		"RSTAT_CD",
		"ROLLSPEZ_TXT"
	)
    
  lazy val attrStr = attributes.mkString(",")
	  
  /**
   * Alle Mandatsrollen zu einem Vertrag, Rolle und Rang holen
   */
  def selectById( top:String, komp:String, roll:Int, rang:Int) : ConnectionIO[List[Trol001]] = {
    ( Fragment.const( "select "+ attrStr + " from vsmadm.trol001") ++
      fr"where isttop_nrx = $top and istkomp_nr = $komp and rollen_cd = $roll and rang_nr = $rang"
    ).query[Trol001].to[List]
  }
  /**
   * Alle Mandatsrollen zu einem Vertrag, Rolle und Rang holen
   */
  def selectById( top : String, roll : Int) : ConnectionIO[List[Trol001]] = {
    ( Fragment.const( "select "+ attrStr + " from vsmadm.trol001") ++
      fr"where isttop_nrx = $top and rollen_cd = $roll"
    ).query[Trol001].to[List]
  }
  
  /**
   * Aktuelle Mandatsrolle zu einem Vertrag und Rang selektieren, falls vorhanden 
   * 
   * .option statt to[List] funktoniert nicht, liefert bei leer Exception: 
   * com.ibm.db2.jcc.am.SqlException: [jcc][t4][10120][10898][4.13.127] Ungueltige Operation: result set ist geschlossen. ERRORCODE=-4470, SQLSTATE=null
   *
   * Workaraoud:
   * to[List].map{ case h::Nil => Some(h) ; case h::h2::t => throw new RuntimeException( "Aktuelle Rolle nicht eindeutig"); case _ => None}
   * 
   * hilft:
   * "jdbc:db2://172.17.4.39:50001/vslt01:allowNextOnExhaustedResultSet=1;",
   */
  def selectAktById( top:String, komp:String, roll:Int, rang:Int) : ConnectionIO[Option[Trol001]] = {
    ( Fragment.const( "select "+ attrStr + " from vsmadm.trol001 r1") ++
      Fragments.whereAnd(           
          fr"r1.isttop_nrx = $top and r1.istkomp_nr = $komp and r1.rollen_cd = $roll and r1.rang_nr = $rang",
          Fragment.const( "va_dtm = (select max(va_dtm) from vsmadm.trol001 r2 " + 
                                      "where r1.isttop_nrx = r2.isttop_nrx and r1.istkomp_nr = r2.istkomp_nr and r1.rollen_cd = r2.rollen_cd and r1.rang_nr = r2.rang_nr)"),
          Fragment.const( "df_zt  = (select max(df_zt)  from vsmadm.trol001 r3 " + 
                                      "where r1.isttop_nrx = r3.isttop_nrx and r1.istkomp_nr = r3.istkomp_nr and r1.rollen_cd = r3.rollen_cd and r1.rang_nr = r3.rang_nr and r1.va_dtm = r3.va_dtm)")
    )).query[Trol001].option

  }
  
  /**
   * Aktuelle Mandatsrolle zu einem Vertrag selektieren, falls vorhanden 
   */
  def selectAktById( top:String, roll:Int) : ConnectionIO[List[Trol001]] = {
    ( Fragment.const( "select "+ attrStr + " from vsmadm.trol001 r1") ++
      Fragments.whereAnd(           
          fr"r1.isttop_nrx = $top and r1.rollen_cd = $roll",
          Fragment.const( "va_dtm = " + 
              "(select max(va_dtm) from vsmadm.trol001 r2 " + 
                  "where r1.isttop_nrx = r2.isttop_nrx and r1.istkomp_nr = r2.istkomp_nr and r1.rollen_cd = r2.rollen_cd and r1.rang_nr = r2.rang_nr)"),
          Fragment.const( "df_zt  = " + 
              "(select max(df_zt)  from vsmadm.trol001 r3 " + 
                   "where r1.isttop_nrx = r3.isttop_nrx and r1.istkomp_nr = r3.istkomp_nr and r1.rollen_cd = r3.rollen_cd and r1.rang_nr = r3.rang_nr and r1.va_dtm = r3.va_dtm)")
    )).query[Trol001].to[List]
  }

  /**
   * Aktuelle Mandatsrolle zu einem Vertrag selektieren, falls vorhanden 
   */
  def selectAktAll( roll:Int) : ConnectionIO[List[Trol001]] = {
    ( Fragment.const( "select "+ attrStr + " from vsmadm.trol001 r1") ++
      Fragments.whereAnd(           
          fr"r1.rollen_cd = $roll",
          Fragment.const( "va_dtm = " + 
              "(select max(va_dtm) from vsmadm.trol001 r2 " + 
                  "where r1.isttop_nrx = r2.isttop_nrx and r1.istkomp_nr = r2.istkomp_nr and r1.rollen_cd = r2.rollen_cd and r1.rang_nr = r2.rang_nr)"),
          Fragment.const( "df_zt  = " + 
              "(select max(df_zt)  from vsmadm.trol001 r3 " + 
                   "where r1.isttop_nrx = r3.isttop_nrx and r1.istkomp_nr = r3.istkomp_nr and r1.rollen_cd = r3.rollen_cd and r1.rang_nr = r3.rang_nr and r1.va_dtm = r3.va_dtm)")
    )).query[Trol001].to[List]
  }
  
  /**
   * Insert  the entry
   */
  def insert( r1:Trol001) : ConnectionIO[Int] = 
    Update[Trol001]("insert into vsmadm.trol001 values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?)").run(r1) 
  
  /**
   * Insert  the entry
   */
  def delete( isttop_nrx:String, istkomp_nr:String, rollen_cd:Int, rang_nr:Int) : ConnectionIO[Int] = 
    Update[(String,String,Int,Int)]("delete from vsmadm.trol001 where isttop_nrx = ? and istkomp_nr = ? and rollen_cd = ? and rang_nr = ?").run(
        isttop_nrx, istkomp_nr, rollen_cd, rang_nr) 

  def terminateAkt( top : String, komp : String, roll : Int, rang : Int) : ConnectionIO[Option[Trol001]] = {
    
    selectAktById( top, komp, roll, rang).flatMap({
      case Some(t1) => insert( t1.setTerminated()).flatMap( _ => selectAktById( top, komp, roll, rang))
      case None     => Option[Trol001](Trol001.unit).pure[ConnectionIO]
    })
  }
  
  
}
