package de.ways42.vsl.transaction

import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.Mandate
import de.ways42.vsl.domains.mandate.tables.BusinessObjectRef
import de.ways42.vsl.domains.mandate.domain.MandateAktDom
import de.ways42.vsl.domains.mandate.domain.MandateAktDom._
import java.sql.Date
import doobie.util.transactor.Transactor

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import java.util.GregorianCalendar
import monix.eval.Task
import monix.execution.Scheduler
import de.ways42.vsl.domains.mandate.service.PaymentService
import de.ways42.vsl.domains.mandate.service.MandateService
import de.ways42.vsl.domains.mandate.domain.MandateDom
import de.ways42.vsl.domains.mandate.domain.BusinessObjectRefDom
import de.ways42.vsl.domains.mandate.domain.MandateDomain


object MandateTask {
  
  def apply( xa : Transactor.Aux[Task, Unit]) : MandateTask = new MandateTask( xa)

}
  
class MandateTask( val xa : Transactor.Aux[Task, Unit]) {

  /**
   * Ein Mandat mit den zugehoerigen Payments parallel laden
   */
  def getMandateWithPayments( mandateId : Long) : Task[(Option[Mandate], List[Payment])] = Task.parZip2(
	    Mandate.selectAktById( mandateId).transact(xa),
	    Payment.selectAllByMandateId(mandateId).transact(xa))
  	
	/**
	 * Alle Mandate mit ihrem letzten Payment falls vorhanden parallel laden 
	 */
  def getAllMandatesWithPayments() : Task[(List[Mandate], List[Payment])] = Task.parZip2(
	    Mandate.selectAktAllNotTerminated().transact(xa),
	    Payment.selectLastPaymentAlle().transact(xa))

	/**
   * Get all MandateExt actual
   */
  def getAllMandateDomainAkt()  : Task[(List[BusinessObjectRef], List[Mandate], List[Payment])] = Task.parZip3(
      BusinessObjectRef.selectAktAll().transact(xa),
      Mandate.selectAktAll().transact(xa),
      Payment.selectLastPaymentAlle().transact(xa))
  
  /**
   * Alle MandateDomain, top Down aufbauen
   */
  def getAllMandateDomainAktTopDown()  : Task[Map[String,MandateDomain]] = getAllMandateDomainAkt().map( x => 
    MandateDomain.apply( x._1, x._2, x._3))
                
  /**
   * Alle MandateDomain, Bottom Up aufbauen
   */
  def getAllMandateDomainAktBottomUp()  : Task[Map[String,MandateDomain]] = getAllMandateDomainAkt.map( x =>
        MandateDomain(
            BusinessObjectRefDom.aggregateListBusinessObjectRefDom( 
                x._1, MandateDom.aggregateMandateWithPayment( x._2, MandateDom.aggregatePayments(x._3)))))
  

	/**
	 * Alle nicht terminierten Mandate mit ihrem letzten Payment parallel laden und eine Mappe bilden 
	 * Kombiniert das Laden der Mandate und Payments mit der Assosation in einer Mappe
	 */
	def getMapMandateWithLatestPayment() : Task[Map[Long, MandateAktDom]] = 	  	  
	  getAllMandatesWithPayments().map( ll => 
	    MandateAktDom.buildMapMandateWithLatestPayment( ll._1, ll._2))  
 
  /**
   * Sequntiell dei Datenbank gefragen
   */
	def getMandateWithPaymentsSlow() : Task[Unit] = {
			Mandate.selectAktAll().flatMap( 
					_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x))))
					).transact(xa).map(println)
	}
}