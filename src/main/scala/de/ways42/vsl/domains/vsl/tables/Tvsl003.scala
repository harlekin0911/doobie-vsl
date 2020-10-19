package de.ways42.vsl.domains.vsl.tables

case class Tvsl003 (
  GV_DTM           : Long,
  GE_DTM           : Long,
  VA_DTM           : Long,
  DF_ZT            : Long,
  SYSTAT_CD        : Byte,
  LV_VTG_NR        : String,
  LV_VERS_NR       : Short,
  LV_TEILV_NR      : Short,
  LV_ABLBZD_DTM    : Long,
  LV_ABLLST_DTM    : Long,
  LV_ABLVD_DTM     : Long,
  LV_BEGDIV_DTM    : Long,
  LV_BEGSD_DTM     : Long,
  LV_BEGST_DTM     : Long,
  LV_BEGTE_DTM     : Long,
  LV_BXN_BTR       : Double,
  LV_ES_BTR        : Double,
  LV_GARRKW_BTR    : Double,
  LV_PXNZ_BTR      : Double,
  LV_PXNUZ_BTR     : Double,
  LV_RATZU_BTR     : Double,
  LV_RENTE_BTR     : Double,
  LV_RISZU_BTR     : Double,
  LV_RS_BTR        : Double,
  LV_TV_STAT_CD    : Short,
  LV_TV_TARIF_CD   : Short,
  LV_BSKOR_BTR     : Double,
  LV_RENTEZUS_BTR  : Double,
  LV_STUF_BXN_BTR  : Double,
  LV_RISINF_BTR    : Double,
  LV_REZINSIND_PRZ : Double
  )
  
  object Tvsl003 {

  val attributes = Array[String] (
      "GV_DTM",
      "GE_DTM",
      "VA_DTM",
      "DF_ZT",
      "SYSTAT_CD",
      "LV_VTG_NR",
      "LV_VERS_NR",
      "LV_TEILV_NR",
      "LV_ABLBZD_DTM",
      "LV_ABLLST_DTM",
      "LV_ABLVD_DTM",
      "LV_BEGDIV_DTM",
      "LV_BEGSD_DTM",
      "LV_BEGST_DTM",
      "LV_BEGTE_DTM",
      "LV_BXN_BTR",
      "LV_ES_BTR",
      "LV_GARRKW_BTR",
      "LV_PXNZ_BTR",
      "LV_PXNUZ_BTR",
      "LV_RATZU_BTR",
      "LV_RENTE_BTR",
      "LV_RISZU_BTR",
      "LV_RS_BTR",
      "LV_TV_STAT_CD",
      "LV_TV_TARIF_CD",
      "LV_BSKOR_BTR",
      "LV_RENTEZUS_BTR",
      "LV_STUF_BXN_BTR",
      "LV_RISINF_BTR",
      "LV_REZINSIND_PRZ"
      )
      
      lazy val attrStr = attributes.mkString(",")
}