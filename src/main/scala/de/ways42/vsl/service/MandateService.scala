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


object MandateService {

	def istMandateAbgelaufen( d : Date, m : Mandate, p : Option[Payment]) : Boolean = {
			p.flatMap( x => x.SCHEDULED_DUE_DATE).getOrElse( m.SIGNED_DATE.getOrElse( new Date(1900, 1, 1)))
			.compareTo( d) >= 0			 			  			  
	}
	
	/**
	 * Aktuelles Mandat mit allen zugehoerigen Payments laden
	 */
	def getMandateWithPayments( mandateId : Long, xa : Transactor.Aux[IO, Unit]) : ( Mandate, List[Payment]) = {
	  Mandate.selectAktById( mandateId).flatMap( 
	      m => Payment.selectAllByMandateId(mandateId).map( 
	          l => (m,l))).transact(xa).unsafeRunSync
	}
	
	def getNichtTerminierteAbgelaufeneMandate( xa : Transactor.Aux[IO, Unit]) = {

			val lm = Mandate.selectAktAllNotTerminated().transact(xa).unsafeRunSync
			val lp = Payment.selectLastPaymentAlle().transact(xa).unsafeRunSync
		
			val mm =  lm.foldRight( Map.empty[Long,( Mandate, Option[Payment])])((m,z) => z.updated(m.MANDATE_ID, (m,None)))
			val mmp = lp.foldRight( mm)((p,m) =>  m.get(p.MANDATE_ID)  match { 
			  case Some(k)   => m.updated(p.MANDATE_ID, (k._1, Some(p)))
			  case _         => m
			})
					
			println ( "Anzahl ohne Payment: " + mmp.filter( _._2._2.isEmpty).size) 
			val r = mmp.filter( _._2._2.isEmpty)
			println ( "Anzahl abgelaufene mit aktiven Status: " + mmp.filter( _._2._2.isEmpty).size) 
			
			r
	}

	//			Mandate.selectAktAll().to[List].flatMap( 
//			  _.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).to[List].map(x => (m, x match {
//			    case Nil => None
//			    case _   => Some( x.max(Payment.orderByScheduledDueDate))}
//			  )))).
//			  transact(xa).unsafeRunSync.foreach(println)


}