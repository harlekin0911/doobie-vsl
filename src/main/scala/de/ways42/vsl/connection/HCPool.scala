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

object HCPool extends App {



  // hikari pooling config with mysql
  val config = new HikariConfig()
  config.setJdbcUrl("jdbc:db2://172.17.4.39:50001/vslt01")
  config.setUsername("vsmadm")
  config.setPassword("together")
  config.setMaximumPoolSize(5)
  config.setDriverClassName("com.ibm.db2.jcc.DB2Driver")

  
  implicit val cs = IO.contextShift(ExecutionContext.global)
  // transactor with config
  val transactor: IO[HikariTransactor[IO]] =
//    IO.pure(HikariTransactor.apply[IO](new HikariDataSource(config), ExecutionContexts.fixedThreadPool[IO](32), Blocker[IO]))
    IO.pure(HikariTransactor.apply[IO](new HikariDataSource(config), ExecutionContext.global, Blocker.liftExecutionContext(ExecutionContext.global)))

  // create table
  val c = for {
    xa <- transactor
    result <- sql"select 41 from sysibm.sysdummy1".query[Int].unique.transact(xa)
  } yield result
  println(c.unsafeRunSync())
}

