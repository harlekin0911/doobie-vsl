package de.ways42.vsl.connection.hikari

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import cats.effect.Blocker
import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Async
import doobie.hikari.HikariTransactor
import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService

import  scala.language.higherKinds
import monix.execution.Callback

import cats.effect.{LiftIO, Sync}
import de.ways42.vsl.connection.JdbcOptions

object HcTransactor2 {
    
  def apply[M[_] : Async: ContextShift]( ec : ExecutionContext, hds:HikariDataSource)  : HikariTransactor[M] =
    HikariTransactor.apply[M](hds, ec, Blocker.liftExecutionContext(ec))

  
  /** 
   *  @ToDo
   *  Leider ist der Pool  nicht down
   */
  def apply[M[_] : Async : ContextShift]( ec : ExecutionContext, hcc:HikariConfig) : HikariTransactor[M] = 
    apply( ec, new HikariDataSource(hcc))
  
  def apply(driver:String, url:String, user:String, passwd:String, size:Int) : (Task[HikariTransactor[Task]],SchedulerService, HikariDataSource) = {

    val c = HcConfig.hcConfig(driver, url + JdbcOptions.db2Options, user, passwd, size)
    val es : ExecutorService   = Executors.newFixedThreadPool(size)
    val ec : ExecutionContext  = ExecutionContext.fromExecutor(es) //ExecutionContext.global
      
    val ds = HcConfig.getDataSource( c)
    val scheduler :  SchedulerService = Scheduler(es)
    
    implicit val cs : cats.effect.ContextShift[monix.eval.Task] = Task.contextShift(scheduler)
    
    // IOInstances enthaelt den implicit Async
    import cats.effect.IOInstances
    //implicit val as : Async[Task] = ???

    // ◾not enough arguments for method apply: 
    // * (implicit evidence$1: cats.effect.Async[monix.eval.Task], 
    //    implicit evidence$2: cats.effect.ContextShift[monix.eval.Task]) doobie.hikari.HikariTransactor[monix.eval.Task] 
    // in object HcTransactor2. Unspecified value parameters evidence$1, evidence$2.
    // ◾diverging implicit expansion for type cats.effect.Async[monix.eval.Task] starting with method ReaderWriterStateTAsync in object Async


    ( Task.pure(HcTransactor2[monix.eval.Task]( ec, ds)), scheduler, ds)
  }
  
  def register[A] : Callback[Throwable, A] => Unit  = ???

}

