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

object HcResource {


  def apply(ds: HikariDataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, HikariTransactor[Task]] = for {
    ec <- ExecutionContexts.fixedThreadPool[Task](size) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield   HikariTransactor.apply[Task](ds, ec, be)

  /**
   * Create a Transactor Resource backed with a Hikari Pool
   */
  def apply(ds: DataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, DataSourceTransactor[Task]] = for {
    ce <- ExecutionContexts.fixedThreadPool[Task](size) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield Transactor.fromDataSource[Task](ds, ce, be)

  /**
   * The same with flatMap
   */
  def apply2(ds: DataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, DataSourceTransactor[Task]] = 
    ExecutionContexts.fixedThreadPool[Task](size).flatMap(ce => Blocker[Task].map( be =>  Transactor.fromDataSource[Task](ds, ce, be))) 

  
  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  // 
  // "org.h2.Driver",                        // driver classname
  // "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",   // connect URL
  // "sa",                                   // username
  // "",                                     // password

  def apply(driver:String, url:String, user:String, passwd:String)(implicit ev: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] = for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO]    // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](driver, url, user, passwd,
              ce, // await connection here
              be) // execute JDBC operations here
    } yield xa

def main( args:Array[String]) : Unit = {
    //import monix.execution.Scheduler.Implicits.global
    implicit val sc = monix.execution.Scheduler.io( "Monix-Pool")
    val ds = HcConfig.getDataSource(SiteConfig.dbConf)
    
    val xa = HcResource( ds, 32)
    val c =  xa.use( xa => sql"select count(*) from vsmadm.tvsl001".query[Int].unique.transact(xa))
    
    val d = HcResource.apply2( ds, 3).use( x => sql"select count(*) from vsmadm.tvsl002".query[Int].unique.transact(x))
    
    println(c.runSyncUnsafe())
    println(d.runSyncUnsafe())
  }

}

