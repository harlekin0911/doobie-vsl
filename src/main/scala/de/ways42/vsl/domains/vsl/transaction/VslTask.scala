package de.ways42.vsl.domains.vsl.transaction

import de.ways42.vsl.domains.vsl.domain.VslDom
import de.ways42.vsl.domains.vsl.domain.VslRefDom
import de.ways42.vsl.domains.vsl.service.VslService
import doobie.implicits.toConnectionIOOps
import doobie.util.transactor.Transactor
import monix.eval.Task


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
	
	def getSingleMandateRefDom( vtgnr:String) : Task[Option[VslRefDom]] = {
	  val ( la,lb,lc) = VslService.getAktVertragWithVersicherungMandate(vtgnr)
	  Task.parZip3(la.transact(xa),lb.transact(xa), lc.transact(xa)).map({
	    case (Some(x),y,z) => Some(VslRefDom( x, y, z))	
	    case  _ => None
	    })
	}
	/**
	 * Alle aktiven, aufrechten Vertraege mit ihren aktiven Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
  def getAllAktiveMandateRefDom() : Task[Map[String, VslRefDom]] = {
	  val ( la,lb,lc) = VslService.getAllActiveVertraegeWithVersicherungenMandate()
	  Task.parZip3(la.transact(xa),lb.transact(xa), lc.transact(xa)).map( x => VslRefDom( x._1, x._2, x._3)	)
	}
		
	/**
	 * Alle aufrechten Vertraege mit ihren  Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
  def getAllMandateRefDom() : Task[Map[String, VslRefDom]] = {
	  val ( la,lb,lc) = VslService.getAllVertraegeWithVersicherungenMandate()
	  Task.parZip3(la.transact(xa),lb.transact(xa), lc.transact(xa)).map( x => VslRefDom( x._1, x._2, x._3)	)
	}
}