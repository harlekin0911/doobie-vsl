package de.ways42.vsl.transaction

import de.ways42.vsl.tables.mandate.Payment
import de.ways42.vsl.tables.mandate.Mandate
import java.sql.Date
import de.ways42.vsl.tables.mandate.BusinessObjectRef
import doobie.util.transactor.Transactor

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import java.util.GregorianCalendar


object MandateIO {
  
  def apply( xa : Transactor.Aux[IO, Unit]) : MandateIO = new MandateIO( xa)

}
  
class MandateIO( val xa : Transactor.Aux[IO, Unit]) 