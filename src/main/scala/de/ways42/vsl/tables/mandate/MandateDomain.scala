package de.ways42.vsl.tables.mandate

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.implicits._
import cats.data.Validated._
import cats.data._

/** MandateDomain:
 * (string,Map[Long,(BusinessObjectRef,(Mandate,List[Payment]))])
 * (BUSINESS_OBJ_EXT_REF, Map[BUSINESS_OBJ_REFERENCE_ID,(BusinessObjectRef,(Mandate,List[Payment]))])
 * 
 * MandateExtDom:
 * (BusinessObjectRef,(MANDATE_ID, (Mandate,List[Payment])))
 */
object MandateDomain {
  
  def apply( ob:Option[MandateExtDom]) : Option[MandateDomain] = ob.map( apply(_))
    
  def apply( med:MandateExtDom) : MandateDomain = (med._1.BUSINESS_OBJ_EXT_REF, Map((med._1.BUSINESS_OBJ_REFERENCE_ID, med)))
  
  def apply( mmed:Map[Long,MandateExtDom]) : Map[String,Map[Long,MandateDomain]] = mmed.foldLeft( Map.empty[String,Map[Long,MandateDomain]])( 
      (acc,mmed) => acc.get( mmed._2._1.BUSINESS_OBJ_EXT_REF) match {
        case Some(u) => acc.updated( mmed._2._1.BUSINESS_OBJ_EXT_REF, u.updated( mmed._2._1.BUSINESS_OBJ_REFERENCE_ID, apply(mmed._2))) 
        case None    => acc.updated( mmed._2._1.BUSINESS_OBJ_EXT_REF, Map(( mmed._2._1.BUSINESS_OBJ_REFERENCE_ID, apply(mmed._2))))
        }
      )
  def apply( lmed:List[MandateExtDom]) : Map[String,Map[Long,MandateDomain]] = lmed.foldLeft( Map.empty[String,Map[Long,MandateDomain]])( 
      (acc,med) => acc.get( med._1.BUSINESS_OBJ_EXT_REF) match {
        case Some(u) => acc.updated( med._1.BUSINESS_OBJ_EXT_REF, u.updated( med._1.BUSINESS_OBJ_REFERENCE_ID, apply(med))) 
        case None    => acc.updated( med._1.BUSINESS_OBJ_EXT_REF, Map(( med._1.BUSINESS_OBJ_REFERENCE_ID, apply(med))))
        }
      )
      
  
    
  import scala.language.implicitConversions

  implicit def MandateDomain2MandateDomainOps( m:MandateDomain) = new MandateDomainOps(m)
  
  class MandateDomainOps ( val m:MandateDomain)
}