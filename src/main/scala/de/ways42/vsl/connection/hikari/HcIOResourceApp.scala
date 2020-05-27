package de.ways42.vsl.connection.hikari


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

object HcIOResourceApp extends App {

  /**
   *  https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html
   */
  
  //implicit val cs = IO.contextShift(ExecutionContext.global)

  val hcc = HcConfig.hcConfig()
  val fixedThreadPool  : ExecutorService = Executors.newFixedThreadPool(32)
	val BlockingFileIO : ExecutionContext  = ExecutionContext.fromExecutor(fixedThreadPool)
  implicit val cs2 = IO.contextShift(BlockingFileIO)
  
  // transactor with config
  val transactor:  IO[HikariTransactor[IO]] = HcTransactor.get( BlockingFileIO, hcc)
  val transactorG: IO[HikariTransactor[IO]] = HcTransactor.get( ExecutionContext.global, hcc)

  //def main( args:Array[String]) : Unit = {
    val c = for {
      xa <- transactor
      result <- sql"select 41 from sysibm.sysdummy1".query[Int].unique.transact(xa)
      _ <- IO.shift(IO.contextShift(ExecutionContext.global))
      xa2 <- transactorG
      result2 <- sql"select 41 from sysibm.sysdummy1".query[Int].unique.transact(xa2)
    } yield (result, result2)
  println(c.unsafeRunSync())
  
  // Leider ist der Pool noch nicht down 
  //}
}

