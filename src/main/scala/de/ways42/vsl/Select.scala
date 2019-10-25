package de.ways42.vsl

//import de.ways42.vsl.tables.Tables.TVSL001

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream



object Select {
  
  //import de.ways42.vsl.tables.Tables
  
  //val t = Read[Tables.TVSL001]
  
    case class TVSL001(
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
		  LV_SWISSRE_CD    : String/**/)

		 

	def main( args : Array[String]) : Unit = {

			val xa = connection()

					println( tvsl001( xa))
					println( tvsl001_2( xa))
					println( tvsl001_3( xa))
					println( tables.TVSL001.select( xa))
					
					tables.TVSL002.selectVtgnr( "0003065903411").transact(xa).unsafeRunSync.foreach(println)

	}

	
	def connection() : Transactor.Aux[IO, Unit] = {

			// We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
			// is where nonblocking operations will be executed. For testing here we're using a synchronous EC.
			import scala.concurrent.ExecutionContext

			implicit val cs = IO.contextShift( ExecutionContext.global)

			// A transactor that gets connections from java.sql.DriverManager and executes blocking operations
			// on an our synchronous EC. See the chapter on connection handling for more info.
			Transactor.fromDriverManager[IO](
					"com.ibm.db2.jcc.DB2Driver", // driver classname
					"jdbc:db2://172.17.4.39:50001/vslt01", // connect URL (driver-specific)
					"vsmadm",              // user
					"together"                       // password
					//Blocker.liftExecutionContext( ExecutionContext.global) // just for testing
					)
	}

	def tvsl001(xa : Transactor.Aux[IO, Unit]) = {
			//val q = Query.( "select * from VSMADM.TVSL001")
			sql"select * from VSMADM.TVSL001".query[(String,String, String)].stream.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).foreach(println)
	}

	def tvsl001_2( xa : Transactor.Aux[IO, Unit]) = {
			val proc = HC.stream[(String, String, String, String, String, String)](
					"select * from VSMADM.TVSL001",    // statement
					().pure[PreparedStatementIO],      // prep (none)
					512                                // chunk size
					)
					proc.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).foreach(println)
	}

	def tvsl001_3(xa : Transactor.Aux[IO, Unit]) = {

	  println( "Mit Klasse TVSL001")
		sql"select * from VSMADM.TVSL001".query[TVSL001].stream.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).foreach(println)
	}
}

