package de.ways42.vsl.domains.vslMandate.transaction

import de.ways42.vsl.domains.mandate.transaction.MandateTask
import de.ways42.vsl.domains.vsl.transaction.VslTask
import de.ways42.vsl.domains.vslMandate.domain.VslMandateDomain
import de.ways42.vsl.domains.zik.transaction.ZikTask
import doobie.util.transactor.Transactor
import monix.eval.Task


object VslMandateTask {
  
  def apply[A]( xa : Transactor.Aux[Task, A]) : VslMandateTask[A] = new VslMandateTask[A]( xa)
}
  
class VslMandateTask[A]( val xa : Transactor.Aux[Task, A]) {
  
  /**
   * Einen aktuellen Vertrag mit dem zugehoerigen Mandate laden
   */
  def getSingle( vtgnr:String) : Task[VslMandateDomain] = Task.parZip3(
	    VslTask(xa).getSingleMandateRefDom(vtgnr), 
	    MandateTask(xa).getAktSingleMandateDomainAllPayments(vtgnr),
	    ZikTask(xa).getSingleZikDomainVertrag(vtgnr)
	  ).map( x => VslMandateDomain( vtgnr, x._1, Some(x._2), x._3))
	
	/**
	 * Alle aktiven, aufrechten Vertraege mit ihren aktiven Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
	def getAllAktive() : Task[Map[String, VslMandateDomain]] = Task.parZip3(
	    VslTask(xa).getAllAktiveMandateRefDom(), 
	    MandateTask(xa).getAktAllAktiveMandateDomain(),
	    ZikTask(xa).getAktAllZikDomain()
	  ).map( x => VslMandateDomain( x._1, x._2, x._3))

	/**
	 * Alle aufrechten Vertraege mit ihren  Versicherungen 
	 * parallel laden und eine Mappe bilden 
	 */
	def getAll() : Task[Map[String, VslMandateDomain]] = Task.parZip3(
	    VslTask(xa).getAllMandateRefDom(), 
	    MandateTask(xa).getAktAllMandateDomain(),
	    ZikTask(xa).getAllZikDomain()
	  ).map( x => VslMandateDomain( x._1, x._2, x._3))
}