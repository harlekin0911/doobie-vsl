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
   * Mandate mit den zugehoerigen Payments parallel laden
   */
  def getMandateWithPayments( mandateId : Long) : Task[(Option[Mandate], List[Payment])] = Task.parZip2(
	    Mandate.selectAktById( mandateId).transact(xa),
	    Payment.selectAllByMandateId(mandateId).transact(xa))
  	
	def getNichtTerminierteMandateMitLetztemPayment()( implicit ev : Scheduler) : Task[Map[Long, (Mandate, Option[Payment])]] = {
	  
	  val m : Task[List[Mandate]] =  Mandate.selectAktAllNotTerminated().transact(xa)
	  val p : Task[List[Payment]] =  Payment.selectLastPaymentAlle().transact(xa)
	  
	  for {
	    ll <- Task.parZip2(m,p)
	    mm <- Task( MandateService.aggregateMandateWithEmptyPayment(ll._1))
	    mp <- Task( MandateService.aggregateMandateWithPayment( mm, ll._2).filter( _._2._2.isEmpty))
	  } yield  mp	  
	}

	def getMandateWithPaymentsSlow() : Task[Unit] = {
			Mandate.selectAktAll().flatMap( 
					_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x))))
					).transact(xa).map(println)
	}
}