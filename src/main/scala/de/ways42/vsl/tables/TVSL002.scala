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
		  GV_DTM          : Long, 
		  GE_DTM          : Long, 
		  VA_DTM          : Long, 
		  DF_ZT           : Long, 
		  SYSTAT_CD       : Byte, 
		  LV_VTG_NR       : String, 
		  LV_VERS_NR      : Short, 
		  LV_TARIF_CD     : Short, 
		  LV_REFVS_NR     : Short, 
		  LV_HVZV_CD      : Short, 
		  LV_RUECK_KNZ    : Byte, 
		  LV_LEIST_PRZ    : Double, 
		  LV_DIVVOR_CD    : Short, 
		  LV_BTRFON_PRZ   : Double, 
		  LV_ABFMOD_CD    : Short, 
		  LV_BARAUS_PRZ   : Double, 
		  LV_BEGREN_DTM   : Long, 
		  LV_BEIVER_PRZ   : Double, 
		  LV_BONSYS_CD    : Short, 
		  LV_BONUS_PRZ    : Double, 
		  LV_DYN_CD       : Short, 
		  LV_KARENZ_ZTR   : Short, 
		  LV_GARRLZ_DTM   : Long, 
		  LV_KLAUS_CD     : Short, 
		  LV_RZAHLW_CD    : Short, 
		  LV_VERS_STAT_CD : Short, 
		  LV_VA_PRZ       : Double, 
		  LV_FONDS_KL_CD  : Short, 
		  LV_FONDS_PRZ    : Double, 
		  LV_RENTSTG_PRZ  : Double, 
		  LV_TEIL_PRZ     : Double, 
		  LV_LEIUMF_CD    : Short, 
		  LV_BEIMOD_CD    : Short, 
		  LV_STUFEN_DTM   : Long, 
		  LV_DIVVORBU_CD  : Short, 
		  LV_LERG_OPT_CD  : Short )   
				 
   
   def selectVtgnr( vtgnr : String) : doobie.ConnectionIO[List[de.ways42.vsl.tables.TVSL002.Table]] = {
     
		sql"""select 
      GV_DTM          , 
		  GE_DTM          , 
		  VA_DTM          , 
		  DF_ZT           , 
		  SYSTAT_CD       , 
		  LV_VTG_NR       , 
		  LV_VERS_NR      , 
		  LV_TARIF_CD     , 
		  LV_REFVS_NR     , 
		  LV_HVZV_CD      , 
		  LV_RUECK_KNZ    , 
		  LV_LEIST_PRZ    , 
		  LV_DIVVOR_CD    , 
		  LV_BTRFON_PRZ   , 
		  LV_ABFMOD_CD    , 
		  LV_BARAUS_PRZ   , 
		  LV_BEGREN_DTM   , 
		  LV_BEIVER_PRZ   , 
		  LV_BONSYS_CD    , 
		  LV_BONUS_PRZ    , 
		  LV_DYN_CD       , 
		  LV_KARENZ_ZTR   , 
		  LV_GARRLZ_DTM   , 
		  LV_KLAUS_CD     , 
		  LV_RZAHLW_CD    , 
		  LV_VERS_STAT_CD , 
		  LV_VA_PRZ       , 
		  LV_FONDS_KL_CD  , 
		  LV_FONDS_PRZ    , 
		  LV_RENTSTG_PRZ  , 
		  LV_TEIL_PRZ     , 
		  LV_LEIUMF_CD    , 
		  LV_BEIMOD_CD    , 
		  LV_STUFEN_DTM   , 
		  LV_DIVVORBU_CD  , 
		  LV_LERG_OPT_CD 
		  from VSMADM.TVSL002 
		  where  LV_VTG_NR = $vtgnr""".query[Table].to[List]
   }
}

