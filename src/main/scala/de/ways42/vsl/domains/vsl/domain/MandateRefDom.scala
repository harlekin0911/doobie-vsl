package de.ways42.vsl.domains.vsl.domain


import de.ways42.vsl.domains.vsl.tables.Trol001
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Tvsl002

/**
 * Vsl-Domain mit seinen Rollen
 */

case class MandateRefDom( vtgnr:String, vsldom:Option[VslDom], lr1:List[Trol001]) {
  
  def add( r1:Trol001) = 
    if ( r1.ISTTOP_NRX.trim() == vtgnr ) 
      MandateRefDom( vtgnr, vsldom, r1::lr1)
    else throw new RuntimeException( "Vertragsnummer<" + vtgnr + "> und rolle.isttop_nrx<" + r1.ISTTOP_NRX.trim() + "> stimmen nicht ueberein")
  
 /**
  * Ist beitagspflichtig
  */
  def istBpfl : Boolean = vsldom.map( _.istBpfl).getOrElse(false)
 
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
  
}

object MandateRefDom {
  
  /**
   * In der Rolle sind Leerzeichen bei isttop_nrx
   */
  def apply( r1:Trol001) : MandateRefDom = MandateRefDom( r1.ISTTOP_NRX.trim(), None, List(r1))
  
  def apply( vsld:VslDom) : MandateRefDom = MandateRefDom( vsld.tvsl001.LV_VTG_NR, Some(vsld), Nil)

  /**
   * Construction top down, Mappe [VtgNr,MandateRefDom] aufbauen
   */
  def apply(  lv:List[Tvsl001], lvers:List[Tvsl002], lrol:List[Trol001]) : Map[String,MandateRefDom] = {
	  
    val mvsld = VslDom( lv, lvers).map( x => (x._1, MandateRefDom( x._2)))
    	  
	  // Versicherungen in die Mappe fuellen
	  def aggregateWithRolle( mm : Map[String, MandateRefDom], lrol:List[Trol001])  : Map[String, MandateRefDom] = 
	    lrol.foldRight(mm)( (r,m) =>  m.get(r.ISTTOP_NRX.trim())  match { 
			  case Some(v)   => m.updated(r.ISTTOP_NRX.trim(), v.add(r))
			  case _         => m.updated(r.ISTTOP_NRX.trim(), MandateRefDom( r))
			})
      
		aggregateWithRolle( mvsld, lrol)
  } 
  
    /**
   * Construction eines einzelnen MandateRefDom
   */
  def apply(  lv:Tvsl001, lvers:List[Tvsl002], lrol:List[Trol001]) : MandateRefDom = 
    lrol.foldLeft(MandateRefDom(VslDom( lv, lvers)))( (v,r) =>  v.add(r))
    	  
}