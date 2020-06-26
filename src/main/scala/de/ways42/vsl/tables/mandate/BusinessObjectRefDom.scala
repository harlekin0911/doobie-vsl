package de.ways42.vsl.tables.mandate

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.implicits._
import cats.data.Validated._
import cats.data._

case class BusinessObjectRefDom( b:BusinessObjectRef, md:Option[MandateDom])

object BusinessObjectRefDom {
  
  def apply( ob:Option[BusinessObjectRef], m:Mandate, lp:List[Payment]) : Option[BusinessObjectRefDom] = 
    ob.flatMap( x => Some(apply( x, m, lp)))
    
  def apply( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : BusinessObjectRefDom = BusinessObjectRefDom(b, Some(MandateDom(m, lp)))

  def apply( b:BusinessObjectRef, md: MandateDom) : BusinessObjectRefDom = BusinessObjectRefDom(b, Some(md))
  
  
  def applyV( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : BusinessObjectRefDom = 
    validate( b, m, lp) match {
    case Valid(o) => o
    case Invalid(c) => throw new RuntimeException( c.foldLeft( "")( (acc,s) => acc + " " + s))
  }
  
  
  def validate( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : ValidatedNec[String, BusinessObjectRefDom] = {
    
    def valM( b:BusinessObjectRef, m:Mandate) : Validated[String,(BusinessObjectRef,Mandate)] = 
      if ( b.MANDATE_ID != m.MANDATE_ID)  Invalid("Fehlerhaftes BusinessObject mit abweichender Mandats_id:" + b.toString())
      else Valid((b,m))
    
    def build(  d1:(BusinessObjectRef, Mandate),md:MandateDom) = apply(d1._1, d1._2, md.lp) 

    ( valM( b,m).toValidatedNec, MandateDom.validate( m, lp).toValidatedNec).mapN( build(_,_))
  }
  
  /**
   * Mappe mit businessObjectRefId und dem BusinessObjectRefDom aufbauen,
   * zwischen BusinessObjectReference und Mandate besteht eine 1 <--> 1 Beziehung
   * 
   * Referenzen zu nicht vorhandenen Mandaten werden ignoriert
   */
  def aggregateListBusinessObjectRefDom( lbor:List[BusinessObjectRef], mmd:Map[Long,MandateDom]) : Map[Long,BusinessObjectRefDom] = 
    lbor.foldLeft( Map.empty[Long,BusinessObjectRefDom])( 
        (acc,bor) => mmd.get(bor.MANDATE_ID) match {
        case Some(d) => acc.updated(bor.BUSINESS_OBJ_REFERENCE_ID, apply(bor,d))
        case None    => acc})
    
    
  import scala.language.implicitConversions

  implicit def BusinessObjectRefDom2BusinessObjectRefDomOps( m:BusinessObjectRefDom) = new BusinessObjectRefDomOps(m)
  
  class BusinessObjectRefDomOps ( val m:BusinessObjectRefDom) {
  }
}