package de.ways42.vsl.tables.mandate

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.implicits._
import cats.data.Validated._
import cats.data._

object MandateExtDom {
  
  def apply( ob:Option[BusinessObjectRef], m:Mandate, lp:List[Payment]) : Option[MandateExtDom] = 
    ob.flatMap( x => Some(apply( x, m, lp)))
    
  def apply( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : MandateExtDom = (b, (m, lp))
  
  
  def applyV( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : MandateExtDom = 
    validate( b, m, lp) match {
    case Valid(o) => o
    case Invalid(c) => throw new RuntimeException( c.foldLeft( "")( (acc,s) => acc + " " + s))
  }
  
  
  def validate( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : ValidatedNec[String, MandateExtDom] = {
    
    def valM( b:BusinessObjectRef, m:Mandate) : Validated[String,(BusinessObjectRef,Mandate)] = 
      if ( b.MANDATE_ID != m.MANDATE_ID)  Invalid("Fehlerhaftes BusinessObject mit abweichender Mandats_id:" + b.toString())
      else Valid((b,m))
    
    def build(  d1:(BusinessObjectRef, Mandate),d2:(Mandate, List[Payment])) = apply(d1._1, d1._2, d2._2) 

    ( valM( b,m).toValidatedNec, MandateDom.validate( m, lp).toValidatedNec).mapN( build(_,_))
  }
    
    
  import scala.language.implicitConversions

  implicit def MandateExtDom2MandateExtDomOps( m:MandateExtDom) = new MandateExtDomOps(m)
  
  class MandateExtDomOps ( val m:MandateExtDom) {
  }
}