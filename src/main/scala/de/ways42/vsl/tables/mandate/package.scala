package de.ways42.vsl.tables


/**
 * Aufbau der Datenbank
 * 
 * BusinessObjectExtRefKey 1 <---> n BusinessObjectRef 1 <---> 1 Mandate 1 <---> n Payments 
 */
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
  type MandateExtDom    = (BusinessObjectRef, MandateDom)

  /**
   * BusinessObjectReference and it's according Mandate with all its payments
   */
  type MandateExtAktDom = (BusinessObjectRef, MandateAktDom)
  
  
  /**
   * Liste aller Mandatsstrukturen zu einem Vertrag
   */
  type MandateDomain = ( String, Map[Long,MandateExtDom])

  /**
   * Liste aller Mandatsstrukturen zu einem Vertrag nur mit dem juengsten Payment
   */
  type MandateAktDomain = ( String, Map[Long,MandateExtAktDom])
}