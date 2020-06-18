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
import de.ways42.vsl.tables.mandate.MandatePayment
import de.ways42.vsl.tables.mandate.BusinessObjectRef
import java.util.Calendar


object MandateService {
  
  sealed trait Qual  
  final case object NtNoPayment    extends Qual
  final case object NtPayment      extends Qual
  final case object NtOod          extends Qual
  final case object NtOodPayment   extends Qual
  final case object NtOodNoPayment extends Qual
  
  def catfn( f: (MandatePayment) => Boolean, a:Qual) = ( acc: Map[Qual,  Map[Long, MandatePayment]],e:MandatePayment) => 
    if (f(e._1, e._2)) 
      acc.updated( a, acc.getOrElse(a, Map.empty[Long, MandatePayment]).updated( e._1.MANDATE_ID, e))
    else acc
    
  val sepL: List[ (Map[Qual,  Map[Long, MandatePayment]],MandatePayment) => Map[Qual,  Map[Long, MandatePayment]]] = 
    catfn(                                   mandateHasNoPayment,    NtNoPayment)::
    catfn( e =>                            ! mandateHasNoPayment(e), NtPayment)::
    catfn(      istMandateAbgelaufen,                                NtOod)::
    catfn( e => istMandateAbgelaufen(e) && ! mandateHasNoPayment(e), NtOodPayment)::
    catfn( e => istMandateAbgelaufen(e) &&   mandateHasNoPayment(e), NtOodNoPayment)::
    Nil

  def seperate(m: Map[Long, MandatePayment]) : Map[Qual,  Map[Long, MandatePayment]] = {
    m.foldLeft(Map.empty[Qual,  Map[Long, MandatePayment]])( (acc,e) => 
      sepL.foldLeft( acc)( (acc2,f) => f(acc2, e._2)))
  }
  
//  def foldDisjunkt(  m:Map[Long, MandatePayment]) :  Map[Int,  Map[Long, MandatePayment]] = ???

  def getEntryWithPayment( m:Map[Long, MandatePayment]) =  
    m.filter( e => ! mandateHasNoPayment( e._2)) 
    
  def getEntryOhnePayment( m:Map[Long, MandatePayment]) =  
    m.filter( e =>   mandateHasNoPayment( e._2)) 
    
  def getEntryAbgelaufenWithPayment( m:Map[Long, MandatePayment]) =  
    m.filter( e => ! mandateHasNoPayment( e._2) && istMandateAbgelaufen( e._2)) 
    
  def getEntryAbgelaufenOhnePayment( m:Map[Long, MandatePayment]) =  
    m.filter( e =>   mandateHasNoPayment( e._2) && istMandateAbgelaufen( e._2)) 
    
  def getEntryNotTerminatedAbgelaufen( m:Map[Long, MandatePayment]) =  
    m.filter( e => istMandateAbgelaufen( e._2)) 

	/**
	 * Filter Mandate ohne Payments
	 */
	def mandateHasNoPayment( mp : MandatePayment) : Boolean = mp._2.isEmpty
    
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
	def istMandateAbgelaufen( e:MandatePayment) : Boolean = {
	  val d = TimeService.getCurrentTimeYearsBefore( 3)
		val v = getLastValidationDate(e._1,e._2).getOrElse( (new GregorianCalendar( 1900, 1, 1)).getTime())
			
		v.compareTo( d) < 0			 			  			  
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
	def aggregateMandateWithEmptyPayment( ml:List[Mandate]) : Map[Long, MandatePayment] = 
	  ml.foldRight( Map.empty[Long,( Mandate, Option[Payment])])((m,z) => z.updated(m.MANDATE_ID, (m,None)))
	  
	/**
	 * Paments in die Mappe fuellen
	 */
	def aggregateMandateWithPayment( mm : Map[Long, MandatePayment], pl : List[Payment])  : Map[Long, MandatePayment] = 
	    pl.foldRight(mm)( (p,m) =>  m.get(p.MANDATE_ID)  match { 
			  case Some(k)   => m.updated(p.MANDATE_ID, (k._1, Some(p)))
			  case _         => m
			})
				
  /**
   * Aggregiert aus der Liste der Mandate und der Liste der Payments eine Mappe zur Mandats-ID
   */
  def buildMapMandateWithLatestPayment( lm:List[Mandate], lp:List[Payment]) : Map[Long, MandatePayment] = 
	 MandateService.aggregateMandateWithPayment(
	     MandateService.aggregateMandateWithEmptyPayment(lm),lp)	  
	/**
	 * Mandate mit ihrem letzten Payment anreichern falls vorhanden 
	 */
	def getNichtTerminierteMandateUndLetztesPayment()  : ConnectionIO[Map[Long, MandatePayment]] = {

	  for {
	    lm <- Mandate.selectAktAllNotTerminated()
	    mm <- aggregateMandateWithEmptyPayment(lm).pure[ConnectionIO]
	    lp <- Payment.selectLastPaymentAlle()
	    mp <-  aggregateMandateWithPayment( mm, lp).pure[ConnectionIO]
	  } yield mp
	}
	    
    
	/**
	 * Mandate mit ihrem letzten Payment anreichern falls vorhanden 
	 */

	def getMandateWithPaymentsSlow() = Mandate.selectAktAll().flatMap( 
				_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x)))))
}