package de.ways42.vsl.connection.hikari


import cats.effect.IO
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor
//import doobie.hikari.hikaritransactor.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query

import scala.concurrent.ExecutionContext
import cats._
import cats.effect._
import cats.implicits._

import cats.effect.ContextShift
import doobie.util.ExecutionContexts
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import javax.sql.DataSource
import doobie.util.transactor.Transactor



/**
 *  https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html
 */
object HcTask  {
  

  def apply(driver:String, url:String, user:String, passwd:String, size:Int) : (Task[HikariTransactor[Task]],SchedulerService, HikariDataSource) = {

      val c = HcConfig.hcConfig(driver, url, user, passwd, size)
      val es : ExecutorService   = Executors.newFixedThreadPool(size)
      val ec : ExecutionContext  = ExecutionContext.fromExecutor(es) //ExecutionContext.global
      
      val ds = HcConfig.getDataSource( c)
      implicit val scheduler :  SchedulerService = Scheduler(es)
      ( HcTransactor.get( ec, ds), scheduler, ds)
  }

  
  


  def main( args:Array[String]) : Unit = {

    implicit val (xas,s,ds) = apply("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 2)
    
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

