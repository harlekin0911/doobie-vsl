package de.ways42.vsl.transaction

import java.sql.Date
import doobie.util.transactor.Transactor

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import java.util.GregorianCalendar

import de.ways42.vsl.domains.mandate.tables.Payment
import de.ways42.vsl.domains.mandate.tables.Mandate
import de.ways42.vsl.domains.mandate.tables.BusinessObjectRef


object MandateIO {
  
  def apply( xa : Transactor.Aux[IO, Unit]) : MandateIO = new MandateIO( xa)

}
  
class MandateIO( val xa : Transactor.Aux[IO, Unit]) 