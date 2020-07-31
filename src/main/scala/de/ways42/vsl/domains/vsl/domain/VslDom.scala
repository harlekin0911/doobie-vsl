package de.ways42.vsl.domains.vsl.domain


import de.ways42.vsl.domains.vsl.tables.Tvsl002
import de.ways42.vsl.domains.vsl.tables.Tvsl001

case class VslDom( tvsl001:Tvsl001, mtvsl002: Map[Short,Tvsl002]) {
  
  def addVersicherung( t2:Tvsl002) : VslDom = 
    if ( t2.LV_VTG_NR != tvsl001.LV_VTG_NR) 
      throw new RuntimeException( "Die Vertragsnummern stimmen nicht ueberein: " + tvsl001.LV_VTG_NR + " " + t2.LV_VTG_NR)
    else VslDom( tvsl001, mtvsl002.updated(t2.LV_VERS_NR, t2))
 
  /**
   * Gueltige Vertraege
   */
  def isAufrecht : Boolean = tvsl001.isAufrecht && mtvsl002.find( _._2.isAufrecht).isDefined
 
  /**
   * Ist beitagspflichtig
   */
  def istBpfl : Boolean = tvsl001.istBpfl && mtvsl002.find( _._2.istBpfl).isDefined
 
  /**
  * Ist beitagspflichtig, falsch in der DB
  * Nur der Vertrag, keine Versicherung ist beitragspflichtig
  */
  def istBpflNurVertrag : Boolean = tvsl001.istBpfl && mtvsl002.find( _._2.istBpfl).isEmpty

  /**
  * Ist beitagspflichtig, falsch in der DB
  * Der Vertrag ist beitragsfrei, aber es gbt beitragspflichtige Versicherungen
  */
  def istBpflNurVers : Boolean = tvsl001.istBfr && mtvsl002.find( _._2.istBpfl).isDefined
  
}

object VslDom {
  
  def apply( vertrag:Tvsl001) : VslDom = VslDom( vertrag, Map.empty[Short,Tvsl002])
  
  /**
   * Aufbau eines einzelnen Objektes
   */
  def apply( vertrag:Tvsl001, versicherungen:List[Tvsl002]) : VslDom = VslDom( vertrag, 
        versicherungen.foldLeft(Map.empty[Short,Tvsl002])( 
          (m,v) => 
            if ( v.LV_VTG_NR != vertrag.LV_VTG_NR) 
              throw new RuntimeException( "Die Vertragsnummern stimmen nicht ueberein: " + vertrag.LV_VTG_NR + " " + v.LV_VTG_NR)
            else if ( m.get(v.LV_VERS_NR).isDefined)
              throw new RuntimeException( "Zur Versicherung mit Nummer: " + v.LV_VERS_NR + " ist bereits eine Versicherung vorhanden")              
            else m.updated( v.LV_VERS_NR, v)))

  /**
   * Construction top down, Mappe [VtgNr,VslDom] aufbauen
   */
  def apply( lv:List[Tvsl001], lvers:List[Tvsl002]) : Map[String,VslDom] = {
  //def buildAktVertraegeMitVersicherungen( lv:List[Tvsl001], lvers:List[Tvsl002]) : Map[String,VslDom] = {
	  
    // Mappe mit leeren Versicherungen aufbauen
	  def aggregateVertragWithEmptyVers( mv:List[Tvsl001]) : Map[String, VslDom] = 
	    mv.foldRight( Map.empty[String,VslDom])((m,acc) => acc.updated(m.LV_VTG_NR, VslDom(m)))
	  
	  // Versicherungen in die Mappe fuellen
	  def aggregateVertragWithVers( mv : Map[String, VslDom], pl:List[Tvsl002])  : Map[String, VslDom] = 
	    pl.foldRight(mv)( (p,m) =>  m.get(p.LV_VTG_NR)  match { 
			  case Some(v)   => m.updated(p.LV_VTG_NR, v.addVersicherung(p))
			  case _         => m
			})
      
		aggregateVertragWithVers( aggregateVertragWithEmptyVers(lv),lvers)
  }
      
  
//  import scala.language.implicitConversions
//  
//  
//  implicit def tvsl001Tvsl002ToVslDom( t1:Tvsl001, t2:Tvsl002)            : VslDom = (t1,Map((t2.LV_VERS_NR,t2)))
//  implicit def tvsl001Tvsl002ToVslDom( t1:Tvsl001, t2:Map[Short,Tvsl002]) : VslDom = (t1,t2)
//
//  implicit def vertrVers2VslDomOps( vv:(Tvsl001, Map[Short,Tvsl002])) : VslDomOps = new VslDomOps( vv)
//
//
//  class VslDomOps( vd:VslDom) {
//    
//    def istBpfl : Boolean          = VslDom.istBpfl( vd)
//    def istBpflNurVertrag : Boolean= VslDom.istBpflNurVertrag( vd)
//    def istBpflNurVers : Boolean   = VslDom.istBpflNurVers( vd)
//  }
}