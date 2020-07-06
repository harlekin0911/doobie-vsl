package de.ways42.vsl.domains.vsl.transaction

import de.ways42.vsl.domains.vsl.service.VslService
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import doobie.implicits.toConnectionIOOps
import doobie.util.transactor.Transactor
import monix.eval.Task


object VslTask {
  
  def apply( xa : Transactor.Aux[Task, Unit]) : VslTask = new VslTask( xa)

}
  
class VslTask( val xa : Transactor.Aux[Task, Unit]) {
  
  /**
   * Einen aktuellen Vertrag mit den zugehoerigen aktuellen Versicherungen parallel laden
   */
  def getVertragWithVersicherung( vtgnr : String) : Task[(Option[Tvsl001], List[Tvsl002])] = Task.parZip2(
	    Tvsl001.selectAktById( vtgnr).transact(xa),
	    Tvsl002.selectAktZuVertrag(vtgnr).transact(xa))
  	
	/**
	 * Alle aktuellen aufrechten Vertraege mit ihren aktuellen Versicherungen parallel laden 
	 */
  def getAllActiveVertraegeWithVersicherungen() : Task[(List[Tvsl001], List[Tvsl002])] = Task.parZip2(
	    Tvsl001.selectAktAllAktive().transact(xa),
	    Tvsl002.selectAktAktiveAll().transact(xa))

	/**
	 * Alle aktiven, aufrechten Vertraege mit ihren aktiven Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
	def getAktiveVertraegeMitAktVersicherungen() : Task[Map[String, (Tvsl001, Map[Short,Tvsl002])]] = for {
	    ll <- getAllActiveVertraegeWithVersicherungen()
	  } yield  VslService.buildAktVertraegeMitVersicherungen( ll._1, ll._2)	
}