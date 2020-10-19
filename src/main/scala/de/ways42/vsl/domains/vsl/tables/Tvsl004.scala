package de.ways42.vsl.domains.vsl.tables

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._

case class Tvsl004 (
    GV_DTM        : Long,
    GE_DTM        : Long,
    VA_DTM        : Long,
    DF_ZT         : Long,
    SYSTAT_CD     : Byte,
    LV_VTG_NR     : String,
    LV_VERS_NR    : Short,
    LV_TEILV_NR   : Short,
    LV_TLAUS_NR   : Short,
    LV_TLAUS_DTM  : Long,
    LV_TLAUS_BTR  : Double,
    LV_TL_STAT_CD : Short,
    LV_TZAHL_DTM  : Long)
    
object Tvsl004 {

  val attributes = Array[String] (
    "GV_DTM",
    "GE_DTM",
    "VA_DTM",
    "DF_ZT",
    "SYSTAT_CD",
    "LV_VTG_NR",
    "LV_VERS_NR",
    "LV_TEILV_NR",
    "LV_TLAUS_NR",
    "LV_TLAUS_DTM",
    "LV_TLAUS_BTR",
    "LV_TL_STAT_CD",
    "LV_TZAHL_DTM"
    )
    
   lazy val attrStr = attributes.mkString(",")
   
  /**
   * Alle Teilauszahlungen mit Historie zu einem Vertrag
   */
   
  def selectVtgnr( vtgnr : String) : ConnectionIO[List[Tvsl004]] = {
    ( Fragment.const( "select " + attrStr + " from VSMADM.TVSL004") ++
      fr"where  LV_VTG_NR = $vtgnr order by lv_vers_nr asc, lv_teilv_nr asc, lv_tlaus_nr asc, va_dtm desc, df_zt desc"
    ).query[Tvsl004].to[List]
  }
  
  /**
   * Alle aktuellen Teilauszahlungen zu einem Vertrag
   */
  
  def selectAktZuVertrag( vtgnr:String) :  ConnectionIO[List[Tvsl004]] = {
    ( Fragment.const( "select " + attrStr + " from VSMADM.Tvsl004 ") ++ Fragments.whereAnd( 
      fr"LV_VTG_NR = $vtgnr ",
      fr"SYSTAT_CD = 1 and GV_DTM < 25000101 and GE_DTM >= 25000101 ") ++
      Fragment.const( " order by lv_vers_nr asc, lv_teilv_nr asc, lv_tlaus_nr asc")
    ).query[Tvsl004].to[List]
  }
  /**
   * Alle aufrechten Teilauszahlungen laden
   */
  def selectAktAll() :  ConnectionIO[List[Tvsl004]] = {
    ( Fragment.const( "select " + attrStr + " from VSMADM.Tvsl004") ++ 
      fr"where SYSTAT_CD = 1 and GV_DTM < 25000101 and GE_DTM >= 25000101 order by lv_vers_nr asc, lv_teilv_nr asc, lv_tlaus_nr asc" 
    ).query[Tvsl004].to[List]
  }
}