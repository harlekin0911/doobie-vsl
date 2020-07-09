package de.ways42.vsl.domains.vsl.transaction

import de.ways42.vsl.domains.vsl.service.VslService
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import doobie.implicits.toConnectionIOOps
import doobie.util.transactor.Transactor
import monix.eval.Task
import de.ways42.vsl.domains.vsl.domain.VslDom


object VslTask {
  
  def apply[A]( xa : Transactor.Aux[Task, A]) : VslTask[A] = new VslTask[A]( xa)

}
  
class VslTask[A]( val xa : Transactor.Aux[Task, A]) {
  
  /**
   * Einen aktuellen Vertrag mit den zugehoerigen aktuellen Versicherungen parallel laden
   */
  def getVertragWithVersicherung( vtgnr : String) : Task[Option[VslDom]] = { //: Task[(Option[Tvsl001], List[Tvsl002])] = {
    val (v,lvers) = VslService.getVertragWithVersicherung(vtgnr)
    Task.parZip2(v.transact(xa),lvers.transact(xa)).map( x => x._1 match { 
      case Some(h) => Some(VslDom( h, x._2)); 
      case None    => None
    })
  }
//  Task.parZip2(
//	    Tvsl001.selectAktById( vtgnr).transact(xa),
//	    Tvsl002.selectAktZuVertrag(vtgnr).transact(xa))
  	
	/**
	 * Alle aktuellen aufrechten Vertraege mit ihren aktuellen Versicherungen parallel laden 
	 */
//  def getAllActiveVertraegeWithVersicherungen() : Task[(List[Tvsl001], List[Tvsl002])] = {
//    val (la,lb) = VslService.getAllActiveVertraegeWithVersicherungen()
//    Task.parZip2(la.transact(xa),lb.transact(xa))
//  }
//	    Tvsl001.selectAktAllAktive().transact(xa),
//	    Tvsl002.selectAktAktiveAll().transact(xa))

	/**
	 * Alle aktiven, aufrechten Vertraege mit ihren aktiven Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
	def getAktiveVertraegeMitAktVersicherungen() : Task[Map[String, VslDom]] = {
    val (la,lb) = VslService.getAllActiveVertraegeWithVersicherungen()
    Task.parZip2(la.transact(xa),lb.transact(xa)).map( x => VslDom( x._1, x._2)	)
	}
//	  getAllActiveVertraegeWithVersicherungen().map( x => VslDom( x._1, x._2)	)
}