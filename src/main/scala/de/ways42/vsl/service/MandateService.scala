package de.ways42.vsl.service


import java.sql.Date
import java.util.GregorianCalendar

import cats._
import cats.effect._
import cats.implicits._

import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

//import monix.eval.Task
//import monix.execution.Scheduler

//import de.ways42.vsl.tables.mandate.MandateAktDomOps._
import de.ways42.vsl.tables.mandate.Payment
import de.ways42.vsl.tables.mandate.Mandate
import de.ways42.vsl.tables.mandate.MandateAktDom
import de.ways42.vsl.tables.mandate.BusinessObjectRef
import java.util.Calendar


object MandateService {
  
		/**
	 * Aktuelles Mandat mit allen zugehoerigen Payments laden, falls vorhanden
	 */
	def getMandateWithPayments( mandateId : Long) : ConnectionIO[(Option[Mandate], List[Payment])] = Mandate.selectAktById( mandateId).flatMap({
	    case Some(m) => Payment.selectAllByMandateId(mandateId).map( l => (Some(m),l))
	    case None    => ( Option.empty[Mandate], List.empty[Payment]).pure[ConnectionIO]})

	/**
	 * Mandate mit ihrem letzten Payment anreichern falls vorhanden 
	 */
	def getNichtTerminierteMandateUndLetztesPayment()  : ConnectionIO[Map[Long, MandateAktDom]] = {

	  for {
	    lm <- Mandate.selectAktAllNotTerminated()
	    mm <- MandateAktDom.aggregateMandateWithEmptyPayment(lm).pure[ConnectionIO]
	    lp <- Payment.selectLastPaymentAlle()
	    mp <- MandateAktDom.aggregateMandateWithPayment( mm, lp).pure[ConnectionIO]
	  } yield mp
	}
	    
    
	/**
	 * Mandate mit ihrem letzten Payment anreichern falls vorhanden 
	 */

	def getMandateWithPaymentsSlow() = Mandate.selectAktAll().flatMap( 
				_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x)))))
}