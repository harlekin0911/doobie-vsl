package de.ways42.vsl.domains.vsl.service

import doobie.free.ConnectionIO
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Trol001




object VslService {
  
    /**
   * Einen aktuellen Vertrag mit den zugehoerigen aktuellen Versicherungen parallel laden
   */
  def getVertragWithVersicherung( vtgnr : String) : (ConnectionIO[Option[Tvsl001]], ConnectionIO[List[Tvsl002]]) = (
	    Tvsl001.selectAktById( vtgnr),
	    Tvsl002.selectAktZuVertrag(vtgnr))
  	
	/**
	 * Alle aktuellen aufrechten Vertraege mit ihren aktuellen Versicherungen parallel laden 
	 */
  def getAllActiveVertraegeWithVersicherungen() : (ConnectionIO[List[Tvsl001]], ConnectionIO[List[Tvsl002]]) = (
	    Tvsl001.selectAktAllAktive(),
	    Tvsl002.selectAktAktiveAll())

	/**
	 * Alle aktuellen aufrechten Vertraege mit ihren aktuellen Versicherungen parallel laden 
	 */
  def getAllActiveVertraegeWithVersicherungenMandate() : (ConnectionIO[List[Tvsl001]], ConnectionIO[List[Tvsl002]], ConnectionIO[List[Trol001]]) = (
	    Tvsl001.selectAktAllAktive(),
	    Tvsl002.selectAktAktiveAll(),
	    Trol001.selectAktAll(89))
}
