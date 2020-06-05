package de.ways42.vsl.transaction

import de.ways42.vsl.tables.mandate.Payment
import de.ways42.vsl.tables.mandate.Mandate
import java.sql.Date
import de.ways42.vsl.tables.mandate.BusinessObjectRef
import doobie.util.transactor.Transactor

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import java.util.GregorianCalendar
import monix.eval.Task
import monix.execution.Scheduler
import de.ways42.vsl.service.PaymentService
import de.ways42.vsl.service.MandateService


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
	 * Alle nicht terminierten Mandate mit ihrem letzten Payment parallel laden und eine Mappe bilden 
	 */
	def getNichtTerminierteMandateUndLetztesPayment() : Task[Map[Long, (Mandate, Option[Payment])]] = {	  	  
	  val ll = getAllMandatesWithPayments()
	  for {
	    mp <- getMapMandateWithLatestPayment( ll)
	  } yield  mp	  
	}

  /**
   * Aggregiert aus der Liste der Mandate und der Liste der Payments eine Mappe zur Mandats-ID
   */
  def buildMapMandateWithLatestPayment( lm:List[Mandate], lp:List[Payment]) : Task[Map[Long, (Mandate, Option[Payment])]] = for {
	  mm <- Task( MandateService.aggregateMandateWithEmptyPayment(lm))
	  mp <- Task( MandateService.aggregateMandateWithPayment(mm,lp))
	} yield  mp	  

	/**
	 * Kombiniert das Laden der Mandate und Payments mit der Assosation in einer Mappe
	 */
	def getMapMandateWithLatestPayment( t: Task[(List[Mandate], List[Payment])]) : Task[Map[Long, (Mandate, Option[Payment])]] = 
	  for {
	    ll <- t
	    mp <- buildMapMandateWithLatestPayment( ll._1, ll._2)
	  } yield  mp	  

  /**
   * Nicht terminierte Mandate mit Payment
   */
	def getNichtTerminierteMandateMitPayment( t: Task[(List[Mandate], List[Payment])]) : Task[Map[Long, (Mandate, Option[Payment])]] = 
      getMapMandateWithLatestPayment(t).flatMap(  _.filter( e => ! MandateService.mandateHasNoPayment( e._2)).pure[Task] )
	/**
	 * Nicht terminierte  Mandate ohne Payment
	 */
	def getNichtTerminierteMandateOhnePayment( t: Task[(List[Mandate], List[Payment])])  : Task[Map[Long, (Mandate, Option[Payment])]] = 
	  getMapMandateWithLatestPayment(t).flatMap(  _.filter( e => MandateService.mandateHasNoPayment( e._2)).pure[Task] )
	  
	/**
	 * Nicht terminierte Abgelaufene Mandate
	 */
	def getNichtTerminierteAbgelaufeneMandate( t: Task[(List[Mandate], List[Payment])])  : Task[Map[Long, (Mandate, Option[Payment])]] = 
	  getMapMandateWithLatestPayment(t).flatMap( _.filter( e => MandateService.istMandateAbgelaufen( e._2._1, e._2._2)).pure[Task])

  /**
   * Sequntiell dei Datenbank gefragen
   */
	def getMandateWithPaymentsSlow() : Task[Unit] = {
			Mandate.selectAktAll().flatMap( 
					_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x))))
					).transact(xa).map(println)
	}
}