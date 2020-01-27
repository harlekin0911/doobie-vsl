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

import tables.mandate._
import tables.vsmadm._
import java.time.LocalDate
import java.time.LocalDate
import java.sql.Date

import org.scalatest.funsuite.AnyFunSuite //TestSuite

class TestSelect extends AnyFunSuite {

	val xa : Transactor.Aux[IO, Unit] = connection()

	test( "FLTEST-Select") {

			tvsl001(   xa)
			tvsl001_2( xa)
	}
	
	test( "FLTest-Select2-VSL") {
			Tvsl001.selectAll().stream.take(5).compile.to[List].transact(xa).unsafeRunSync.take(5).foreach(println)
			Tvsl001.selectAktById(   "0003065903411").to[List].transact(xa).unsafeRunSync.foreach(println)
			println ( "Anzahl aktiver Vertraege: " + Tvsl001.selectAktAktive().to[List].transact(xa).unsafeRunSync.length)
			println ( "Anzahl bpfl.   Vertraege: " + Tvsl001.selectAktBeitragspflichtig().to[List].transact(xa).unsafeRunSync.length)
			
			Tvsl002.selectVtgnr( "0003065903411").to[List].transact(xa).unsafeRunSync.foreach(println)

  }

  test ( "FLTest-Select-Rolle") {
			Trol001.selectById( "0050034703671", "", 89, 1).to[List].transact(xa).unsafeRunSync.foreach(println)
  }
  
  test ( "FLTest-Select-ZIK") {
  
			Tzik012.selectAktById( "002110500524101", "1", 0).to[List].transact(xa).unsafeRunSync.foreach(println)
			Tzik012.selectAllById( "002110500524101", "1", 0).to[List].transact(xa).unsafeRunSync.foreach(println)
			val z12 = Tzik012.selectNktoAktByNkartandUktoart( NonEmptyList("1", List("C")), NonEmptyList( 0, Nil)).to[List].transact(xa).unsafeRunSync
			//z12.foreach(println)
			println( "Anzahl Nebenkonten " + z12.length)
			println( "Nicht im LEV " + z12.filter ( _.Z_ZAHLART_CD != 1).length)
  }
  
  test ( "FLTest-Select-Mandate") {
  
			println( Mandate.selectAktById(313038).transact(xa).unsafeRunSync)
			Mandate.selectAllById(313038).transact(xa).unsafeRunSync.foreach(println)
			println( "Alle Mandate: " + Mandate.selectAktAll().transact(xa).unsafeRunSync.length)
			
			println( Payment.selectById(2229).transact(xa).unsafeRunSync)
			BusinessObjectRef.selectById(2229, 1).to[List].transact(xa).unsafeRunSync.foreach(println)
			BusinessObjectRef.selectByMandateId(313038).to[List].transact(xa).unsafeRunSync.foreach(println)
			
			val lm = Mandate.selectAktAllNotTerminated().transact(xa).unsafeRunSync
			println( "Anzahl Mandate nicht terminiert: " + lm.length)
			val lp = Payment.selectLastPaymentAlle().transact(xa).unsafeRunSync
			println( "Anzahl Payments: " + lp.length)
		

			val mm =  lm.foldRight( Map.empty[Long,( Mandate, Option[Payment])])((m,z) => z.updated(m.MANDATE_ID, (m,None)))
			val mmp = lp.foldRight( mm)((p,m) =>  m.get(p.MANDATE_ID)  match { 
			  case Some(k)   => m.updated(p.MANDATE_ID, (k._1, Some(p)))
			  case _         => m
			})
			
			
			println ( "Anzahl ohne Payment: " + mmp.filter( _._2._2.isEmpty).size) 
			println ( "Anzahl abgelaufene mit aktiven Status: " + mmp.filter( _._2._2.isEmpty).size) 
			
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
}

