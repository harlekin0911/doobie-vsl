package de.ways42.vsl.domains.zik.service

import doobie.free.ConnectionIO
import de.ways42.vsl.domains.vsl.tables.Tvsl002
import de.ways42.vsl.domains.vsl.tables.Tvsl001
import de.ways42.vsl.domains.vsl.tables.Trol001
import de.ways42.vsl.domains.zik.tables.Tzik012




object ZikService {
  
    /**
   * Einen aktuellen Vertrag mit den zugehoerigen aktuellen Versicherungen parallel laden
   * Die Unterkonto-Art ist immer 0
   * NktoArt aus 1,7, A, B, C
   */
  def getAktNktoStammDaten( nktoNr:String, nktoArt:String) : ConnectionIO[Option[Tzik012]] = Tzik012.selectAktById( nktoNr, nktoArt, 0)
	    
    /**
   * Aktuellen Stammdaten zu einem Nebenkonto laden
   * Die Unterkonto-Art ist immer 0
   * NktoArt aus 1,7, A, B, C
   */
  def getAktNktoStammDatenByNkto( nktoNr:String) : ConnectionIO[List[Tzik012]] = Tzik012.selectAktByNkto(nktoNr)

    /**
   * Aktuellen Stammdaten zu einem Vertrag laden
   * Die Unterkonto-Art ist immer 0
   * NktoArt aus 1,7, A, B, C
   */
  def getAktNktoStammDatenByVertrag( nktoNr:String) : ConnectionIO[List[Tzik012]] = Tzik012.selectAktByNkto(nktoNr)
  
  /**
   * Alle aktuellen Stammdaten laden
   */
  def getAktAll() : ConnectionIO[List[Tzik012]] = Tzik012.selectAktAll()
  	
  /**
   * Alle  Stammdaten laden
   */
  def getAll() : ConnectionIO[List[Tzik012]] = Tzik012.selectAll()
}
