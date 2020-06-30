package de.ways42.vsl.tables.mandate

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.implicits._
import cats.data.Validated._
import cats.data._



case class MandateDomain( extRef:String, mmed:Map[Long,BusinessObjectRefDom]) {
  
  def add( bor: BusinessObjectRef) :  MandateDomain = {
      
    if ( bor.BUSINESS_OBJ_EXT_REF != extRef) 
        throw new RuntimeException( "Vertragsnummern stimmen nicht ueberein")
      
    if ( ! mmed.get( bor.BUSINESS_OBJ_REFERENCE_ID).isEmpty)
        throw new RuntimeException( "BusinessObjectRefId bereits vorhanden")
      
    MandateDomain( extRef, mmed.updated( bor.BUSINESS_OBJ_REFERENCE_ID, BusinessObjectRefDom( bor,None)))
  }
  
  def add ( bord: BusinessObjectRefDom): MandateDomain = MandateDomain( extRef, mmed.updated( bord.b.BUSINESS_OBJ_REFERENCE_ID, bord))

}

object MandateDomain {
  
  
  def apply( ob:Option[BusinessObjectRefDom]) : Option[MandateDomain] = ob.map( apply(_))
    
  def apply( med:BusinessObjectRefDom) : MandateDomain = MandateDomain(med.b.BUSINESS_OBJ_EXT_REF, Map((med.b.BUSINESS_OBJ_REFERENCE_ID, med)))
  
  /**
   * Construcion Bottom UP
   */
  def apply( mmed:Map[Long,BusinessObjectRefDom]) : Map[String,MandateDomain] = mmed.foldLeft( Map.empty[String,MandateDomain])( 
      (acc,mmed) => acc.get( mmed._2.BUSINESS_OBJ_EXT_REF) match {
        case Some(md) => acc.updated( mmed._2.b.BUSINESS_OBJ_EXT_REF, md.add(mmed._2))
        case None     => acc.updated( mmed._2.b.BUSINESS_OBJ_EXT_REF, MandateDomain( mmed._2))
        }
      )
  /**
   * Construcion Bottom UP
   */
  def apply( lmed:List[BusinessObjectRefDom]) : Map[String,MandateDomain] = lmed.foldLeft( Map.empty[String,MandateDomain])( 
      (acc,bord) => acc.get( bord.BUSINESS_OBJ_EXT_REF) match {
        case Some(md) => acc.updated( bord.BUSINESS_OBJ_EXT_REF, md.add(bord))
        case None     => acc.updated( bord.BUSINESS_OBJ_EXT_REF, MandateDomain( bord))
        }
      )
      
   /**
    * Construction Top->Down   
    */
  def apply( lbor:List[BusinessObjectRef], lm:List[Mandate], lp:List[Payment]) : Map[String,MandateDomain] = 
    BusinessObjectRefDom( lbor, lm, lp).foldLeft( Map.empty[String,MandateDomain])( (acc,mbor) => 
        acc.get( mbor._2.BUSINESS_OBJ_EXT_REF) match {
          case None      => acc.updated( mbor._2.BUSINESS_OBJ_EXT_REF, MandateDomain( mbor._2))
          case Some( md) => acc.updated( mbor._2.BUSINESS_OBJ_EXT_REF, md.add( mbor._2))
        })
        
      
  
  import scala.language.implicitConversions

  implicit def MandateDomain2MandateDomainOps( m:MandateDomain) = new MandateDomainOps(m)
  
  class MandateDomainOps ( val m:MandateDomain)
}