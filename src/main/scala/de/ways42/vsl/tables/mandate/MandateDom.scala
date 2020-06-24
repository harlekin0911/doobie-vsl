package de.ways42.vsl.tables.mandate

import java.sql.Date
import java.util.GregorianCalendar

import de.ways42.vsl.service.TimeService
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid


object MandateDom {
  
  def apply( m:Mandate, lp:List[Payment]) : MandateDom = (m, lp)
  
  def buildValidated( m:Mandate, lp:List[Payment]) : Validated[String,MandateDom] = 
    validate( m, lp)

  def validate( m:Mandate, lp:List[Payment]) : Validated[String, (Mandate,List[Payment])] = lp.filter( _.MANDATE_ID != m.MANDATE_ID) match {
    case Nil => Valid((m,lp))
    case fp   => 
      val ep = fp.fold( "Fehlerhafte Payments mit abweichender MandatsID: ")((f,p) => f + " " + p.toString())
      Invalid( "Objekte mit falscher MandatsId, " + ep)
  }

}

object MandateAktDom {
  
  sealed trait Qual  {
    def catfn( f: (MandateAktDom) => Boolean, a:Qual) = ( acc: Map[Qual,  Map[Long, MandateAktDom]],e:MandateAktDom) => 
      if (f(e._1, e._2)) 
        acc.updated( a, acc.getOrElse(a, Map.empty[Long, MandateAktDom]).updated( e._1.MANDATE_ID, e))
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
  
  
	/**
	 * Mappe mit leeren Payment aufbauen
	 */
	def aggregateMandateWithEmptyPayment( ml:List[Mandate]) : Map[Long, MandateAktDom] = 
	  ml.foldRight( Map.empty[Long,( Mandate, Option[Payment])])((m,z) => z.updated(m.MANDATE_ID, (m,None)))
	  
	/**
	 * Paments in die Mappe fuellen
	 */
	def aggregateMandateWithPayment( mm : Map[Long, MandateAktDom], pl : List[Payment])  : Map[Long, MandateAktDom] = 
	    pl.foldRight(mm)( (p,m) =>  m.get(p.MANDATE_ID)  match { 
			  case Some(k)   => m.updated(p.MANDATE_ID, (k._1, Some(p)))
			  case _         => m
			})
				
  /**
   * Aggregiert aus der Liste der Mandate und der Liste der Payments eine Mappe zur Mandats-ID
   */
  def buildMapMandateWithLatestPayment( lm:List[Mandate], lp:List[Payment]) : Map[Long, MandateAktDom] = 
	  aggregateMandateWithPayment( aggregateMandateWithEmptyPayment(lm),lp)	  

  
  import scala.language.implicitConversions
  implicit def MandateAktDom2MandateAktDomOps( md:MandateAktDom) = new MandateAktDomOps(md)
  
  	/**
	 * Filter Mandate ohne Payments
	 */
	def mandateHasNoPayment( md : MandateAktDom) : Boolean = md._2.isEmpty
    
  /**
   * Datum der letzen Erneuerung der Gueltigkeit
   */
	def getLastValidationDate( md : MandateAktDom) : Option[Date] = {
	  md._2.flatMap( _.SCHEDULED_DUE_DATE match {
	    case None => md._1.SIGNED_DATE
	    case y    => y})
	}
	  
  /**
   * Mandate mit letztem Payment aelter als 3 Jahre ?
   */
	def istMandateAbgelaufen( e:MandateAktDom) : Boolean = {
	  val d = TimeService.getCurrentTimeYearsBefore( 3)
		val v = getLastValidationDate(e._1,e._2).getOrElse( (new GregorianCalendar( 1900, 1, 1)).getTime())
			
		v.compareTo( d) < 0			 			  			  
	}

  
  class MandateAktDomOps( md:MandateAktDom) {
    def mandateHasNoPayment : Boolean = MandateAktDom.mandateHasNoPayment(md)
    def getLastValidationDate : Option[Date] = MandateAktDom.getLastValidationDate( md)
    def abgelaufen : Boolean            = MandateAktDom.istMandateAbgelaufen(md)
    def abgelaufenMitPayment : Boolean  = MandateAktDom.istMandateAbgelaufen(md) && ! MandateAktDom.mandateHasNoPayment(md)
    def abgelaufenOhnePayment : Boolean = MandateAktDom.istMandateAbgelaufen(md) &&   MandateAktDom.mandateHasNoPayment(md)
  }
  
}