package de.ways42.vsl.domains.mandate.domain

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.implicits._
import cats.data.Validated._
import cats.data._
import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.BusinessObjectRef
import de.ways42.vsl.domains.mandate.tables.Mandate

case class BusinessObjectRefDom( b:BusinessObjectRef, md:Option[MandateDom]) {
  
  def BUSINESS_OBJ_EXT_REF      = b.BUSINESS_OBJ_EXT_REF
  def BUSINESS_OBJ_REFERENCE_ID = b.BUSINESS_OBJ_REFERENCE_ID
  def MANDATE_ID                = b.MANDATE_ID

  def addMandate( m:Mandate) : BusinessObjectRefDom = md match {
    case None => 
      if( m.MANDATE_ID != b.MANDATE_ID)
        throw new RuntimeException( "BusinessObjectRef.mandate_id=<" +  b.MANDATE_ID + "> und Mandate.mandate_id=<" + m.MANDATE_ID + "> stimmen nicht ueberein")
      BusinessObjectRefDom( b, Some(MandateDom(m,Nil)))
      
    case Some(m) => 
      throw new RuntimeException( "BusinessObjectRefDom hat bereits ein Mandate, BusinessObjectRef.mandate_id=<" +  b.MANDATE_ID + ">")
  }
  
  def addPayment( p:Payment) : BusinessObjectRefDom = md match {
    case None    => this //throw new RuntimeException( "BusinessObjectRefDom hat kein Mandate") // Fehler bei: 311779, 312585, 312794
    case Some(m) => 
      if( m.m.MANDATE_ID != p.MANDATE_ID) 
        throw new RuntimeException( "BusinessObjectRefDom MandateId stimmt nicht mit Payment ueberein")
      BusinessObjectRefDom( b, Some(MandateDom(m.m, p::m.lp)))
  }
  
  /**
   * Abgelaufenes Mandat oder kein Mandat vorhanden
   */
  def isOutOfDate: Boolean = md.map( _.istAbgelaufen()).getOrElse(true)
  
  /**
   * Terminierte Mandate
   */
  def isTerminated : Boolean = md.map( _.isTerminated).getOrElse(true)
  
  def mandateExtRef : Option[String] = md.flatMap( _.mandateExtRef )
  def mandateId     : Option[Long]   = md.map(    _.mandateId )
}

object BusinessObjectRefDom {
  
  def apply( b:BusinessObjectRef) : BusinessObjectRefDom = BusinessObjectRefDom(b, None)

  def apply( ob:Option[BusinessObjectRef], m:Mandate, lp:List[Payment]) : Option[BusinessObjectRefDom] = 
    ob.flatMap( x => Some(apply( x, m, lp)))
    
  def apply( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : BusinessObjectRefDom = BusinessObjectRefDom(b, Some(MandateDom(m, lp)))

  def apply( b:BusinessObjectRef, md: MandateDom) : BusinessObjectRefDom = BusinessObjectRefDom(b, Some(md))
  
  def apply( lbor:List[BusinessObjectRef], lm:List[Mandate], lp:List[Payment]) : Map[Long,BusinessObjectRefDom] = {

    // Mappe zur MandatsId, Busines_obj_refernce_id
    val revMap : Map[Long,Long] = lbor.foldLeft(Map.empty[Long,Long])((acc,b) => acc.updated( b.MANDATE_ID, b.BUSINESS_OBJ_REFERENCE_ID))

    // Mappe mit BORD
    val aca  = lbor.foldLeft( Map.empty[Long,BusinessObjectRefDom])( 
       (acc,bor) => acc.get(bor.BUSINESS_OBJ_REFERENCE_ID) match {
         case Some(m) => acc // Gibts nicht 
         case None    => acc.updated( bor.BUSINESS_OBJ_REFERENCE_ID, BusinessObjectRefDom(bor))
       })
       
    // BORD mit Mandate anreichern   
    val acb = lm.foldLeft( aca)( (acc,m) => 
      revMap.get(m.MANDATE_ID) match { 
        case None => acc // sollte es nicht geben
        case Some(id) =>
          acc.get( id )match {
            case None      => acc // Mandate ohne Vertrag werden ignoriert
            case Some(bor) => acc.updated( bor.b.BUSINESS_OBJ_REFERENCE_ID, bor.addMandate(m))
      }})
    
    //BORD Payments einfügen
    val acc = lp.foldLeft( acb)( (acc,p) =>  
        revMap.get(p.MANDATE_ID)  match {
      case None      => acc
      case Some(id)  =>
        acc.get(id) match {
          case Some(bor) => acc.updated( bor.b.BUSINESS_OBJ_REFERENCE_ID, bor.addPayment(p))
          case None      => acc
        }})
    
    acc
  }
  
  /**
   * Aufbau eines einzelnen BusinessObjectRefDom top down
   */
  def apply( bor:BusinessObjectRef, lm:List[Mandate], lp:List[Payment]) : BusinessObjectRefDom = {

    
    // BORD mit Mandate anreichern   
    val bordm = lm.foldLeft( BusinessObjectRefDom(bor))( (bord,m) => bord.addMandate(m))
        
    //BORD Payments einfügen
    val acc = lp.foldLeft( bordm)( (bord,p) => bordm.addPayment(p))
    
    acc
  }
  
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