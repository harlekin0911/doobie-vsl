package de.ways42.vsl.domains.mandate.transaction

import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.Mandate
import de.ways42.vsl.domains.mandate.tables.BusinessObjectRef
import de.ways42.vsl.domains.mandate.domain.MandateAktDom
import de.ways42.vsl.domains.mandate.domain.MandateAktDom._
import java.sql.Date
import doobie.util.transactor.Transactor

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import java.util.GregorianCalendar
import monix.eval.Task
import monix.execution.Scheduler
import de.ways42.vsl.domains.mandate.service.PaymentService
import de.ways42.vsl.domains.mandate.service.MandateService
import de.ways42.vsl.domains.mandate.domain.MandateDom
import de.ways42.vsl.domains.mandate.domain.BusinessObjectRefDom
import de.ways42.vsl.domains.mandate.domain.MandateDomain


object MandateTask {
  
  def apply[A]( xa : Transactor.Aux[Task, A]) : MandateTask[A] = new MandateTask( xa)

}
  
class MandateTask[A]( val xa : Transactor.Aux[Task, A]) {
  

  def getAktSingleMandateDomain( vtgnr:String, f:Long => (ConnectionIO[Option[Mandate]],ConnectionIO[List[Payment]])) = {
    val bor = BusinessObjectRef.selectAktByBusinessObjExtRef(vtgnr).transact(xa)
    val c = bor.flatMap( x => x.foldLeft(Task(MandateDomain(vtgnr)))( (tmd,b) => {
      val (a,c) = f(b.MANDATE_ID)
      val d = Task.parZip2(a.transact(xa),c.transact(xa))
      val e = d.map( x => BusinessObjectRefDom(b,x._1.get,x._2))
      e.flatMap( x => tmd.map(_.add(x)))
      }))
      c
    }
  def getAktSingleMandateDomainPar( vtgnr:String, f:Long => (ConnectionIO[Option[Mandate]],ConnectionIO[List[Payment]])) = {
    val bor = BusinessObjectRef.selectAktByBusinessObjExtRef(vtgnr).transact(xa)
    val c = bor.flatMap( lb => Task.sequence( lb.map( b => {
      val (a,c) = f(b.MANDATE_ID)
      Task.parZip2(a.transact(xa),c.transact(xa)).map( x => BusinessObjectRefDom(b,x._1.get,x._2))

      })))
      c.map( _.foldLeft(MandateDomain(vtgnr))( (tmd,b) => tmd.add(b)))
    }
/**
   * aktuelles MandateDomain zu einem Vertrag laden
   */
  
  def getAktSingleMandateDomainLastPayment( vtgnr:String) = 
    getAktSingleMandateDomain( vtgnr, MandateService.getMandateWithLastPaymentTables)
      
  def getAktSingleMandateDomainAllPayments( vtgnr:String) = 
    getAktSingleMandateDomain( vtgnr, MandateService.getMandateWithPaymentsTables)
  

  /**
   * Alle MandateDomain, top Down aufbauen
   */
  def getAktAllMandateDomain()  : Task[Map[String,MandateDomain]] = {
    val (a,b,c) = MandateService.getAktAllMandateDomainTables()
    Task.parZip3( a.transact(xa), b.transact(xa), c.transact(xa)).map( x => 
    MandateDomain.apply( x._1, x._2, x._3))
  }
                
  /**
   * Alle aktiven MandateDomain, top Down aufbauen
   */
  def getAktAllAktiveMandateDomain()  : Task[Map[String,MandateDomain]] = {
    val (a,b,c) = MandateService.getAktAllAktiveMandateDomainTables()
    Task.parZip3( a.transact(xa), b.transact(xa), c.transact(xa)).map( x =>   
      MandateDomain.apply( x._1, x._2, x._3))
  }
  /**
   * Alle MandateDomain, Bottom Up aufbauen
   */
  def getAllMandateDomainAktBottomUp()  : Task[Map[String,MandateDomain]] = { 
    val (a,b,c) = MandateService.getAktAllMandateDomainTables
    Task.parZip3( a.transact(xa), b.transact(xa), c.transact(xa)).map( x =>
        MandateDomain(
            BusinessObjectRefDom.aggregateListBusinessObjectRefDom( 
                x._1, MandateDom.aggregateMandateWithPayment( x._2, MandateDom.aggregatePayments(x._3)))))
  }
  

	/**
	 * Alle nicht terminierten Mandate mit ihrem letzten Payment parallel laden und eine Mappe bilden 
	 * Kombiniert das Laden der Mandate und Payments mit der Assosation in einer Mappe
	 */
	def getMapMandateWithLatestPayment() : Task[Map[Long, MandateAktDom]] = {	 
	  val (a,b) = MandateService.getAllMandatesWithPayments()
	  Task.parZip2(a.transact(xa),b.transact(xa)).map( ll => 
	    MandateAktDom.buildMapMandateWithLatestPayment( ll._1, ll._2))
	}
 
  /**
   * Sequntiell die Datenbank gefragen
   */
	def getMandateWithPaymentsSlow() : Task[Unit] = {
			Mandate.selectAktAll().flatMap( 
					_.traverse( m => Payment.selectAllByMandateId( m.MANDATE_ID).map( x => (m, PaymentService.getLatestPayment(x))))
					).transact(xa).map(println)
	}
}