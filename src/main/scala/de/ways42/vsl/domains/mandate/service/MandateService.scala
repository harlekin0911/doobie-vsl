package de.ways42.vsl.domains.mandate.service


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
import de.ways42.vsl.domains.mandate.tables.BusinessObjectRef
import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.Mandate
import de.ways42.vsl.domains.mandate.domain.MandateAktDom
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
	    lm <- Mandate.selectAktAllAktive()
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
				
				
  /**
   * Ein Mandat mit den zugehoerigen letzem Payment parallel laden
   */
  def getMandateWithLastPaymentTables( mandateId:Long) : (ConnectionIO[Option[Mandate]],ConnectionIO[ List[Payment]]) = (
	    Mandate.selectAktById( mandateId),
	    Payment.selectLastByMandateId(mandateId))
  /**
   * Ein Mandat mit den zugehoerigen Payments parallel laden
   */
  def getMandateWithPaymentsTables( mandateId:Long) : (ConnectionIO[Option[Mandate]],ConnectionIO[ List[Payment]]) = (
	    Mandate.selectAktById( mandateId),
	    Payment.selectAllByMandateId(mandateId))
  	
	/**
	 * Alle Mandate mit ihrem letzten Payment falls vorhanden parallel laden 
	 */
  def getAllMandatesWithPayments() : (ConnectionIO[List[Mandate]], ConnectionIO[List[Payment]]) = (
	    Mandate.selectAktAllAktive(),
	    Payment.selectLastPaymentAlle())

	/**
   * Get all MandateExt actual
   */
  def getAktAllMandateDomainTables()  : 
    (ConnectionIO[List[BusinessObjectRef]], ConnectionIO[List[Mandate]], ConnectionIO[List[Payment]]) = (
      BusinessObjectRef.selectAktAll(),
      Mandate.selectAktAll(),
      Payment.selectLastPaymentAlle())
  
	/**
   * Get all MandateExt actual and Aktive
   */
  def getAktAllAktiveMandateDomainTables()  : 
    (ConnectionIO[List[BusinessObjectRef]], ConnectionIO[List[Mandate]], ConnectionIO[List[Payment]]) = (
      BusinessObjectRef.selectAktAllAktive(),
      Mandate.selectAktAllAktive(),
      Payment.selectLastPaymentAlle())
      
  /**
   * Einzelnens aktuelles Mandate laden
   */
  def getSingleMandateDomainTables( vtgnr:String) = {
      val b = BusinessObjectRef.selectAktByBusinessObjExtRef(vtgnr)
      val mp = b.map( x =>
        x.map( b => (Mandate.selectAktById( b.MANDATE_ID), Payment.selectLastPaymentByMandate(b.MANDATE_ID))))
        
       ( b, mp)
	}
    
				
}