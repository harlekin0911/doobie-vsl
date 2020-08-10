package de.ways42.vsl.domains.vsl.domain


import de.ways42.vsl.domains.vsl.tables.Trol001
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Tvsl002

/**
 * VslRefDom: Vsl-Domain mit seinen Rollen
 * 
 * In der Rolle lr1 ist die Vertragsnummer (isttop_nrx) nicht gestrippt
 * Jedoch in der vtgnr
 */

case class VslRefDom( vtgnr:String, vsldom:Option[VslDom], lr1:List[Trol001]) {
  
  def add( r1:Trol001) = 
    if ( r1.ISTTOP_NRX.trim() == vtgnr ) 
      VslRefDom( vtgnr, vsldom, r1::lr1)
    else throw new RuntimeException( "Vertragsnummer<" + vtgnr + "> und rolle.isttop_nrx<" + r1.ISTTOP_NRX.trim() + "> stimmen nicht ueberein")

  /**
   * Bestehender Vertrag
   */
  def isAufrecht : Boolean = vsldom.map( _.isAufrecht).getOrElse( false)
 /**
  * Ist beitagspflichtig
  */
  def istBpfl : Boolean = vsldom.map( _.istBpfl).getOrElse(false)
  
  /**
   * Beitragsfrei
   */
  
  def istBfr : Boolean = vsldom.map( _.istBfr).getOrElse(false)
 
  /**
   * Reserve
   */
  
  def isReserve : Boolean = vsldom.map( _.isReserve).getOrElse(false)
  /**
  * Ist beitagspflichtig, falsch in der DB
  * Nur der Vertrag, keine Versicherung ist beitragspflichti
  */
  def istBpflNurVertrag : Boolean = vsldom.map( _.istBpflNurVertrag).getOrElse(false)

  /**
  * Ist beitagspflichtig, falsch in der DB
  * Der Vertrag ist beitragsfrei, aber es gbt beitragspflichtige Versicherungen
  */
  def istBpflNurVers : Boolean = vsldom.map( _.istBpflNurVers).getOrElse(false)
  
  /**
   * Anzahl Mandate
   */
  
  def anzahlMandate() = lr1.size
  
}

object VslRefDom {
  
  /**
   * In der Rolle sind Leerzeichen bei isttop_nrx
   */
  def apply( r1:Trol001) : VslRefDom = VslRefDom( r1.ISTTOP_NRX.trim(), None, List(r1))
  
  def apply( vsld:VslDom) : VslRefDom = VslRefDom( vsld.tvsl001.LV_VTG_NR, Some(vsld), Nil)

  /**
   * Construction top down, Mappe [VtgNr,MandateRefDom] aufbauen
   */
  def apply(  lv:List[Tvsl001], lvers:List[Tvsl002], lrol:List[Trol001]) : Map[String,VslRefDom] = {
	  
    val mvsld = VslDom( lv, lvers).map( x => (x._1, VslRefDom( x._2)))
    	  
	  // Versicherungen in die Mappe fuellen
	  def aggregateWithRolle( mm : Map[String, VslRefDom], lrol:List[Trol001])  : Map[String, VslRefDom] = 
	    lrol.foldRight(mm)( (r,m) =>  m.get(r.ISTTOP_NRX.trim())  match { 
			  case Some(v)   => m.updated(r.ISTTOP_NRX.trim(), v.add(r))
			  case _         => m.updated(r.ISTTOP_NRX.trim(), VslRefDom( r))
			})
      
		aggregateWithRolle( mvsld, lrol)
  } 
  
    /**
   * Construction eines einzelnen VslRefDom
   */
  def apply(  lv:Tvsl001, lvers:List[Tvsl002], lrol:List[Trol001]) : VslRefDom = 
    lrol.foldLeft(VslRefDom(VslDom( lv, lvers)))( (v,r) =>  v.add(r))
    	  
}