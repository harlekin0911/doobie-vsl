package de.ways42.vsl.domains.mandate.domain

import java.sql.Date
import java.util.GregorianCalendar

import de.ways42.vsl.service.TimeService
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.Mandate


case class MandateDom( m:Mandate, lp:List[Payment]) {
    /**
   * Mandate mit letztem Payment aelter als 3 Jahre ?
   */
	def istAbgelaufen() : Boolean = {
	  val d = TimeService.getCurrentTimeYearsBefore( 3)
		val v = getLastValidationDate().getOrElse( (new GregorianCalendar( 1900, 1, 1)).getTime())
			
		v.compareTo( d) < 0			 			  			  
	}
	
	  /**
   * Datum der letzen Erneuerung der Gueltigkeit
   */
	def getLastValidationDate() : Option[Date] = lp match { 
	  case Nil => m.SIGNED_DATE
	  case _   => lp.max.SCHEDULED_DUE_DATE
	  }
	
	def isTerminated : Boolean = m.TERMINATED_FLAG > 0
	
	def mandateExtRef : Option[String] = m.MANDATE_EXT_REF
	def mandateId     : Long           = m.MANDATE_ID
}

object MandateDom {
  
  def apply( m:Mandate, lp:List[Payment]) : MandateDom = new MandateDom(m, lp)
  
  def buildValidated( m:Mandate, lp:List[Payment]) : Validated[String,MandateDom] = 
    validate( m, lp)

  def validate( m:Mandate, lp:List[Payment]) : Validated[String, MandateDom] = lp.filter( _.MANDATE_ID != m.MANDATE_ID) match {
    case Nil => Valid(MandateDom(m,lp))
    case fp   => 
      val ep = fp.fold( "Fehlerhafte Payments mit abweichender MandatsID: ")((f,p) => f + " " + p.toString())
      Invalid( "Objekte mit falscher MandatsId, " + ep)
  }
  
	/**
	 * Mappe zur MandatsId und dem MandateDom aufbauen, also das Mandat mit all seinen Payments
	 */
	def aggregateMandateWithPayment( lm:List[Mandate], mp:Map[Long, List[Payment]]) : Map[Long, MandateDom] = 
	  lm.foldRight( 
	      Map.empty[Long,MandateDom])(
	          (m,z) => z.updated(m.MANDATE_ID, MandateDom(m, List.empty)))
	  
	/**
	 * Paments in die Mappe fuellen
	 */
	def aggregatePayments( pl : List[Payment])  : Map[Long, List[Payment]] = pl.groupBy( _.MANDATE_ID)
}

case class MandateAktDom( m:Mandate, op:Option[Payment]) {
  
  def mandateHasNoPayment() : Boolean = op.isEmpty
    
	def isTerminated : Boolean = m.TERMINATED_FLAG > 0

	/**
   * Datum der letzen Erneuerung der Gueltigkeit
   */
	def getLastValidationDate() : Option[Date] = op.flatMap( _.SCHEDULED_DUE_DATE match {
	    case None => m.SIGNED_DATE
	    case y    => y})
	
	  
  /**
   * Mandate mit letztem Payment aelter als 3 Jahre ?
   */
	def abgelaufen() : Boolean = {
	  val d = TimeService.getCurrentTimeYearsBefore( 3)
		val v = getLastValidationDate().getOrElse( (new GregorianCalendar( 1900, 1, 1)).getTime())
			
		v.compareTo( d) < 0			 			  			  
	}
  
  def abgelaufenMitPayment : Boolean  = abgelaufen && ! mandateHasNoPayment
  def abgelaufenOhnePayment : Boolean = abgelaufen &&   mandateHasNoPayment

}

object MandateAktDom {
  
  sealed trait Qual  {
    def catfn( f: (MandateAktDom) => Boolean, a:Qual) = ( acc: Map[Qual,  Map[Long, MandateAktDom]],e:MandateAktDom) => 
      if (f(e)) 
        acc.updated( a, acc.getOrElse(a, Map.empty[Long, MandateAktDom]).updated( e.m.MANDATE_ID, e))
      else acc
  }
  
  object Qual extends Qual {  
    final case object NtNoPayment    extends Qual
    final case object NtPayment      extends Qual
    final case object NtOod          extends Qual
    final case object NtOodPayment   extends Qual
    final case object NtOodNoPayment extends Qual
  
