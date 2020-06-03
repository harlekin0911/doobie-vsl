package de.ways42.vsl.service

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


object MandateService {
  
  def apply( xa : Transactor.Aux[IO, Unit]) : MandateService = new MandateService( xa)

}
  
class MandateService( val xa : Transactor.Aux[IO, Unit]) {

	def istMandateAbgelaufen( d : Date, m : Mandate, p : Option[Payment]) : Boolean = {
			p.flatMap( x => x.SCHEDULED_DUE_DATE).getOrElse( m.SIGNED_DATE.getOrElse( (new GregorianCalendar( 1900, 1, 1)).getTime()))
			.compareTo( d) >= 0			 			  			  
	}
	
	/**
	 * Aktuelles Mandat mit allen zugehoerigen Payments laden
	 */
	def getMandateWithPayments( mandateId : Long) : ConnectionIO[(Option[Mandate], List[Payment])] = {
	  Mandate.selectAktById( mandateId).flatMap({
	    case Some(m) => Payment.selectAllByMandateId(mandateId).map( l => (Some(m),l))
	    case None  => ( Option.empty[Mandate], List.empty[Payment]).pure[ConnectionIO]})
	}
	
	def aggregateMandateWithEmptyPayment( ml:List[Mandate]) : Map[Long, (Mandate, Option[Payment])] = 
	  ml.foldRight( Map.empty[Long,( Mandate, Option[Payment])])((m,z) => z.updated(m.MANDATE_ID, (m,None)))
	  
	def aggregateMandateWithPayment( mm : Map[Long, (Mandate, Option[Payment])], pl : List[Payment])  : Map[Long, (Mandate, Option[Payment])] = 
	    pl.foldRight(mm)( (p,m) =>  m.get(p.MANDATE_ID)  match { 
			  case Some(k)   => m.updated(p.MANDATE_ID, (k._1, Some(p)))
			  case _         => m
			})
			
	def mandateHasNoPayment( mm : Map[Long, (Mandate, Option[Payment])]) : Map[Long, (Mandate, Option[Payment])] =
	  mm.filter( _._2._2.isEmpty)
	
	/**
	 * Mandate mit ihrem letzten Payment anreichern falls vorhanden 
	 */
	def getNichtTerminierteAbgelaufeneMandateMitLetztemPayment()  : ConnectionIO[Map[Long, (Mandate, Option[Payment])]] = {

	  for {
	    lm <- Mandate.selectAktAllNotTerminated()
	    mm <- aggregateMandateWithEmptyPayment(lm).pure[ConnectionIO]
	    lp <- Payment.selectLastPaymentAlle()
	    mp  <- mandateHasNoPayment( aggregateMandateWithPayment( mm, lp)).pure[ConnectionIO]
	  } yield mp

	  // Mandate.selectAktAllNotTerminated().flatMap( 
	  //   lm => aggregateMandateWithEmptyPayment(lm).pure[ConnectionIO].flatMap(
	  //	   mm => Payment.selectLastPaymentAlle().flatMap( 
	  //	     lp => mandateHasNoPayment( aggregateMandateWithPayment( mm, lp)).pure[ConnectionIO])))
	  }

	def getMandateWithPaymentsSlow() = {
			Mandate.selectAktAll().flatMap( 
					_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, x match {
					case Nil => None
					case _   => Some( x.max(Payment.orderByScheduledDueDate))
					})))
					)
	}
}