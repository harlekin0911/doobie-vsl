package de.ways42.vsl.domains.vslMandate.domain

import de.ways42.vsl.domains.zik.tables.Tzik012




/**
 * Vsl-Domain mit seinen Rollen
 */

case class ZikDomain( nktonr:String, lz:List[Tzik012]) {
  
  def add( z012:Tzik012) : ZikDomain = 
    if ( nktonr == z012.Z_NKTO_NR) 
      ZikDomain( nktonr, z012::lz) 
    else throw new RuntimeException("Die Nebenkontonummern stimmen nicht ueberein: nktonr=<" + nktonr + "> z012.nktonr=<" + z012.Z_NKTO_NR + ">")
  
}

object ZikDomain {
  
  def apply( nktonr:String) : ZikDomain = ZikDomain( nktonr, Nil)
  
  def apply(z:Tzik012) : ZikDomain = ZikDomain( z.Z_NKTO_NR, z::Nil)

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