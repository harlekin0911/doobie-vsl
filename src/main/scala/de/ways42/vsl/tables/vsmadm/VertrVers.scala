package de.ways42.vsl.tables.vsmadm

//class VertrVers( val vv: (Tvsl001, Map[Short,Tvsl002]))

object VertrVersOps {
  
  import scala.language.implicitConversions
  
  import de.ways42.vsl.tables.vsmadm.VertrVers
  
  implicit def tvsl001Tvsl002ToVertrVers( t1:Tvsl001, t2:Tvsl002)            : VertrVers = (t1,Map((t2.LV_VERS_NR,t2)))
  implicit def tvsl001Tvsl002ToVertrVers( t1:Tvsl001, t2:Map[Short,Tvsl002]) : VertrVers = (t1,t2)
  //implicit def tvsl001Tvsl002ToVertrVers( t1:(Tvsl001, Map[Short,Tvsl002])) : VertrVers = new VertrVers(t1)

//  implicit def vertrVers2VertrVersOps( vv:VertrVers) : VertrVersOps = new VertrVersOps( vv)
  implicit def vertrVers2VertrVersOps( vv:(Tvsl001, Map[Short,Tvsl002])) : VertrVersOps = new VertrVersOps( vv)

 /**
  * Ist beitagspflichtig
  */
  def istBpfl( vv:VertrVers) : Boolean = vv._1.LV_VERTR_STAT_CD == 0 && vv._2.filter( _._2.LV_VERS_STAT_CD == 0).size > 0
 
  /**
  * Ist beitagspflichtig, falsch in der DB
  * Nur der Vertrag, keine Versicherung ist beitragspflichti
  */
  def istBpflNurVertrag( vv:VertrVers) : Boolean = vv._1.LV_VERTR_STAT_CD >  0 && vv._2.filter( _._2.LV_VERS_STAT_CD == 0).size > 0

  /**
  * Ist beitagspflichtig, falsch in der DB
  * Der Vertrag ist beitragsfrei, aber es gbt beitragspflichtige Versicherungen
  */
  def istBpflNurVers( vv:VertrVers) : Boolean = vv._1.LV_VERTR_STAT_CD == 0 && vv._2.filter( _._2.LV_VERS_STAT_CD == 0).size == 0

  class VertrVersOps( vv:VertrVers) {
    
    def istBpfl : Boolean          = VertrVersOps.istBpfl( vv)
    def istBpflNurVertrag : Boolean= VertrVersOps.istBpflNurVertrag( vv)
    def istBpflNurVers : Boolean   = VertrVersOps.istBpflNurVers( vv)
  }
}