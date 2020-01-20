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
	  
  def selectById( top : String, komp : String, roll : Int, rang : Int) : Query0[Trol001] = {
    val s = Fragment.const( "select ")
    val a = Fragment.const( attrStr)
    val f = Fragment.const( " from vsmadm.trol001")
    val w = fr"where isttop_nrx = $top and istkomp_nr = $komp and rollen_cd = $roll and rang_nr = $rang"
    (s ++ a ++ f ++ w).query[Trol001]
  }

  }
