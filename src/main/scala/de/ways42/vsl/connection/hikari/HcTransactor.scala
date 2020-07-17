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
  
  
  /** 
   *  Der Pool muss nach Verwendung geschlossen werden
   */
  def apply( ec : ExecutionContext, ds:HikariDataSource)( implicit ev: ContextShift[IO])  : IO[HikariTransactor[IO]] = 
      IO.pure(HikariTransactor.apply[IO](ds, ec, Blocker.liftExecutionContext(ec)))

  /** 
   *  Leider ist der Pool nicht down
   *  Der Pool muss nach Verwendung geschlossen werden
   */
  def applyTask( ec : ExecutionContext, ds:HikariDataSource)( implicit ev: ContextShift[Task]) : Task[HikariTransactor[Task]] = {
    Task.pure( HikariTransactor.apply[Task](ds, ec, Blocker.liftExecutionContext(ec)))
  }
  
  /**
   * Pool und ExcetutionService muesssen nach Verwendung geschlossen werden bzw heruntergefahren
   */
  def apply(driver:String, url:String, user:String, passwd:String, size:Int) : (Task[HikariTransactor[Task]],SchedulerService, HikariDataSource) = {

    val c = HcConfig.hcConfig(driver, url, user, passwd, size)
    val es : ExecutorService   = Executors.newFixedThreadPool(size)
    val ec : ExecutionContext  = ExecutionContext.fromExecutor(es) //ExecutionContext.global
      
    val ds = HcConfig.getDataSource( c)
    val scheduler :  SchedulerService = Scheduler(es)

    ( HcTransactor.applyTask( ec, ds)( Task.contextShift(scheduler)), scheduler, ds)
  }

}

