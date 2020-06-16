package de.ways42.vsl.service


import java.sql.Date
import java.util.GregorianCalendar

import cats._
import cats.effect._
import cats.implicits._

import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import monix.eval.Task
import monix.execution.Scheduler

import de.ways42.vsl.tables.mandate.Payment
import de.ways42.vsl.tables.mandate.Mandate
import de.ways42.vsl.tables.mandate.BusinessObjectRef
import java.util.Calendar

object MandateService {
  
  /**
   * Datum der letzen Erneuerung der Gueltigkeit
   */
	def getLastValidationDate( m : Mandate, p : Option[Payment]) : Option[Date] = {
	  p.flatMap( _.SCHEDULED_DUE_DATE match {
	    case None => m.SIGNED_DATE
	    case y    => y})
	}
	  
  /**
   * Mandate mit letztem Payment aelter als 3 Jahre ?
   */
	def istMandateAbgelaufen( m:Mandate, p:Option[Payment]) : Boolean = {
	  val d = TimeService.getCurrentTimeYearsBefore( 3)
		val v = getLastValidationDate(m,p).getOrElse( (new GregorianCalendar( 1900, 1, 1)).getTime())
			
		v.compareTo( d) >= 0			 			  			  
	}
	
		/**
	 * Aktuelles Mandat mit allen zugehoerigen Payments laden, falls vorhanden
	 */
	def getMandateWithPayments( mandateId : Long) : ConnectionIO[(Option[Mandate], List[Payment])] = Mandate.selectAktById( mandateId).flatMap({
	    case Some(m) => Payment.selectAllByMandateId(mandateId).map( l => (Some(m),l))
	    case None    => ( Option.empty[Mandate], List.empty[Payment]).pure[ConnectionIO]})

	
	/**
	 * Mappe mit leeren Payment aufbauen
	 */
	def aggregateMandateWithEmptyPayment( ml:List[Mandate]) : Map[Long, (Mandate, Option[Payment])] = 
	  ml.foldRight( Map.empty[Long,( Mandate, Option[Payment])])((m,z) => z.updated(m.MANDATE_ID, (m,None)))
	  
	/**
	 * Paments in die Mappe fuellen
	 */
	def aggregateMandateWithPayment( mm : Map[Long, (Mandate, Option[Payment])], pl : List[Payment])  : Map[Long, (Mandate, Option[Payment])] = 
	    pl.foldRight(mm)( (p,m) =>  m.get(p.MANDATE_ID)  match { 
			  case Some(k)   => m.updated(p.MANDATE_ID, (k._1, Some(p)))
			  case _         => m
			})
			
	/**
	 * Filter Mandate ohne Payments
	 */
	def mandateHasNoPayment( mp : (Mandate, Option[Payment])) : Boolean = mp._2.isEmpty

	
	  
	/**
	 * Mandate mit ihrem letzten Payment anreichern falls vorhanden 
	 */
	def getNichtTerminierteMandateUndLetztesPayment()  : ConnectionIO[Map[Long, (Mandate, Option[Payment])]] = {

	  for {
	    lm <- Mandate.selectAktAllNotTerminated()
	    mm <- aggregateMandateWithEmptyPayment(lm).pure[ConnectionIO]
	    lp <- Payment.selectLastPaymentAlle()
	    mp <-  aggregateMandateWithPayment( mm, lp).pure[ConnectionIO]
	  } yield mp

	  // Mandate.selectAktAllNotTerminated().flatMap( 
	  //   lm => aggregateMandateWithEmptyPayment(lm).pure[ConnectionIO].flatMap(
	  //	   mm => Payment.selectLastPaymentAlle().flatMap( 
	  //	     lp => mandateHasNoPayment( aggregateMandateWithPayment( mm, lp)).pure[ConnectionIO])))
	}
	
  def getEntryWithPayment( m:Map[Long, (Mandate, Option[Payment])]) =  m.filter( e => ! mandateHasNoPayment( e._2)) 
  def getEntryOhnePayment( m:Map[Long, (Mandate, Option[Payment])]) =  m.filter( e =>   mandateHasNoPayment( e._2)) 
  def getEntryNotTerminatedAbgelaufen( m:Map[Long, (Mandate, Option[Payment])]) =  m.filter( e => istMandateAbgelaufen( e._2._1, e._2._2)) 
    
  def getNichtTerminierteMandateMitPayment() : ConnectionIO[Map[Long, (Mandate, Option[Payment])]] = 
    getNichtTerminierteMandateUndLetztesPayment.flatMap(  getEntryWithPayment(_).pure[ConnectionIO] )
	/**
	 * Gueltige Mandate ohne Payment
	 */
	def getNichtTerminierteMandateOhnePayment()  : ConnectionIO[Map[Long, (Mandate, Option[Payment])]] = 
	  getNichtTerminierteMandateUndLetztesPayment.flatMap(  getEntryOhnePayment(_).pure[ConnectionIO] )
	  
	/**
	 * 
	 */
	def getNichtTerminierteAbgelaufeneMandate()  : ConnectionIO[Map[Long, (Mandate, Option[Payment])]] = 
	  getNichtTerminierteMandateUndLetztesPayment.flatMap( getEntryNotTerminatedAbgelaufen(_).pure[ConnectionIO])
	  
	/**
	 * Mandate mit ihrem letzten Payment anreichern falls vorhanden 
	 */

	def getMandateWithPaymentsSlow() = Mandate.selectAktAll().flatMap( 
				_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x)))))
}