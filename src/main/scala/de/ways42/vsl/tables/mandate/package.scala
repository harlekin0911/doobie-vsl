package de.ways42.vsl.tables

package object mandate {
  
  /**
   * Mandate with all its Payments
   */
  type MandateDom    = (Mandate, List[Payment])
  
  /**
   * Mandate with actual Payment when exists
   */
  type MandateAktDom = (Mandate, Option[Payment])
  
  /**
   * BusinessObjectReference and it's according actual Mandate
   */
  type MandateExtDom    = (BusinessObjectRef, List[MandateDom])

  /**
   * BusinessObjectReference and it's according Mandate with all its payments
   */
  type MandateExtAktDom = (BusinessObjectRef, List[MandateAktDom])
  
  
  /**
   * Liste aller Mandatsstrukturen zu einem Vertrag
   */
  type MandateDomain = ( String, List[BusinessObjectRef])
}