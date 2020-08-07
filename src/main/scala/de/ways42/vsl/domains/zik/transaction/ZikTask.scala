package de.ways42.vsl.domains.zik.transaction

import de.ways42.vsl.domains.vsl.service.VslService
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import doobie.implicits.toConnectionIOOps
import doobie.util.transactor.Transactor
import monix.eval.Task
import de.ways42.vsl.domains.vsl.domain.VslDom
import de.ways42.vsl.domains.vsl.domain.MandateRefDom
import de.ways42.vsl.domains.zik.service.ZikService
import de.ways42.vsl.domains.zik.domain.ZikDomain


object ZikTask {
  
  def apply[A]( xa : Transactor.Aux[Task, A]) : ZikTask[A] = new ZikTask[A]( xa)

}
  
class ZikTask[A]( val xa : Transactor.Aux[Task, A]) {
  
  /**
   * Ein aktuelles Nebenkonto (Vertrag + Mandant) mit den zugehoerigen Stammdaten laden
   */
  def getSingleZikDomainNkto( nktoNr:String) : Task[Map[String,ZikDomain]] = {
    ZikService.getAktNktoStammDatenByNkto(nktoNr).transact(xa).map( x => ZikDomain(x))
  }
  
  def getSingleZikDomainVertrag( vtgnr:String) : Task[Map[String,ZikDomain]] = {
    ZikService.getAktNktoStammDatenByVertrag(vtgnr).transact(xa).map( x => ZikDomain(x))
  }
  
  /**
   * Alle aktuellen Stammdaten laden
   */
  def getAktAllZikDomain() : Task[Map[String,ZikDomain]] = {
    ZikService.getAktAll().transact(xa).map( x => ZikDomain(x))
  }
  
  /**
   * Allle Stammdaten laden, also mit Historie ist das sinnvoll?
   */
  def getAllZikDomain() : Task[Map[String,ZikDomain]] = {
    ZikService.getAll().transact(xa).map( x => ZikDomain(x))
  }
}
