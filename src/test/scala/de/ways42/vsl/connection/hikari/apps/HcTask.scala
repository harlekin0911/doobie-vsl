package de.ways42.vsl.connection.hikari.apps

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sql.DataSource

import scala.concurrent.ExecutionContext

import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query
import doobie.util.transactor.Transactor
import doobie.util.ExecutionContexts

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

import cats._
import cats.effect.IO
import cats.effect._
import cats.implicits._
import cats.effect.ContextShift

import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService

import de.ways42.vsl.connection.hikari.HcTransactor
import de.ways42.vsl.connection.hikari.HcConfig



/**
 *  https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html
 */
object HcTask  {
  
  def main( args:Array[String]) : Unit = {

    implicit val (xas,s,ds) = HcTransactor("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 2)
    
    // Modify transaction behavior
    //val ta = xas.flatMap( xa => Task(Transactor.strategy.set(xa, doobie.util.transactor.Strategy.default.copy())))
    
    //implicit val s = t._2
    val c :Task[List[Int]] = for {
        xa <- xas
        result <- Task.gather(
            List( sql"select count(*) from vsmadm.tvsl001".query[Int].unique.transact(xa), //ensuring xa.shutdown
                sql"select count(*) from vsmadm.tvsl002".query[Int].unique.transact(xa))) //ensuring xa.shutdown
      } yield result
      val d = c.runSyncUnsafe()
      println( d)
      d.mkString( " ")
      ds.close()
      s.shutdown()
  }
}

