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
object HCPoolTask  {
  

  def apply(driver:String, url:String, user:String, passwd:String, size:Int) : (Task[HikariTransactor[Task]],SchedulerService, HikariDataSource) = {

      val c = hcConfig(driver, url, user, passwd, size)
      val es : ExecutorService   = Executors.newFixedThreadPool(size)
      val ec : ExecutionContext  = ExecutionContext.fromExecutor(es) //ExecutionContext.global
      
      val ds = hcdss( c)
      implicit val scheduler :  SchedulerService = Scheduler(es)
      ( transactor( ec, ds), scheduler, ds)
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

  private def hcdss( hcc:HikariConfig) : HikariDataSource = new HikariDataSource(hcc)
  
  private  def transactor( ec : ExecutionContext, ds:HikariDataSource) : Task[HikariTransactor[Task]] = {
    val hc = HikariTransactor.apply[Task](ds, ec, Blocker.liftExecutionContext(ec))
    Task.pure(hc)
  }
  
  def transactor2(ds: HikariDataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, HikariTransactor[Task]] = for {
    ec <- ExecutionContexts.fixedThreadPool[Task](size) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield   HikariTransactor.apply[Task](ds, ec, be)


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

