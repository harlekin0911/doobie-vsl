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

/**
 * Die Vertragsnummer im BusinessObjectRef kann mit leerzeichen aufgefuellt sein im extRef entfernen
 */

case class MandateDomain( extRef:String, mmed:Map[Long,BusinessObjectRefDom]) {
  
  def add( bor: BusinessObjectRef) :  MandateDomain = {
      
    if ( bor.BUSINESS_OBJ_EXT_REF.trim() != extRef) 
        throw new RuntimeException( 
            "Vertragsnummern stimmen nicht ueberein bor.BUSINESS_OBJ_EXT_REF<" + bor.BUSINESS_OBJ_EXT_REF + "> extRef<" + extRef + ">")
      
    if ( ! mmed.get( bor.BUSINESS_OBJ_REFERENCE_ID).isEmpty)
        throw new RuntimeException( "BusinessObjectRefId bereits vorhanden")
      
    MandateDomain( extRef, mmed.updated( bor.BUSINESS_OBJ_REFERENCE_ID, BusinessObjectRefDom( bor,None)))
  }
  
  def add ( bord: BusinessObjectRefDom): MandateDomain = MandateDomain( extRef, mmed.updated( bord.b.BUSINESS_OBJ_REFERENCE_ID, bord))
  
  /**
   * Liefert eine Liste von (BusinessObjectReferenceId,Mandate) zu dem die Mandate aufgrund von Signed Date und letzem Payment Datum abgelaufen sind,
   * das Datum liegt laenger als 3 Jahre zurueck
   */
  def getOutDatedMandates : List[(Long,Mandate)] =
    mmed.foldLeft(List.empty[(Long,Mandate)])( (acc,tbor) => if( tbor._2.isOutOfDate) (tbor._1,tbor._2.md.get.m)::acc else acc)
    
  /**
   * Anzahl der Mandate
   */
    
  def anzahlMandate = mmed.size
  
  def anzahlMandateAktive     = mmed.filter( ! _._2.isTerminated).size
  def anzahlMandateTerminated = mmed.filter(   _._2.isTerminated).size

}

object MandateDomain {
  
  def apply( vtgnr:String) : MandateDomain = MandateDomain( vtgnr, Map.empty)
  
  def apply( ob:Option[BusinessObjectRefDom]) : Option[MandateDomain] = ob.map( apply(_))
    
  def apply( med:BusinessObjectRefDom) : MandateDomain = MandateDomain(med.b.BUSINESS_OBJ_EXT_REF.trim(), Map((med.b.BUSINESS_OBJ_REFERENCE_ID, med)))
  
  /**
   * Construcion Bottom UP
   */
  def apply( mmed:Map[Long,BusinessObjectRefDom]) : Map[String,MandateDomain] = mmed.foldLeft( Map.empty[String,MandateDomain])( 
      (acc,mmed) => acc.get( mmed._2.BUSINESS_OBJ_EXT_REF.trim()) match {
        case Some(md) => acc.updated( mmed._2.b.BUSINESS_OBJ_EXT_REF.trim(), md.add(mmed._2))
        case None     => acc.updated( mmed._2.b.BUSINESS_OBJ_EXT_REF.trim(), MandateDomain( mmed._2))
        }
      )
  /**
   * Construcion Bottom UP
   */
  def apply( lmed:List[BusinessObjectRefDom]) : Map[String,MandateDomain] = lmed.foldLeft( Map.empty[String,MandateDomain])( 
      (acc,bord) => acc.get( bord.BUSINESS_OBJ_EXT_REF.trim()) match {
        case Some(md) => acc.updated( bord.BUSINESS_OBJ_EXT_REF.trim(), md.add(bord))
        case None     => acc.updated( bord.BUSINESS_OBJ_EXT_REF.trim(), MandateDomain( bord))
        }
      )
      
   /**
    * Construction Top->Down   
    */
  def apply( lbor:List[BusinessObjectRef], lm:List[Mandate], lp:List[Payment]) : Map[String,MandateDomain] = 
    BusinessObjectRefDom( lbor, lm, lp).foldLeft( Map.empty[String,MandateDomain])( (acc,mbor) => 
        acc.get( mbor._2.BUSINESS_OBJ_EXT_REF.trim()) match {
          case None      => acc.updated( mbor._2.BUSINESS_OBJ_EXT_REF.trim(), MandateDomain( mbor._2))
          case Some( md) => acc.updated( mbor._2.BUSINESS_OBJ_EXT_REF.trim(), md.add( mbor._2))
        })
  /**
   * Konstruktion eines einzelnen Stand eines MandateDomain aus den Basis-Tabellen        
   */
  def apply( bor:BusinessObjectRef, lm:List[Mandate], lp:List[Payment]) : MandateDomain = 
    MandateDomain( BusinessObjectRefDom( bor, lm, lp))
      
  
  import scala.language.implicitConversions

  implicit def MandateDomain2MandateDomainOps( m:MandateDomain) = new MandateDomainOps(m)
  
  class MandateDomainOps ( val m:MandateDomain)
}