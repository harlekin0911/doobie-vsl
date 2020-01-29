package de.ways42.vsl.tables.vsmadm

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
)

object Trol001 {

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
	  
  def selectById( top : String, komp : String, roll : Int, rang : Int) : ConnectionIO[List[Trol001]] = {
    ( Fragment.const( "select "+ attrStr + " from vsmadm.trol001") ++
      fr"where isttop_nrx = $top and istkomp_nr = $komp and rollen_cd = $roll and rang_nr = $rang"
    ).query[Trol001].to[List]
  }
  def selectAktById( top : String, komp : String, roll : Int, rang : Int) : ConnectionIO[Option[Trol001]] = {
    ( Fragment.const( "select "+ attrStr + " from vsmadm.trol001 r1") ++
      Fragments.whereAnd(           
          fr"r1.isttop_nrx = $top and r1.istkomp_nr = $komp and r1.rollen_cd = $roll and r1.rang_nr = $rang",
          Fragment.const( "va_dtm = (select max(va_dtm) from vsmadm.trol001 r2 " + 
                                      "where r1.isttop_nrx = r2.isttop_nrx and r1.istkomp_nr = r2.istkomp_nr and r1.rollen_cd = r2.rollen_cd and r1.rang_nr = r2.rang_nr)"),
          Fragment.const( "df_zt  = (select max(df_zt)  from vsmadm.trol001 r3 " + 
                                      "where r1.isttop_nrx = r3.isttop_nrx and r1.istkomp_nr = r3.istkomp_nr and r1.rollen_cd = r3.rollen_cd and r1.rang_nr = r3.rang_nr and r1.va_dtm = r3.va_dtm)")
    )).query[Trol001].option
  }

}
