package de.ways42.vsl.domains.vslMandate.domain


import de.ways42.vsl.domains.vsl.tables.Trol001
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import de.ways42.vsl.domains.mandate.domain.MandateDomain
import de.ways42.vsl.domains.vsl.domain.VslRefDom
import de.ways42.vsl.domains.zik.domain.ZikDomain

/**
 * Vsl-Domain mit seinen Rollen
 */

case class VslMandateDomain( vtgnr:String, oVslr:Option[VslRefDom], omdom:Option[MandateDomain], mz:Map[String,ZikDomain]) {
  
  def add( mrd:VslRefDom) = {
    
    if ( ! oVslr.isEmpty)
      throw new RuntimeException( "Option[MandateRefDom] ist nicht empty")
  
    if ( mrd.vtgnr != vtgnr ) 
      throw new RuntimeException( "Vertragsnummer<" + vtgnr + "> und MandateRefDom.vtgnr<" + mrd.vtgnr + "> stimmen nicht ueberein")
    
    VslMandateDomain( vtgnr, Some(mrd), omdom, mz)
  }
  
  def add( md:MandateDomain) = {
    if ( ! omdom.isEmpty)
      throw new RuntimeException( "Option[MandateRefDom] ist nicht empty")
    if ( md.extRef != vtgnr ) 
      throw new RuntimeException( "Vertragsnummer<" + vtgnr + "> und MandateDomain.extRef<" +  md.extRef + "> stimmen nicht ueberein")
    VslMandateDomain( vtgnr, oVslr, Some(md), mz)
  }
  
  def add( z:ZikDomain) : VslMandateDomain = {
    VslMandateDomain( vtgnr, oVslr, omdom, mz.get( z.nktonr) match {
      case Some(a) => throw new RuntimeException( "Nebenkonto bereits vorhanden: <" + z.nktonr + ">")
      case _       => mz.updated(z.nktonr,z)
    })
  }
    
  
  
  /**
   * Gueltiger Vertrag
   */
  def isAufrecht =  oVslr.map( _.isAufrecht).getOrElse(false) 
  
 /**
  * Ist beitagsfrei
  */
  def istBfr : Boolean = oVslr.map( _.istBfr).getOrElse(false)
 
 /**
  * Ist beitagspflichtig
  */
  def istBpfl : Boolean = oVslr.map( _.istBpfl).getOrElse(false)
 
 /**
  * Ist reserve
  */
  def isReserve : Boolean = oVslr.map( _.isReserve).getOrElse(false)
  /**
  * Ist beitagspflichtig, falsch in der DB
  * Nur der Vertrag, keine Versicherung ist beitragspflichti
  */
  def istBpflNurVertrag : Boolean = oVslr.map( _.istBpflNurVertrag).getOrElse(false)

  /**
  * Ist beitagspflichtig, falsch in der DB
  * Der Vertrag ist beitragsfrei, aber es gbt beitragspflichtige Versicherungen
  */
  def istBpflNurVers : Boolean = oVslr.map( _.istBpflNurVers).getOrElse(false)
  
  /**
   * Validate Mandates
   * ueberpruefung ob die Mandate in Mandatsverwalung und Rolle uebereinstimmen
   */
  
  def validateMandate() : Boolean = {
    
    if ( oVslr.isEmpty   && omdom.isEmpty)   return true;
    if ( oVslr.isDefined && omdom.isEmpty)   return false;
    if ( oVslr.isEmpty   && omdom.isDefined) return false;
    
    val rollen = oVslr.get.lr1
    val mmandate = omdom.get.mmed
        
    val rollenOhneMandate = rollen.filter(   x => mmandate.find( y => y._2.mandateExtRef.map( _ == x.mandateExtRef).getOrElse(false)).isEmpty)
    val mandateOhneRollen = mmandate.filter( y =>   rollen.find( x => y._2.mandateExtRef.map( _ == x.mandateExtRef).getOrElse(false)).isEmpty)
    
    rollenOhneMandate.isEmpty && mandateOhneRollen.isEmpty
  }
}

object VslMandateDomain {
  
  def apply( vtgnr:String, v:Option[VslRefDom],mm:Option[MandateDomain]) = {
    if ( v.isDefined && v.get.vtgnr != vtgnr)
      throw  throw new RuntimeException("Vtgnr<" + vtgnr + "> und MandateRefDom.vtgnr<" + v.get.vtgnr + "> stimmen nicht ueberein")
    if ( mm.isDefined && mm.get.extRef != vtgnr)
      throw  throw new RuntimeException("Vtgnr<" + vtgnr + "> und MandateDomain.extRef<" + mm.get.extRef + "> stimmen nicht ueberein")
    new VslMandateDomain( vtgnr, v, mm, Map.empty)
  }
  
  /**
   * In der Rolle sind Leerzeichen bei isttop_nrx
   */
  def apply( mrd:VslRefDom) : VslMandateDomain = VslMandateDomain(mrd.vtgnr, Some(mrd), None)
  
  def apply( md:MandateDomain) : VslMandateDomain = VslMandateDomain( md.extRef, None, Some(md))

  def apply( zd:ZikDomain) : VslMandateDomain = VslMandateDomain( zd.vtgnr, None, None, Map((zd.nktonr,zd)))

  def apply( mrd:VslRefDom, md:MandateDomain, mzd:Map[String,ZikDomain]) : VslMandateDomain = {
    if ( mrd.vtgnr  != md.extRef ) 
    
    if ( ! mzd.filter( _._2.vtgnr != mrd.vtgnr).isEmpty)
      throw new RuntimeException(
          "Zik-Stammdaten stimmen nicht ueberein: MandateRefDom.vtgnr=<" + mrd.vtgnr + "> MandateDomain.extRef=<" + md.extRef + ">")
    
    VslMandateDomain( md.extRef, Some(mrd), Some(md), mzd)
  }
  /**
   * Construction top down, Mappe [VtgNr,MandateRefDom] aufbauen
   */
  def apply(  mmrd:Map[String,VslRefDom], mmd:Map[String,MandateDomain], mzd:Map[String,ZikDomain]) : Map[String,VslMandateDomain] = {
	  
    val c = mmrd.map( x => (x._1, VslMandateDomain( x._1, Some(x._2), None)))
    	  
    val b = mmd.foldLeft( c)( (m,v) =>  m.get(v._2.extRef)  match { 
			  case Some(vm)  => m.updated(v._2.extRef, vm.add(v._2))
			  case _         => m.updated(v._2.extRef, VslMandateDomain( v._2))
			})
			
		mzd.foldLeft(b)((m,z) =>  m.get( z._2.vtgnr)  match { 
			  case Some(vm)  => m.updated( z._2.vtgnr, vm.add(z._2))
			  case _         => m.updated( z._2.vtgnr, VslMandateDomain( z._2))
			})
  } 
}