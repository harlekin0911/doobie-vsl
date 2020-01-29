package de.ways42.vsl.tables.vsmadm


import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream

case class Tvsl001(
		GV_DTM           : Long, 
		GE_DTM           : Long, 
		VA_DTM           : Long, 
		DF_ZT            : Long, 
		SYSTAT_CD        : Byte, //Char
		LV_VTG_NR        : String, 
		LV_ABBR_CD       : Short, 
		LV_ABRUF_DTM     : Long , 
		LV_BONRAB_BTR    : Double, 
		LV_VERTR_DYN_CD  : Short, 
		LV_DYN_ZTR       : Short, 
		LV_DYNAUS_ANZ    : Short, 
		LV_DYNBEI_BTR    : Double, 
		LV_DYNBEI_PRZ    : Double, 
		LV_DYNVS_BTR     : Double, 
		LV_DYNVS_PRZ     : Double, 
		LV_JURABL_DTM    : Long, 
		LV_JURBEG_DTM    : Long, 
		LV_KIRAB_BTR     : Double, 
		LV_RATABK_BTR    : Double, 
		LV_RDIFF_BTR     : Double, 
		LV_RENTW_CD      : Short, 
		LV_VERTR_RUCK_CD : Short, 
		LV_SAMINK_BTR    : Double, 
		LV_SAMINK_CD     : Short , 
		LV_VERTR_STAT_CD : Short, 
		LV_STK_BTR       : Double, 
		LV_SUMRAB_BTR    : Double, 
		LV_VERT_TARIF_CD : Short, 
		LV_TRDK_BTR      : Double, 
		LV_VERT_ZAHL_BTR : Double, 
		LV_VERT_ZAHLW_CD : Short, 
		LV_OPTBEG_DTM    : Long, 
		LV_OPTION_CD     : Short , 
		LV_WERBE_CD      : Short , 
		LV_PRODUKT_CD    : Short , 
		LV_MAND_CD       : Short, 
		LV_RESIT_CD      : Short , 
		LV_BUEND_CD      : Short, 
		LV_KOLLEK_CD     : Short, 
		LV_WAEHR_CD      : String, 
		LV_FLEXBEI_CD    : Short , 
		LV_PRODKLASS_CD  : Short , 
		LV_VERTR_KEST_CD : Short , 
		LV_UR_FLEXBEI_CD : Short, 
		LV_VKORG_CD      : Short, 
		LV_STUF_ZAHL_BTR : Double , 
		LV_SWISSRE_JZ    : Short , 
		LV_SWISSRE_CD    : Short
		)

object Tvsl001 {
		  
		val attributes = Array[String](
		    "GV_DTM", 
        "GE_DTM", 
        "VA_DTM", 
        "DF_ZT", 
        "SYSTAT_CD", 
        "LV_VTG_NR", 
        "LV_ABBR_CD", 
        "LV_ABRUF_DTM", 
        "LV_BONRAB_BTR", 
        "LV_VERTR_DYN_CD", 
        "LV_DYN_ZTR", 
        "LV_DYNAUS_ANZ", 
        "LV_DYNBEI_BTR", 
        "LV_DYNBEI_PRZ", 
        "LV_DYNVS_BTR", 
        "LV_DYNVS_PRZ", 
        "LV_JURABL_DTM", 
        "LV_JURBEG_DTM", 
        "LV_KIRAB_BTR", 
        "LV_RATABK_BTR", 
        "LV_RDIFF_BTR", 
        "LV_RENTW_CD", 
        "LV_VERTR_RUCK_CD", 
        "LV_SAMINK_BTR", 
        "LV_SAMINK_CD", 
        "LV_VERTR_STAT_CD", 
        "LV_STK_BTR", 
        "LV_SUMRAB_BTR", 
        "LV_VERT_TARIF_CD", 
        "LV_TRDK_BTR", 
        "LV_VERT_ZAHL_BTR", 
        "LV_VERT_ZAHLW_CD", 
        "LV_OPTBEG_DTM", 
        "LV_OPTION_CD", 
        "LV_WERBE_CD", 
        "LV_PRODUKT_CD", 
        "LV_MAND_CD", 
        "LV_RESIT_CD", 
        "LV_BUEND_CD", 
        "LV_KOLLEK_CD", 
        "LV_WAEHR_CD", 
        "LV_FLEXBEI_CD", 
        "LV_PRODKLASS_CD", 
        "LV_VERTR_KEST_CD", 
        "LV_UR_FLEXBEI_CD", 
        "LV_VKORG_CD", 
        "LV_STUF_ZAHL_BTR", 
        "LV_SWISSRE_JZ", 
		    "LV_SWISSRE_CD"
				)   
		    
	lazy val attr = attributes.mkString(",")
	
  def selectAktById( vtgnr : String) : ConnectionIO[Option[Tvsl001]] = {
    ( Fragment.const( "select " + attr +  " from VSMADM.TVSL001") ++ 
      fr"where LV_VTG_NR = $vtgnr and SYSTAT_CD = 1 and GV_DTM < 25000101 and GE_DTM >= 25000101"
    ).query[Tvsl001].option
  }
		
  def selectAktAll() : ConnectionIO[List[Tvsl001]] = {
    ( Fragment.const( "select " + attr + " from VSMADM.TVSL001") ++
      fr"where SYSTAT_CD = 1 and GV_DTM < 25000101 and GE_DTM >= 25000101"
    ).query[Tvsl001].to[List]
  }

  def selectAktAllBeitragspflichtig() : ConnectionIO[List[Tvsl001]] = {
    ( Fragment.const( "select " + attr + " from VSMADM.TVSL001") ++
      fr"where SYSTAT_CD = 1 and GV_DTM < 25000101 and GE_DTM >= 25000101 and lv_vertr_stat_cd = 0"
    ).query[Tvsl001].to[List]
  }
  
  def selectAktAllAktive() : ConnectionIO[List[Tvsl001]] = {
    ( Fragment.const( "select " + attr + " from VSMADM.TVSL001") ++
      fr"where SYSTAT_CD = 1 and GV_DTM < 25000101 and GE_DTM >= 25000101 and lv_vertr_stat_cd < 60"
    ).query[Tvsl001].to[List]
  }

  def selectAll() : ConnectionIO[List[Tvsl001]] = {
    Fragment.const( "select " + attr + " from VSMADM.TVSL001 order by lv_vtg_nr desc, va_dtm desc, df_zt desc").query[Tvsl001].to[List]
  }

  def selectAllMaxCount( c:Long) : ConnectionIO[List[Tvsl001]] = {
    Fragment.const( "select " + attr + " from VSMADM.TVSL001 order by lv_vtg_nr desc, va_dtm desc, df_zt desc").query[Tvsl001].stream.take(c).compile.to[List]
  }  
}
