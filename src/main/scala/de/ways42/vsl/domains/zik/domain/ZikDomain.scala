package de.ways42.vsl.domains.zik.domain

import de.ways42.vsl.domains.zik.tables.Tzik012




/**
 * Zik-Domain, Stammdaten zum Nebenkonto
 * Die Stammdaten werden sortiert nach ihrer NebenkontoArt, UnterkontoArt ist immer 0
 * Die NebenkontoArt ist aus: 1,7,A,B,C
 */

case class ZikDomain( nktonr:String, mz:Map[String,List[Tzik012]]) {
  
  def add( z:Tzik012) : ZikDomain = 
    if ( nktonr == z.Z_NKTO_NR) 
      ZikDomain( nktonr, mz.get(z.Z_NKTOART_CD) match {
        case Some(l) => mz.updated( z.Z_NKTOART_CD, z::l)
        case _       => mz.updated( z.Z_NKTOART_CD, z::Nil) 
      })
    else throw new RuntimeException("Die Nebenkontonummern stimmen nicht ueberein: nktonr=<" + nktonr + "> z012.nktonr=<" + z.Z_NKTO_NR + ">")
  
  lazy val vtgnr = nktonr.substring(0, 12)
  
}

object ZikDomain {
  
  def apply( nktonr:String) : ZikDomain = ZikDomain( nktonr, Map.empty)
  
  def apply(z:Tzik012) : ZikDomain = ZikDomain( z.Z_NKTO_NR, Map(( z.Z_NKTOART_CD, z::Nil)))

  /**
   * Construction top down, Mappe [VtgNr,MandateRefDom] aufbauen
   */
  def apply(  lz:List[Tzik012]) : Map[String,ZikDomain] = {
    	  
    lz.foldLeft( Map.empty[String,ZikDomain])( (m,z) =>  m.get(z.Z_NKTO_NR)  match { 
			  case Some(v)  => m.updated( z.Z_NKTO_NR, v.add(z))
			  case _        => m.updated( z.Z_NKTO_NR, ZikDomain(z))
			})
  } 
}