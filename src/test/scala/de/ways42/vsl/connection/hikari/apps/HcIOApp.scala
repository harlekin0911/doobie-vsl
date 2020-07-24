package de.ways42.vsl.connection.hikari.apps


import cats.effect._
import cats.implicits._
import doobie._
import doobie.hikari._
import doobie.implicits._
import doobie.util.query.Query
import scala.concurrent.ExecutionContext
import de.ways42.vsl.connection.hikari.HcIOResource

//object Pool {
  

object HcIOApp extends IOApp {
  
  // IOApp provides a ContextShift
  //implicit val cs = IO.contextShift(ExecutionContext.global)

  def run(args: List[String]): IO[ExitCode] =

    HcIOResource( "com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50013/vslt03", "vsmadm", "together").use { xa =>

      // Construct and run your server here!
      for {
        n <- sql"select 42 from sysibm.sysdummy1".query[Int].unique.transact(xa)
        _ <- IO(println(n))
      } yield ExitCode.Success
      
      val a = sql"select distinct(lv_vtg_nr) from vsmadm.tvsl001 where lv_vtg_nr = 0003065903411".query[String].unique.transact(xa)
      val b = a.flatMap( n => IO(println(n)).map( _=> ExitCode.Success))
      b

    }
  }

//  def main( args : Array[String]) : Unit = HikariApp.run(args.toList)
//}