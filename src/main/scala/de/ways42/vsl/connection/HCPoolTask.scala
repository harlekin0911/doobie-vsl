package de.ways42.vsl.connection


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






/**
 *  https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html
 */
object HCPoolTask  {

  def apply(driver:String, url:String, user:String, passwd:String, size:Int) : (Task[HikariTransactor[Task]],SchedulerService) = {

      val c = hcConfig("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", size)
      val es : ExecutorService   = Executors.newFixedThreadPool(size)
      val ec : ExecutionContext  = ExecutionContext.fromExecutor(es) //ExecutionContext.global
      

      implicit val scheduler :  SchedulerService = Scheduler(es)
      ( transactor( ec, c), scheduler)
  }

  // hikari pooling config
  private def hcConfig(driver:String, url:String, user:String, passwd:String, size:Int) : HikariConfig = {
      val config = new HikariConfig()
          config.setJdbcUrl(         url)
          config.setUsername(        user)
          config.setPassword(        passwd)
          config.setMaximumPoolSize( size)
          config.setDriverClassName( driver)
          config
  }

  private  def transactor( ec : ExecutionContext, hcc:HikariConfig) : Task[HikariTransactor[Task]] = {
    val ds = new HikariDataSource(hcc)
    sys.addShutdownHook(ds.close())
    val hc = HikariTransactor.apply[Task](ds, ec, Blocker.liftExecutionContext(ec))
    Task.pure(hc)
  }
  
  def transactor2(ds: HikariDataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, HikariTransactor[Task]] = for {
    ec <- ExecutionContexts.fixedThreadPool[Task](size) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield   HikariTransactor.apply[Task](ds, ec, be)


  def main( args:Array[String]) : Unit = {

    val t = apply("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50001/vslt01", "vsmadm", "together", 32)
    implicit val s = t._2
      val c = for {
        xa <- t._1
        result <- sql"select 41 from sysibm.sysdummy1".query[Int].unique.transact(xa) //ensuring xa.shutdown
       // _ <- xa.shutdown
      } yield result
      println(c.runSyncUnsafe())
  }
}

