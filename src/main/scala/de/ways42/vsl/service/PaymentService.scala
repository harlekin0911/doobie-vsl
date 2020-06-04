package de.ways42.vsl.service

import de.ways42.vsl.tables.mandate.Payment

object PaymentService {
  
  def getLatestPayment( lp : List[Payment]) : Option[Payment] = lp match {
	  case Nil => None
		case _   => Some( lp.max(Payment.orderByScheduledDueDate))
	}
}