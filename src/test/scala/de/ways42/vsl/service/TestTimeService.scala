package de.ways42.vsl.service


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import de.ways42.vsl.connection.Connect


class TestTimeService  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  test( "TS-Heute-vor-drei-Jahren") {
    val d = TimeService.getCurrentTimeYearsBefore(3)
    println( "Heute vor drei Jahren " + d)
	  assert(  d.compareTo( TimeService.getCurrentDate()) < 0 )
  }
}
