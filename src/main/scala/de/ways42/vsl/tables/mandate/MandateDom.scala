package de.ways42.vsl.tables.mandate

import java.sql.Date
import java.util.GregorianCalendar

import de.ways42.vsl.service.TimeService

object MandateDom {
  
  sealed trait Qual  {
    def catfn( f: (MandateDom) => Boolean, a:Qual) = ( acc: Map[Qual,  Map[Long, MandateDom]],e:MandateDom) => 
      if (f(e._1, e._2)) 
        acc.updated( a, acc.getOrElse(a, Map.empty[Long, MandateDom]).updated( e._1.MANDATE_ID, e))
      else acc
  }
  
  object Qual extends Qual {  
    final case object NtNoPayment    extends Qual
    final case object NtPayment      extends Qual
    final case object NtOod          extends Qual
    final case object NtOodPayment   extends Qual
    final case object NtOodNoPayment extends Qual
  
    val sepL: List[ (Map[Qual,  Map[Long, MandateDom]],MandateDom) => Map[Qual,  Map[Long, MandateDom]]] = 
      catfn(                                   mandateHasNoPayment,    NtNoPayment)::
      catfn( e =>                            ! mandateHasNoPayment(e), NtPayment)::
      catfn(      istMandateAbgelaufen,                                NtOod)::
      catfn( e => istMandateAbgelaufen(e) && ! mandateHasNoPayment(e), NtOodPayment)::
      catfn( e => istMandateAbgelaufen(e) &&   mandateHasNoPayment(e), NtOodNoPayment)::
      Nil
    def seperate(m: Map[Long, MandateDom]) : Map[Qual,  Map[Long, MandateDom]] = {
      m.foldLeft(Map.empty[Qual,  Map[Long, MandateDom]])( (acc,e) => 
        Qual.sepL.foldLeft( acc)( (acc2,f) => f(acc2, e._2)))
    }
  }
  
	/**
	 * Mappe mit leeren Payment aufbauen
	 */
	def aggregateMandateWithEmptyPayment( ml:List[Mandate]) : Map[Long, MandateDom] = 
	  ml.foldRight( Map.empty[Long,( Mandate, Option[Payment])])((m,z) => z.updated(m.MANDATE_ID, (m,None)))
	  
	/**
	 * Paments in die Mappe fuellen
	 */
	def aggregateMandateWithPayment( mm : Map[Long, MandateDom], pl : List[Payment])  : Map[Long, MandateDom] = 
	    pl.foldRight(mm)( (p,m) =>  m.get(p.MANDATE_ID)  match { 
			  case Some(k)   => m.updated(p.MANDATE_ID, (k._1, Some(p)))
			  case _         => m
			})
				
  /**
   * Aggregiert aus der Liste der Mandate und der Liste der Payments eine Mappe zur Mandats-ID
   */
  def buildMapMandateWithLatestPayment( lm:List[Mandate], lp:List[Payment]) : Map[Long, MandateDom] = 
	  aggregateMandateWithPayment( aggregateMandateWithEmptyPayment(lm),lp)	  

  
  import scala.language.implicitConversions
  implicit def mandateDom2MandateDomOps( md:MandateDom) = new MandateDomOps(md)
  
  	/**
	 * Filter Mandate ohne Payments
	 */
	def mandateHasNoPayment( md : MandateDom) : Boolean = md._2.isEmpty
    
  /**
   * Datum der letzen Erneuerung der Gueltigkeit
   */
	def getLastValidationDate( md : MandateDom) : Option[Date] = {
	  md._2.flatMap( _.SCHEDULED_DUE_DATE match {
	    case None => md._1.SIGNED_DATE
	    case y    => y})
	}
	  
  /**
   * Mandate mit letztem Payment aelter als 3 Jahre ?
   */
	def istMandateAbgelaufen( e:MandateDom) : Boolean = {
	  val d = TimeService.getCurrentTimeYearsBefore( 3)
		val v = getLastValidationDate(e._1,e._2).getOrElse( (new GregorianCalendar( 1900, 1, 1)).getTime())
			
		v.compareTo( d) < 0			 			  			  
	}

  
  class MandateDomOps( md:MandateDom) {
    def mandateHasNoPayment : Boolean = MandateDom.mandateHasNoPayment(md)
    def getLastValidationDate : Option[Date] = MandateDom.getLastValidationDate( md)
    def abgelaufen : Boolean            = MandateDom.istMandateAbgelaufen(md)
    def abgelaufenMitPayment : Boolean  = MandateDom.istMandateAbgelaufen(md) && ! MandateDom.mandateHasNoPayment(md)
    def abgelaufenOhnePayment : Boolean = MandateDom.istMandateAbgelaufen(md) &&   MandateDom.mandateHasNoPayment(md)
  }
  
}