package de.ways42.vsl.service

import doobie.free.ConnectionIO
import de.ways42.vsl.tables.vsmadm.Tvsl002
import de.ways42.vsl.tables.vsmadm.Tvsl001
import de.ways42.vsl.tables.vsmadm.VslDom



object VslService {
  
  
  def aktVertragMitVersicherungen( vertrag:Tvsl001, versicherungen:List[Tvsl002]) : ( Tvsl001,Map[Short,Tvsl002]) = 
    ( vertrag, versicherungen.foldLeft(Map.empty[Short,Tvsl002])( (m,v) => m.updated( v.LV_VERS_NR, v)))
    
  def buildAktVertraegeMitVersicherungen( lv:List[Tvsl001], lvers:List[Tvsl002]) : Map[String,(Tvsl001, Map[Short,Tvsl002])] = 
      aggregateVertragWithVers( aggregateVertragWithEmptyVers(lv),lvers)

 /**
	 * Mappe mit leeren Versicherungen aufbauen
	 */
	def aggregateVertragWithEmptyVers( mv:List[Tvsl001]) : Map[String, VslDom] = 
	  mv.foldRight( Map.empty[String,( Tvsl001, Map[Short,Tvsl002])])((m,acc) => acc.updated(m.LV_VTG_NR, (m,Map.empty)))
	  
	/**
	 * Versicherungen in die Mappe fuellen
	 */
	def aggregateVertragWithVers( mv : Map[String, VslDom], pl : List[Tvsl002])  : Map[String, VslDom] = 
	    pl.foldRight(mv)( (p,m) =>  m.get(p.LV_VTG_NR)  match { 
			  case Some(k)   => m.updated(p.LV_VTG_NR, (k._1, k._2.updated(p.LV_VERS_NR, p)))
			  case _         => m
			})
}
