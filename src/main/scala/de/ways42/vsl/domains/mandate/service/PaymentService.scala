package de.ways42.vsl.domains.mandate.service

import de.ways42.vsl.domains.mandate.tables.Payment

object PaymentService {
  
  def getLatestPayment( lp : List[Payment]) : Option[Payment] = lp match {
	  case Nil => None
		case _   => Some( lp.max(Payment.orderByScheduledDueDate))
	}
}