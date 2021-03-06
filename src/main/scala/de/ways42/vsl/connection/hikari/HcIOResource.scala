package de.ways42.vsl.connection.hikari

import javax.sql.DataSource

import com.zaxxer.hikari.HikariDataSource
import doobie._
import doobie.implicits._
import doobie.util.transactor

import cats.effect._
import cats.implicits._
import doobie.hikari._


//import scalaz.concurrent.Task
import monix.eval.Task
import de.ways42.vsl.connection.SiteConfig
import com.zaxxer.hikari.HikariConfig
import scala.concurrent.ExecutionContext
import de.ways42.vsl.connection.JdbcOptions

object HcIOResource {


  def apply(ds: HikariDataSource, size:Int)( implicit ev: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](size) // our connect EC
    be <- Blocker[IO]    // our blocking EC
  } yield   HikariTransactor.apply[IO](ds, ec, be)

  /**
   * Create a Transactor Resource backed with a Hikari Pool
   */
  def apply(ds: DataSource, size:Int)( implicit ev: ContextShift[IO]): Resource[IO, DataSourceTransactor[IO]] = for {
    ce <- ExecutionContexts.fixedThreadPool[IO](size) // our connect EC
    be <- Blocker[IO]    // our blocking EC
  } yield Transactor.fromDataSource[IO](ds, ce, be)

  /**
   * The same with flatMap
   */
  def apply2(ds: DataSource, size:Int)( implicit ev: ContextShift[IO]): Resource[IO, DataSourceTransactor[IO]] = 
    ExecutionContexts.fixedThreadPool[IO](size).flatMap(ce => Blocker[IO].map( be =>  Transactor.fromDataSource[IO](ds, ce, be))) 

  
  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  // 
  // "org.h2.Driver",                        // driver classname
  // "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",   // connect URL
  // "sa",                                   // username
  // "",                                     // password


  def apply(driver:String, url:String, user:String, passwd:String)(implicit ev: ContextShift[IO]) : Resource[IO, HikariTransactor[IO]] = for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO]    // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](driver, url + JdbcOptions.db2Options, user, passwd,
              ce, // await connection here
              be) // execute JDBC operations here
    } yield xa    
}

