package de.ways42.vsl.connection

import javax.sql.DataSource

//import com.onur.moviedb.conf.SiteConfig
//import com.onur.moviedb.metric.Metrics
import com.zaxxer.hikari.HikariDataSource
import doobie._
import doobie.implicits._
import doobie.util.transactor

import cats.effect._
import cats.implicits._
import doobie.hikari._


//import scalaz.concurrent.Task
import monix.eval.Task

object HCPool2 {


  lazy val dataSource: DataSource = {
    val ds = new HikariDataSource
    val dbConf = SiteConfig.dbConf
    val urlBuilder = new StringBuilder("jdbc:db2://")
    urlBuilder.append(dbConf.host)
      .append(":")
      .append(dbConf.port).append("/")
      .append(dbConf.name)
      .append(dbConf.urlOptions)
//      .append("&prepStmtCacheSize=250")
//      .append("&prepStmtCacheSqlLimit=2048")

    ds.setJdbcUrl(            urlBuilder.toString)
    ds.setUsername(           dbConf.username)
    ds.setPassword(           dbConf.password)
    ds.setConnectionTimeout(  dbConf.connectionTimeout)
    ds.setIdleTimeout(        dbConf.idleTimeout)
    ds.setMaximumPoolSize(    dbConf.connectionCount)
    ds.setMaxLifetime(        dbConf.maxLifetime)
    ds.setPoolName(           dbConf.poolName)
    ds.setRegisterMbeans(     true)
    ds.setValidationTimeout(  dbConf.validationTimeout)
    ds.setDriverClassName("com.ibm.db2.jcc.DB2Driver")
//    ds.setMetricRegistry(     Metrics.metricRegistry)
//    ds.setHealthCheckRegistry(Metrics.healthCheckRegistry)
//    ds.addHealthCheckProperty("expected99thPercentileMs", "100")
    sys.addShutdownHook(     ds.close())
    ds
  }

//  lazy val xa = DataSourceTransactor[Task].apply(HCPool2.dataSource)
  lazy val xa : Resource[Task, Transactor.Aux[Task, DataSource]] = for {
    ce <- ExecutionContexts.fixedThreadPool[Task](32) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield Transactor.fromDataSource[Task](dataSource, ce, be)

  def transactor(ds: DataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, DataSourceTransactor[Task]] = for {
    ce <- ExecutionContexts.fixedThreadPool[Task](size) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield Transactor.fromDataSource[Task](ds, ce, be)

  /**
   * The same with flatMap
   */
  def transactor2(ds: DataSource, size:Int)( implicit ev: ContextShift[Task]): Resource[Task, DataSourceTransactor[Task]] = 
    ExecutionContexts.fixedThreadPool[Task](size).flatMap(ce => Blocker[Task].map( be =>  Transactor.fromDataSource[Task](ds, ce, be))) 

  def main( args:Array[String]) : Unit = {
    val c =  xa.use( xa => sql"select count(*) from vsmadm.tvsl001".query[Int].unique.transact(xa))
    val d = transactor( dataSource, 3).use( x => sql"select count(*) from vsmadm.tvsl002".query[Int].unique.transact(x))
    //import monix.execution.Scheduler.Implicits.global
    implicit val sc = monix.execution.Scheduler.io( "Monix-Pool")
    println(c.runSyncUnsafe())
    println(d.runSyncUnsafe())
  }
}

