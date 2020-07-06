package de.ways42.vsl.domains.vsl.service

import doobie.free.ConnectionIO
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.domain.VslDom



object VslService {
  
  
    
  def buildAktVertraegeMitVersicherungen( lv:List[Tvsl001], lvers:List[Tvsl002]) : Map[String,VslDom] = 
      aggregateVertragWithVers( aggregateVertragWithEmptyVers(lv),lvers)

 /**
	 * Mappe mit leeren Versicherungen aufbauen
	 */
	def aggregateVertragWithEmptyVers( mv:List[Tvsl001]) : Map[String, VslDom] = 
	  mv.foldRight( Map.empty[String,VslDom])((m,acc) => acc.updated(m.LV_VTG_NR, VslDom(m)))
	  
	/**
	 * Versicherungen in die Mappe fuellen
	 */
	def aggregateVertragWithVers( mv : Map[String, VslDom], pl : List[Tvsl002])  : Map[String, VslDom] = 
	    pl.foldRight(mv)( (p,m) =>  m.get(p.LV_VTG_NR)  match { 
			  case Some(k)   => m.updated(p.LV_VTG_NR, VslDom(k.tvsl001, k.mtvsl002.updated(p.LV_VERS_NR, p)))
			  case _         => m
			})
}
