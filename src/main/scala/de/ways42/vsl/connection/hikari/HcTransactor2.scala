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

    val c = HcConfig.hcConfig(driver, url, user, passwd, size)
    val es : ExecutorService   = Executors.newFixedThreadPool(size)
    val ec : ExecutionContext  = ExecutionContext.fromExecutor(es) //ExecutionContext.global
      
    val ds = HcConfig.getDataSource( c)
    val scheduler :  SchedulerService = Scheduler(es)
    
    implicit val cs = Task.contextShift(scheduler)
    implicit val as : Async[Task] = ???


    ( Task.pure(HcTransactor2[Task]( ec, ds)), scheduler, ds)
  }
  
  def register[A] : Callback[Throwable, A] => Unit  = ???

}

