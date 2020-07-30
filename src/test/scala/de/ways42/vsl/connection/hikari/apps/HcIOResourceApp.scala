package de.ways42.vsl.connection.hikari.apps


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
import de.ways42.vsl.connection.hikari.HcTransactor
import de.ways42.vsl.connection.hikari.HcConfig

object HcIOResourceApp extends App {

  /**
   *  https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html
   */
  
  //implicit val cs = IO.contextShift(ExecutionContext.global)

  val hcc = HcConfig.hcConfig("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://172.17.4.39:50013/vslt03", "vsmadm", "together", 32)
  val ds  = new HikariDataSource(hcc)
  val fixedThreadPool  : ExecutorService = Executors.newFixedThreadPool(32)
	val BlockingFileIO : ExecutionContext  = ExecutionContext.fromExecutor(fixedThreadPool)
  implicit val cs2 = IO.contextShift(BlockingFileIO)
  
  // transactor with config
  val transactor:  IO[HikariTransactor[IO]] = HcTransactor( BlockingFileIO, ds)
  val transactorG: IO[HikariTransactor[IO]] = HcTransactor( ExecutionContext.global, ds)

    val c = for {
      xa <- transactor
      result <- sql"select 41 from sysibm.sysdummy1".query[Int].unique.transact(xa)
      _ <- IO.shift(IO.contextShift(ExecutionContext.global))
      xa2 <- transactorG
      result2 <- sql"select 41 from sysibm.sysdummy1".query[Int].unique.transact(xa2)
    } yield (result, result2)
  println(c.unsafeRunSync())
  
  // Leider ist der Pool noch nicht down, jetzt schon
  ds.close()
  fixedThreadPool.shutdown()
}

