package de.ways42.vsl.tables

package object mandate {
  
  type MandateDom    = (Mandate, List[Payment])
  type MandateAktDom = (Mandate, Option[Payment])
  
  type MandateExtDom    = (BusinessObjectRef, List[MandateDom])
  type MandateExtAktDom = (BusinessObjectRef, List[MandateAktDom])
}