package de.ways42.vsl.connection.hikari

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import cats.effect.Blocker
import cats.effect.ContextShift
import cats.effect.IO
import doobie.hikari.HikariTransactor
import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService

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

    ( HcTransactor( ec, ds)( Task.contextShift(scheduler)), scheduler, ds)
  }

}

