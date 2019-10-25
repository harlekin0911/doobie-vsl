package de.ways42.vsl.tables



import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream



object TVSL002 {
  
    case class Table(
		  GV_DTM           : String, 
		  GE_DTM           : String, 
		  VA_DTM           : String, 
		  DF_ZT            : String, 
		  SYSTAT_CD        : String, 
		  LV_VTG_NR        : String , 
		  LV_ABBR_CD       : String, 
		  LV_ABRUF_DTM     : String , 
		  LV_BONRAB_BTR    : String, 
		  LV_VERTR_DYN_CD  : String, 
		  LV_DYN_ZTR       : String, 
		  LV_DYNAUS_ANZ    : String, 
		  LV_DYNBEI_BTR    : String, 
		  LV_DYNBEI_PRZ    : String, 
		  LV_DYNVS_BTR     : String, 
		  LV_DYNVS_PRZ     : String, 
		  LV_JURABL_DTM    : String, 
		  LV_JURBEG_DTM    : String, 
		  LV_KIRAB_BTR     : String, 
		  LV_RATABK_BTR    : String, 
		  LV_RDIFF_BTR     : String, 
		  LV_RENTW_CD      : String, 
		  LV_VERTR_RUCK_CD : String, 
		  LV_SAMINK_BTR    : String, 
		  LV_SAMINK_CD     : String , 
		  LV_VERTR_STAT_CD : String, 
		  LV_STK_BTR       : String, 
		  LV_SUMRAB_BTR    : String, 
		  LV_VERT_TARIF_CD : String, 
		  LV_TRDK_BTR      : String, 
		  LV_VERT_ZAHL_BTR : String, 
		  LV_VERT_ZAHLW_CD : String, 
		  LV_OPTBEG_DTM    : String, 
		  LV_OPTION_CD     : String , 
		  LV_WERBE_CD      : String , 
		  LV_PRODUKT_CD    : String , 
		  LV_MAND_CD       : String, 
		  LV_RESIT_CD      : String , 
		  LV_BUEND_CD      : String, 
		  LV_KOLLEK_CD     : String, 
		  LV_WAEHR_CD      : String, 
		  LV_FLEXBEI_CD    : String , 
		  LV_PRODKLASS_CD  : String , 
		  LV_VERTR_KEST_CD : String , 
		  LV_UR_FLEXBEI_CD : String, 
		  LV_VKORG_CD      : String, 
		  LV_STUF_ZAHL_BTR : String , 
		  LV_SWISSRE_JZ    : String , 
		  LV_SWISSRE_CD    : String)

		 
	def select(xa : Transactor.Aux[IO, Unit]) = {

	  println( "Mit Klasse TVSL001")
		sql"select * from VSMADM.TVSL001".query[Table].stream.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).foreach(println)
	}
}

