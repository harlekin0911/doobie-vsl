package de.ways42.vsl.connection


import cats.effect.IO
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor
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

object HCPool extends App {


  def hcConfig() : HikariConfig = {
    // hikari pooling config with mysql
    val config = new HikariConfig()
    config.setJdbcUrl("jdbc:db2://172.17.4.39:50001/vslt01")
    config.setUsername("vsmadm")
    config.setPassword("together")
    config.setMaximumPoolSize(5)
    config.setDriverClassName("com.ibm.db2.jcc.DB2Driver")
    config
  }
  
  

  /**
   *  https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html
   */
  
  //implicit val cs = IO.contextShift(ExecutionContext.global)

  val fixedThreadPool  : ExecutorService = Executors.newFixedThreadPool(32)
	val BlockingFileIO : ExecutionContext  = ExecutionContext.fromExecutor(fixedThreadPool)
  implicit val cs2 = IO.contextShift(BlockingFileIO)
  
  def transactor( ec : ExecutionContext, hcc:HikariConfig) : IO[HikariTransactor[IO]] = 
    IO.pure(HikariTransactor.apply[IO](new HikariDataSource(hcc), ec, Blocker.liftExecutionContext(ec)))


  // transactor with config
  val transactor:  IO[HikariTransactor[IO]] = transactor( BlockingFileIO, hcConfig)
  val transactorG: IO[HikariTransactor[IO]] = transactor( ExecutionContext.global, hcConfig)

  //def main( args:Array[String]) : Unit = {
    val c = for {
      xa <- transactor
      result <- sql"select 41 from sysibm.sysdummy1".query[Int].unique.transact(xa)
    } yield result
  println(c.unsafeRunSync())
  //}
}

