package de.ways42.vsl.domains.vsl.transaction

import de.ways42.vsl.domains.vsl.service.VslService
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import doobie.implicits.toConnectionIOOps
import doobie.util.transactor.Transactor
import monix.eval.Task
import de.ways42.vsl.domains.vsl.domain.VslDom
import de.ways42.vsl.domains.vsl.domain.MandateRefDom


object VslTask {
  
  def apply[A]( xa : Transactor.Aux[Task, A]) : VslTask[A] = new VslTask[A]( xa)

}
  
class VslTask[A]( val xa : Transactor.Aux[Task, A]) {
  
  /**
   * Einen aktuellen Vertrag mit den zugehoerigen aktuellen Versicherungen parallel laden
   */
  def getSingleVslDom( vtgnr : String) : Task[Option[VslDom]] = {
    val (v,lvers) = VslService.getVertragWithVersicherung(vtgnr)
    Task.parZip2(v.transact(xa),lvers.transact(xa)).map( x => x._1 match { 
      case Some(h) => Some(VslDom( h, x._2)); 
      case None    => None
    })
  }
  	
	/**
	 * Alle aktiven, aufrechten Vertraege mit ihren aktiven Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
	def getAllAktiveVslDom() : Task[Map[String, VslDom]] = {
    val (la,lb) = VslService.getAllActiveVertraegeWithVersicherungen()
    Task.parZip2(la.transact(xa),lb.transact(xa)).map( x => VslDom( x._1, x._2)	)
	}
	
	def getSingleMandateRefDom( vtgnr:String) : Task[Option[MandateRefDom]] = {
	  val ( la,lb,lc) = VslService.getAktVertragWithVersicherungMandate(vtgnr)
	  Task.parZip3(la.transact(xa),lb.transact(xa), lc.transact(xa)).map({
	    case (Some(x),y,z) => Some(MandateRefDom( x, y, z))	
	    case  _ => None
	    })
	}
	/**
	 * Alle aktiven, aufrechten Vertraege mit ihren aktiven Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
  def getAllAktiveMandateRefDom() : Task[Map[String, MandateRefDom]] = {
	  val ( la,lb,lc) = VslService.getAllActiveVertraegeWithVersicherungenMandate()
	  Task.parZip3(la.transact(xa),lb.transact(xa), lc.transact(xa)).map( x => MandateRefDom( x._1, x._2, x._3)	)
	}
		
	/**
	 * Alle aufrechten Vertraege mit ihren  Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
  def getAllMandateRefDom() : Task[Map[String, MandateRefDom]] = {
	  val ( la,lb,lc) = VslService.getAllVertraegeWithVersicherungenMandate()
	  Task.parZip3(la.transact(xa),lb.transact(xa), lc.transact(xa)).map( x => MandateRefDom( x._1, x._2, x._3)	)
	}
}