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
import monix.eval.Task
import monix.execution.Scheduler


object MandateServiceTask {
  
  def apply( xa : Transactor.Aux[Task, Unit]) : MandateServiceTask = new MandateServiceTask( xa)

}
  
class MandateServiceTask( val xa : Transactor.Aux[Task, Unit]) {

//	def istMandateAbgelaufen( d : Date, m : Mandate, p : Option[Payment]) : Boolean = {
//			p.flatMap( x => x.SCHEDULED_DUE_DATE).getOrElse( m.SIGNED_DATE.getOrElse( (new GregorianCalendar( 1900, 1, 1)).getTime()))
//			.compareTo( d) >= 0			 			  			  
//	}
//	
//	/**
//	 * Aktuelles Mandat mit allen zugehoerigen Payments laden, falls vorhanden
//	 */
//	def getMandateWithPayments( mandateId : Long) : ConnectionIO[(Option[Mandate], List[Payment])] = Mandate.selectAktById( mandateId).flatMap({
//	    case Some(m) => Payment.selectAllByMandateId(mandateId).map( l => (Some(m),l))
//	    case None    => ( Option.empty[Mandate], List.empty[Payment]).pure[ConnectionIO]})
//	
//	/**
//	 * Mappe mit leeren Payment aufbauen
//	 */
//	def mapIdMandatePayment( lm:List[Mandate]) :  Map[Long, (Mandate, Option[Payment])] = 
//	  lm.foldRight( Map.empty[Long,( Mandate, Option[Payment])])((m,z) => z.updated(m.MANDATE_ID, (m,None)))
//	
//	/**
//	 * Paments in die Mappe fuellen
//	 */
//	def fillPayment( mm:Map[Long, (Mandate, Option[Payment])], lp: List[Payment]) = lp.foldRight( mm)((p,m) =>  m.get(p.MANDATE_ID)  match { 
//	  case Some(k)   => m.updated(p.MANDATE_ID, (k._1, Some(p)))
//	  case _         => m
//	})
	
	def getNichtTerminierteAbgelaufeneMandateMitLetztemPayment()( implicit ev : Scheduler) : Task[Map[Long, (Mandate, Option[Payment])]] = {
	  
	  val m : Task[List[Mandate]] =  Mandate.selectAktAllNotTerminated().transact(xa)
	  val p : Task[List[Payment]] =  Payment.selectLastPaymentAlle().transact(xa)
	  
	  for {
	    ll <- Task.parZip2(m,p)
	    mm <- Task( MandateService.mapIdMandatePayment(ll._1))
	    mp <- Task( MandateService.fillPayment( mm, ll._2).filter( _._2._2.isEmpty))
	  } yield  mp	  
	}

	def getMandateWithPaymentsSlow() : Task[Unit] = {
			Mandate.selectAktAll().flatMap( 
					_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x))))
					).transact(xa).map(println)
	}
}