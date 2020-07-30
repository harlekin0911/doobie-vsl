package de.ways42.vsl.connection.hikari

import de.ways42.vsl.connection.SiteConfig.DbConf
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import com.zaxxer.hikari.HikariConfig
import de.ways42.vsl.connection.JdbcOptions

object HcConfig {

//  def hcConfig() : HikariConfig = {
//    val config = new HikariConfig()
//    config.setJdbcUrl("jdbc:db2://172.17.4.39:50001/vslt01")
//    config.setUsername("vsmadm")
//    config.setPassword("together")
//    config.setMaximumPoolSize(32)
//    config.setDriverClassName("com.ibm.db2.jcc.DB2Driver")
//    config
//  }
  
  // hikari pooling config
  def hcConfig(driver:String, url:String, user:String, passwd:String, size:Int) : HikariConfig = {
      val config = new HikariConfig()
          config.setJdbcUrl(         url +  JdbcOptions.db2Options)
          config.setUsername(        user)
          config.setPassword(        passwd)
          config.setMaximumPoolSize( size)
          config.setDriverClassName( driver)
          config

  }

  def getDataSource( hcc:HikariConfig) : HikariDataSource = new HikariDataSource(hcc)

  /**
   * Open the Hikari-Pool
   */
  def getDataSource( dbCfg : DbConf) : HikariDataSource = {

    val ds = new HikariDataSource()        
         
    val url = new StringBuilder("jdbc:db2://").append(dbCfg.host)
          .append(":")
          .append(dbCfg.port).append("/")
          .append(dbCfg.name)
          .append(dbCfg.urlOptions)
          //.append("&prepStmtCacheSize=250")
          //.append("&prepStmtCacheSqlLimit=2048")

      ds.setJdbcUrl(            url.toString)
      ds.setUsername(           dbCfg.username)
      ds.setPassword(           dbCfg.password)
      ds.setConnectionTimeout(  dbCfg.connectionTimeout)
      ds.setIdleTimeout(        dbCfg.idleTimeout)
      ds.setMaximumPoolSize(    dbCfg.connectionCount)
      ds.setMaxLifetime(        dbCfg.maxLifetime)
      ds.setPoolName(           dbCfg.poolName)
      ds.setRegisterMbeans(     true)
      ds.setValidationTimeout(  dbCfg.validationTimeout)
      ds.setDriverClassName(    dbCfg.driver)
//    ds.setMetricRegistry(     Metrics.metricRegistry)
//    ds.setHealthCheckRegistry(Metrics.healthCheckRegistry)
//    ds.addHealthCheckProperty("expected99thPercentileMs", "100")
          
      sys.addShutdownHook(     ds.close())
      ds
  }


}