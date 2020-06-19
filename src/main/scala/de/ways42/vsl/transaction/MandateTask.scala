package de.ways42.vsl.transaction

import de.ways42.vsl.tables.mandate.Payment
import de.ways42.vsl.tables.mandate.Mandate
import de.ways42.vsl.tables.mandate.MandateDom
import de.ways42.vsl.tables.mandate.MandateDomOps._
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
	def getNichtTerminierteMandateUndLetztesPayment() : Task[Map[Long, MandateDom]] = {	  	  
	  val ll = getAllMandatesWithPayments()
	  for {
	    mp <- getMapMandateWithLatestPayment( ll)
	  } yield  mp	  
	}

  /**
   * Aggregiert aus der Liste der Mandate und der Liste der Payments eine Mappe zur Mandats-ID
   */
  def buildMapMandateWithLatestPayment( lm:List[Mandate], lp:List[Payment]) : Task[Map[Long, MandateDom]] = for {
	  mm <- Task( MandateDom.aggregateMandateWithEmptyPayment(lm))
	  mp <- Task( MandateDom.aggregateMandateWithPayment(mm,lp))
	} yield  mp	  

	/**
	 * Kombiniert das Laden der Mandate und Payments mit der Assosation in einer Mappe
	 */
	def getMapMandateWithLatestPayment( t: Task[(List[Mandate], List[Payment])]) : Task[Map[Long, MandateDom]] = 
	  for {
	    ll <- t
	    mp <- buildMapMandateWithLatestPayment( ll._1, ll._2)
	  } yield  mp	  

	  /**
	   * 
	   */
	  
  /**
   * Nicht terminierte Mandate mit Payment
   */
	def getNichtTerminierteMandateMitPayment( t: Task[Map[Long, MandateDom]]) : Task[Map[Long, MandateDom]] = 
      t.map(  _.filter( ! _._2.mandateHasNoPayment))
	/**
	 * Nicht terminierte  Mandate ohne Payment
	 */
	def getNichtTerminierteMandateOhnePayment( t: Task[Map[Long, MandateDom]])  : Task[Map[Long, MandateDom]] = 
	  t.map(  _.filter( _._2.mandateHasNoPayment) )
	  
	def getNichtTerminierteAbgelaufeneMandateOhnePayment( t: Task[Map[Long, MandateDom]])  : Task[Map[Long, MandateDom]] = 
	  t.map(   _.filter(_._2.abgelaufenOhnePayment) )
	
	def getNichtTerminierteAbgelaufeneMandateWithPayment( t: Task[Map[Long, MandateDom]])  : Task[Map[Long, MandateDom]] = 
	  t.map(  _.filter( _._2.abgelaufenMitPayment)  )
	/**
	 * Nicht terminierte Abgelaufene Mandate
	 */
	def getNichtTerminierteAbgelaufeneMandate( t: Task[Map[Long, MandateDom]])  : Task[Map[Long, MandateDom]] = 
	  t.map( _.filter( _._2.abgelaufen))

  /**
   * Sequntiell dei Datenbank gefragen
   */
	def getMandateWithPaymentsSlow() : Task[Unit] = {
			Mandate.selectAktAll().flatMap( 
					_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x))))
					).transact(xa).map(println)
	}
}