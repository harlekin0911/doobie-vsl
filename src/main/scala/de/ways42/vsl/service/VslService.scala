package de.ways42.vsl.service

import doobie.free.ConnectionIO
import de.ways42.vsl.tables.vsmadm.Tvsl002
import de.ways42.vsl.tables.vsmadm.Tvsl001

object VslService {
  
  def aktVertragMitVersicherungen( vertrag:Tvsl001, versicherungen:List[Tvsl002]) : ( Tvsl001,Map[Short,Tvsl002]) = 
    ( vertrag, versicherungen.foldLeft(Map.empty[Short,Tvsl002])( (m,v) => m.updated( v.LV_VERS_NR, v)))
  
}