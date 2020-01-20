package de.ways42.vsl.tables.vsmadm

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream

case class Tzik012 (
		GV_DTM         : Long,
		GE_DTM         : Long,
		VA_DTM         : Long,
		DF_ZT          : Long,
		MOD_CNT        : Int,
		SYSTAT_CD      : String,
		KV_A_VA_DTM    : Long,
		KV_A_DF_ZT     : Long,
		KV_VA_DTM      : Long,
		KV_DF_ZT       : Long,
		KV_MOD_CNT     : Int,
		KV_AEND_GRD_CD : Int,
		Z_NKTO_NR      : String,
		Z_NKTOART_CD   : String,
		Z_UKTOART_CD   : Int,
		Z_ZAHLART_CD   : Int,
		Z_ZW_CD        : Int,
		Z_FAELLIG_NR   : Int,
		Z_LEV_TAG_NR   : Int
		)
		
object Tzik012 {

	val attributes = Array[String] (
			"GV_DTM",
			"GE_DTM",
			"VA_DTM",
			"DF_ZT",
			"MOD_CNT",
			"SYSTAT_CD",
			"KV_A_VA_DTM",
			"KV_A_DF_ZT",
			"KV_VA_DTM",
			"KV_DF_ZT",
			"KV_MOD_CNT",
			"KV_AEND_GRD_CD",
			"Z_NKTO_NR",
			"Z_NKTOART_CD",
			"Z_UKTOART_CD",
			"Z_ZAHLART_CD",
			"Z_ZW_CD",
			"Z_FAELLIG_NR",
			"Z_LEV_TAG_NR")

	lazy val attrStr = attributes.mkString(",")

	def selectAllById( nkto : String, nkart : String, uart : Int) : Query0[Tzik012] = {
			val s = Fragment.const( "select")
			val a = Fragment.const( attrStr)
			val f = Fragment.const( "from vsmadm.tzik012")
			val w = fr"where z_nkto_nr = $nkto and z_nktoart_cd = $nkart and z_uktoart_cd = $uart"
					(s ++ a ++ f ++ w).query[Tzik012]
	}

	def selectAktById( nkto : String, nkart : String, uart : Int) : Query0[Tzik012] = {
			val s = Fragment.const( "select")
			val a = Fragment.const( attrStr)
			val f = Fragment.const( "from vsmadm.tzik012 z1")
			val w = fr"where z_nkto_nr = $nkto and z_nktoart_cd = $nkart and z_uktoart_cd = $uart"
			val w1 = Fragment.const(  "and va_dtm = ( select max (va_dtm) from tzik012 z2 where z1.z_nkto_nr = z2.z_nkto_nr and z1.z_nktoart_cd = z2.z_nktoart_cd and z1.z_uktoart_cd = z2.z_uktoart_cd")
			val w2 = Fragment.const0( "and df_zt  = ( select max (df_zt)  from tzik012 z3 where z1.z_nkto_nr = z3.z_nkto_nr and z1.z_nktoart_cd = z3.z_nktoart_cd and z1.z_uktoart_cd = z3.z_uktoart_cd and z1.va_dtm = z3.va_dtm")
			(s ++ a ++ f ++ w).query[Tzik012]
	}

}