    val sepL: List[ (Map[Qual,  Map[Long, MandateAktDom]],MandateAktDom) => Map[Qual,  Map[Long, MandateAktDom]]] = 
      catfn(                                   mandateHasNoPayment,    NtNoPayment)::
      catfn( e =>                            ! mandateHasNoPayment(e), NtPayment)::
      catfn(      istMandateAbgelaufen,                                NtOod)::
      catfn( e => istMandateAbgelaufen(e) && ! mandateHasNoPayment(e), NtOodPayment)::
      catfn( e => istMandateAbgelaufen(e) &&   mandateHasNoPayment(e), NtOodNoPayment)::
      Nil
    def seperate(m: Map[Long, MandateAktDom]) : Map[Qual,  Map[Long, MandateAktDom]] = {
      m.foldLeft(Map.empty[Qual,  Map[Long, MandateAktDom]])( (acc,e) => 
        Qual.sepL.foldLeft( acc)( (acc2,f) => f(acc2, e._2)))
    }
  }
  
	def getNichtTerminierteMandateMitPayment( t:Map[Long, MandateAktDom]) : Map[Long, MandateAktDom] =  t.filter( ! _._2.mandateHasNoPayment)
	/**
	 * Nicht terminierte  Mandate ohne Payment
	 */
	def getNichtTerminierteMandateOhnePayment( t:Map[Long, MandateAktDom])  : Map[Long, MandateAktDom] = t.filter( _._2.mandateHasNoPayment) 
	  
	def getNichtTerminierteAbgelaufeneMandateOhnePayment( t:Map[Long, MandateAktDom])  : Map[Long, MandateAktDom] = t.filter(_._2.abgelaufenOhnePayment)
	
	def getNichtTerminierteAbgelaufeneMandateWithPayment( t:Map[Long, MandateAktDom])  : Map[Long, MandateAktDom] = t.filter( _._2.abgelaufenMitPayment)  
	/**
	 * Nicht terminierte Abgelaufene Mandate
	 */
	def getNichtTerminierteAbgelaufeneMandate( t:Map[Long, MandateAktDom])  : Map[Long, MandateAktDom] = t.filter( _._2.abgelaufen)
  
	/**
	 * Mappe mit leeren Payment aufbauen
	 */
	def aggregateMandateWithEmptyPayment( ml:List[Mandate]) : Map[Long, MandateAktDom] = 
	  ml.foldRight( Map.empty[Long,MandateAktDom])((m,z) => z.updated(m.MANDATE_ID, MandateAktDom(m,None)))
	  
	/**
	 * Paments in die Mappe fuellen
	 */
	def aggregateMandateWithPayment( mm : Map[Long, MandateAktDom], pl : List[Payment])  : Map[Long, MandateAktDom] = 
	    pl.foldRight(mm)( (p,m) =>  m.get(p.MANDATE_ID)  match { 
			  case Some(k)   => m.updated(p.MANDATE_ID, MandateAktDom(k.m, Some(p)))
			  case _         => m
			})
				
  /**
   * Aggregiert aus der Liste der Mandate und der Liste der Payments eine Mappe zur Mandats-ID
   */
  def buildMapMandateWithLatestPayment( lm:List[Mandate], lp:List[Payment]) : Map[Long, MandateAktDom] = 
	  aggregateMandateWithPayment( aggregateMandateWithEmptyPayment(lm),lp)	  

  
//  import scala.language.implicitConversions
//  implicit def MandateAktDom2MandateAktDomOps( md:MandateAktDom) : MandateAktDomOps = new MandateAktDomOps(md)
  
  	/**
	 * Filter Mandate ohne Payments
	 */
	def mandateHasNoPayment( md : MandateAktDom) : Boolean = md.mandateHasNoPayment()
    
  /**
   * Datum der letzen Erneuerung der Gueltigkeit
   */
	def getLastValidationDate( md : MandateAktDom) : Option[Date] = md.getLastValidationDate()
	  
  /**
   * Mandate mit letztem Payment aelter als 3 Jahre ?
   */
	def istMandateAbgelaufen( e:MandateAktDom) : Boolean = e.abgelaufen()

  
//  class MandateAktDomOps( md:MandateAktDom) {
//    def mandateHasNoPayment : Boolean = MandateAktDom.mandateHasNoPayment(md)
//    def getLastValidationDate : Option[Date] = MandateAktDom.getLastValidationDate( md)
//    def abgelaufen : Boolean            = MandateAktDom.istMandateAbgelaufen(md)
//    def abgelaufenMitPayment : Boolean  = MandateAktDom.istMandateAbgelaufen(md) && ! MandateAktDom.mandateHasNoPayment(md)
//    def abgelaufenOhnePayment : Boolean = MandateAktDom.istMandateAbgelaufen(md) &&   MandateAktDom.mandateHasNoPayment(md)
//  }
  
}