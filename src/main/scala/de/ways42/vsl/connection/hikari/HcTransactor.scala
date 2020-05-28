package de.ways42.vsl.connection.hikari

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sql.DataSource

import scala.concurrent.ExecutionContext

import doobie._
import doobie.util.transactor
import doobie.hikari._

import cats.effect._

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import monix.execution.schedulers.SchedulerService
import monix.execution.Scheduler
import monix.eval.Task

object HcTransactor {
  
  // @ToDo
  // Leider ist der Pool noch nicht down
  def apply( ec : ExecutionContext, hcc:HikariConfig)( implicit ev: ContextShift[IO])  : IO[HikariTransactor[IO]] = 
      IO.pure(HikariTransactor.apply[IO](new HikariDataSource(hcc), ec, Blocker.liftExecutionContext(ec)))

   // Leider ist der Pool noch nicht down
  // @ToDo

  def apply( ec : ExecutionContext, ds:HikariDataSource)( implicit ev: ContextShift[Task]) : Task[HikariTransactor[Task]] = {
    Task.pure( HikariTransactor.apply[Task](ds, ec, Blocker.liftExecutionContext(ec)))
  }
  
  def apply(driver:String, url:String, user:String, passwd:String, size:Int) : (Task[HikariTransactor[Task]],SchedulerService, HikariDataSource) = {

    val c = HcConfig.hcConfig(driver, url, user, passwd, size)
    val es : ExecutorService   = Executors.newFixedThreadPool(size)
    val ec : ExecutionContext  = ExecutionContext.fromExecutor(es) //ExecutionContext.global
      
    val ds = HcConfig.getDataSource( c)
    val scheduler :  SchedulerService = Scheduler(es)
    //SchedulerService.
    ( HcTransactor( ec, ds), scheduler, ds)
  }

}

