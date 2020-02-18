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
//    ds.setMetricRegistry(     Metrics.metricRegistry)
//    ds.setHealthCheckRegistry(Metrics.healthCheckRegistry)
//    ds.addHealthCheckProperty("expected99thPercentileMs", "100")
    sys.addShutdownHook(     ds.close())
    ds
  }

//  lazy val xa = DataSourceTransactor[Task].apply(HCPool2.dataSource)
  lazy val xa = for {
    ce <- ExecutionContexts.fixedThreadPool[Task](32) // our connect EC
    be <- Blocker[Task]    // our blocking EC
  } yield Transactor.fromDataSource[Task](dataSource, ce, be)
}

//def transactor(ds: DataSource)(
//  implicit ev: ContextShift[IO]
//): Resource[IO, DataSourceTransactor[IO]] =
//  for {
//    ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
//    be <- Blocker[IO]    // our blocking EC
//  } yield Transactor.fromDataSource[IO](ds, ce, be)

