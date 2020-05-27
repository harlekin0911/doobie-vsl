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

object HcTransactor {

  def get( ec : ExecutionContext, hcc:HikariConfig)( implicit ev: ContextShift[IO])  : IO[HikariTransactor[IO]] = 
      IO.pure(HikariTransactor.apply[IO](new HikariDataSource(hcc), ec, Blocker.liftExecutionContext(ec)))

  def get( ec : ExecutionContext, ds:HikariDataSource)( implicit ev: ContextShift[Task]) : Task[HikariTransactor[Task]] = {
    Task.pure( HikariTransactor.apply[Task](ds, ec, Blocker.liftExecutionContext(ec)))
  }

  def getResource(ds: HikariDataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, HikariTransactor[Task]] = for {
    ec <- ExecutionContexts.fixedThreadPool[Task](size) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield   HikariTransactor.apply[Task](ds, ec, be)

  /**
   * Create a Transactor Resource backed with a Hikari Pool
   */
  def getResource(ds: DataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, DataSourceTransactor[Task]] = for {
    ce <- ExecutionContexts.fixedThreadPool[Task](size) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield Transactor.fromDataSource[Task](ds, ce, be)

  /**
   * The same with flatMap
   */
  def getResource2(ds: DataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, DataSourceTransactor[Task]] = 
    ExecutionContexts.fixedThreadPool[Task](size).flatMap(ce => Blocker[Task].map( be =>  Transactor.fromDataSource[Task](ds, ce, be))) 

  def main( args:Array[String]) : Unit = {
    //import monix.execution.Scheduler.Implicits.global
    implicit val sc = monix.execution.Scheduler.io( "Monix-Pool")
    val ds = HcConfig.getDataSource(SiteConfig.dbConf)
    
    val xa = getResource( ds, 32)
    val c =  xa.use( xa => sql"select count(*) from vsmadm.tvsl001".query[Int].unique.transact(xa))
    
    val d = getResource2( ds, 3).use( x => sql"select count(*) from vsmadm.tvsl002".query[Int].unique.transact(x))
    
    println(c.runSyncUnsafe())
    println(d.runSyncUnsafe())
  }
  
    // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  def transactor(implicit ev: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] = for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO]    // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
//              "org.h2.Driver",                        // driver classname
//              "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",   // connect URL
//              "sa",                                   // username
//              "",                                     // password
					"com.ibm.db2.jcc.DB2Driver", // driver classname
//					"jdbc:db2://172.17.4.39:50001/vslt01", // connect URL (driver-specific)
					"jdbc:db2://172.17.4.39:50013/vslt03", // connect URL (driver-specific)
					"vsmadm",              // user
					"together",                       // password
              ce,                                     // await connection here
              be                                      // execute JDBC operations here
            )
    } yield xa

}

