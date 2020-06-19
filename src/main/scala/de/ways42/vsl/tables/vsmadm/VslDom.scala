package de.ways42.vsl.tables.vsmadm


object VslDom {
  
  import scala.language.implicitConversions
  
  
  implicit def tvsl001Tvsl002ToVslDom( t1:Tvsl001, t2:Tvsl002)            : VslDom = (t1,Map((t2.LV_VERS_NR,t2)))
  implicit def tvsl001Tvsl002ToVslDom( t1:Tvsl001, t2:Map[Short,Tvsl002]) : VslDom = (t1,t2)

  implicit def vertrVers2VslDomOps( vv:(Tvsl001, Map[Short,Tvsl002])) : VslDomOps = new VslDomOps( vv)

 /**
  * Ist beitagspflichtig
  */
  def istBpfl( vd:VslDom) : Boolean = vd._1.LV_VERTR_STAT_CD == 0 && vd._2.filter( _._2.LV_VERS_STAT_CD == 0).size > 0
 
  /**
  * Ist beitagspflichtig, falsch in der DB
  * Nur der Vertrag, keine Versicherung ist beitragspflichti
  */
  def istBpflNurVertrag( vd:VslDom) : Boolean = vd._1.LV_VERTR_STAT_CD >  0 && vd._2.filter( _._2.LV_VERS_STAT_CD == 0).size > 0

  /**
  * Ist beitagspflichtig, falsch in der DB
  * Der Vertrag ist beitragsfrei, aber es gbt beitragspflichtige Versicherungen
  */
  def istBpflNurVers( vd:VslDom) : Boolean = vd._1.LV_VERTR_STAT_CD == 0 && vd._2.filter( _._2.LV_VERS_STAT_CD == 0).size == 0

  class VslDomOps( vd:VslDom) {
    
    def istBpfl : Boolean          = VslDom.istBpfl( vd)
    def istBpflNurVertrag : Boolean= VslDom.istBpflNurVertrag( vd)
    def istBpflNurVers : Boolean   = VslDom.istBpflNurVers( vd)
  }
}