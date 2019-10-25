package de.ways42.vsl.tables

import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream
import doobie.util.ExecutionContexts

object Tables {

  //implicit val pointRead: Read[TVSL001] = Read[(Int, Int)].map { 
  //  case (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,aa,ab,ac,ad,ae,af,ag,ah,ai,aj,ak,am,an,ao,ap,aq,ar,as,at,au,av,aw, ax) => 
  //    TVSL001(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,aa,ab,ac,ad,ae,af,ag,ah,ai,aj,ak,am,an,ao,ap,aq,ar,as,at,au,av,aw, ax) }


  //implicit val pointWrite: Write[TVSL001] = Write[(Int, Int)].contramap(p => (p.x, p.y))
/*
  case class TVSL001(
		  GV_DTM           : Long, 
		  GE_DTM           : Long, 
		  VA_DTM           : Long, 
		  DF_ZT            : Long, 
		  SYSTAT_CD        : Char, 
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
		  LV_SWISSRE_CD    : Short)		  
*/
}