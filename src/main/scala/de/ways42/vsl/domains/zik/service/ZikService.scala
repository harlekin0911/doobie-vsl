package de.ways42.vsl.domains.zik.service

import doobie.free.ConnectionIO
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Trol001
import de.ways42.vsl.domains.zik.tables.Tzik012




object ZikService {
  
    /**
   * Einen aktuellen Vertrag mit den zugehoerigen aktuellen Versicherungen parallel laden
   */
  def getAktNktoStammDaten( nktoNr:String, ukto:String) : ConnectionIO[Option[Tzik012]] = Tzik012.selectAktById( nktoNr, ukto, 0)
	    
  	
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
	    Tvsl002.selectAktAllAktive())

	/**
	 * Alle aktuellen aufrechten Vertraege mit ihren aktuellen Versicherungen parallel laden 
	 */
  def getAllActiveVertraegeWithVersicherungenMandate() : (ConnectionIO[List[Tvsl001]], ConnectionIO[List[Tvsl002]], ConnectionIO[List[Trol001]]) = (
	    Tvsl001.selectAktAllAktive(),
	    Tvsl002.selectAktAllAktive(),
	    Trol001.selectAktAllAktive(89))
	/**
	 * Alle aktuellen aufrechten Vertraege mit ihren aktuellen Versicherungen parallel laden 
	 */
  def getAllVertraegeWithVersicherungenMandate() : (ConnectionIO[List[Tvsl001]], ConnectionIO[List[Tvsl002]], ConnectionIO[List[Trol001]]) = (
	    Tvsl001.selectAktAll(),
	    Tvsl002.selectAktAll(),
	    Trol001.selectAktAll(89))
}
