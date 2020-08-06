package de.ways42.vsl.domains.vslMandate.domain


import de.ways42.vsl.domains.vsl.tables.Trol001
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import de.ways42.vsl.domains.mandate.domain.MandateDomain
import de.ways42.vsl.domains.vsl.domain.MandateRefDom

/**
 * Vsl-Domain mit seinen Rollen
 */

case class VslMandateDomain( vtgnr:String, omrd:Option[MandateRefDom], omdom:Option[MandateDomain], lz:List[ZikDomain]) {
  
  def add( mrd:MandateRefDom) = {
    
    if ( ! omrd.isEmpty)
      throw new RuntimeException( "Option[MandateRefDom] ist nicht empty")
  
    if ( mrd.vtgnr != vtgnr ) 
      throw new RuntimeException( "Vertragsnummer<" + vtgnr + "> und MandateRefDom.vtgnr<" + mrd.vtgnr + "> stimmen nicht ueberein")
    
    VslMandateDomain( vtgnr, Some(mrd), omdom, lz)
  }
  
  def add( md:MandateDomain) = {
    if ( ! omdom.isEmpty)
      throw new RuntimeException( "Option[MandateRefDom] ist nicht empty")
    if ( md.extRef != vtgnr ) 
      throw new RuntimeException( "Vertragsnummer<" + vtgnr + "> und MandateDomain.extRef<" +  md.extRef + "> stimmen nicht ueberein")
    VslMandateDomain( vtgnr, omrd, Some(md), lz)
  }
  
  /**
   * Gueltiger Vertrag
   */
  def isAufrecht =  omrd.map( _.isAufrecht).getOrElse(false) 
  
 /**
  * Ist beitagsfrei
  */
  def istBfr : Boolean = omrd.map( _.istBfr).getOrElse(false)
 
 /**
  * Ist beitagspflichtig
  */
  def istBpfl : Boolean = omrd.map( _.istBpfl).getOrElse(false)
 
 /**
  * Ist reserve
  */
  def isReserve : Boolean = omrd.map( _.isReserve).getOrElse(false)
  /**
  * Ist beitagspflichtig, falsch in der DB
  * Nur der Vertrag, keine Versicherung ist beitragspflichti
  */
  def istBpflNurVertrag : Boolean = omrd.map( _.istBpflNurVertrag).getOrElse(false)

  /**
  * Ist beitagspflichtig, falsch in der DB
  * Der Vertrag ist beitragsfrei, aber es gbt beitragspflichtige Versicherungen
  */
  def istBpflNurVers : Boolean = omrd.map( _.istBpflNurVers).getOrElse(false)
  
  /**
   * Validate Mandates
   * ueberpruefung ob die Mandate in Mandatsverwalung und Rolle uebereinstimmen
   */
  
  def validateMandate() : Boolean = {
    
    if ( omrd.isEmpty   && omdom.isEmpty)   return true;
    if ( omrd.isDefined && omdom.isEmpty)   return false;
    if ( omrd.isEmpty   && omdom.isDefined) return false;
    
    val rollen = omrd.get.lr1
    val mmandate = omdom.get.mmed
        
    val rollenOhneMandate = rollen.filter(   x => mmandate.find( y => y._2.mandateExtRef.map( _ == x.mandateExtRef).getOrElse(false)).isEmpty)
    val mandateOhneRollen = mmandate.filter( y =>   rollen.find( x => y._2.mandateExtRef.map( _ == x.mandateExtRef).getOrElse(false)).isEmpty)
    
    rollenOhneMandate.isEmpty && mandateOhneRollen.isEmpty
  }
}

object VslMandateDomain {
  
  def apply( vtgnr:String, v:Option[MandateRefDom],mm:Option[MandateDomain]) = {
    if ( v.isDefined && v.get.vtgnr != vtgnr)
      throw  throw new RuntimeException("Vtgnr<" + vtgnr + "> und MandateRefDom.vtgnr<" + v.get.vtgnr + "> stimmen nicht ueberein")
    if ( mm.isDefined && mm.get.extRef != vtgnr)
      throw  throw new RuntimeException("Vtgnr<" + vtgnr + "> und MandateDomain.extRef<" + mm.get.extRef + "> stimmen nicht ueberein")
    new VslMandateDomain( vtgnr, v, mm, Nil)
  }
  
  /**
   * In der Rolle sind Leerzeichen bei isttop_nrx
   */
  def apply( mrd:MandateRefDom) : VslMandateDomain = VslMandateDomain(mrd.vtgnr, Some(mrd), None)
  
  def apply( md:MandateDomain) : VslMandateDomain = VslMandateDomain( md.extRef, None, Some(md))

  def apply( mrd:MandateRefDom, md:MandateDomain) : VslMandateDomain = 
    if ( mrd.vtgnr  != md.extRef ) 
      throw new RuntimeException(
          "Vertragsnummern stimmen nicht ueberein: MandateRefDom.vtgnr=<" + mrd.vtgnr + "> MandateDomain.extRef=<" + md.extRef + ">")
    else VslMandateDomain( md.extRef, Some(mrd), Some(md))
  /**
   * Construction top down, Mappe [VtgNr,MandateRefDom] aufbauen
   */
  def apply(  mmrd:Map[String,MandateRefDom], mmd:Map[String,MandateDomain]) : Map[String,VslMandateDomain] = {
	  
    val c = mmrd.map( x => (x._1, VslMandateDomain( x._1, Some(x._2), None)))
    	  
    mmd.foldLeft( c)( (m,v) =>  m.get(v._2.extRef)  match { 
			  case Some(vm)  => m.updated(v._2.extRef, vm.add(v._2))
			  case _         => m.updated(v._2.extRef, VslMandateDomain( v._2))
			})
  } 
}