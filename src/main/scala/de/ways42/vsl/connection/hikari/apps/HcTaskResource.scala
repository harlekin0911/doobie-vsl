package de.ways42.vsl.connection.hikari.apps

import de.ways42.vsl.connection.hikari.HcConfig
import de.ways42.vsl.connection.hikari.HcResource
import de.ways42.vsl.connection.SiteConfig

import doobie.implicits._

/**
 *  https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html
 */
object HcTaskResource  {
  
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

