package de.ways42.vsl


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._


class TestConnect  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  test( "FLTEST-Connect") {
    

    val xa = Connect( "VSMADM", "together")

	  println( odel0 (xa)) 
		println( odel1 (xa))
		println( odel2 (xa))
		         odel3 (xa)
		         odel4 (xa)
		println( odel5 (xa))
		println( odel6 (xa))
		println( Connect.usingOwnMonad())
	}
    

  
  	def odel0( xa : doobie.util.transactor.Transactor.Aux[IO, Unit]) : Int =  {
			val program1 = 42.pure[ConnectionIO]
			program1.transact(xa).unsafeRunSync()
	}

	def odel1( xa : doobie.util.transactor.Transactor.Aux[IO, Unit]) : Int =  {
			val program2 = sql"select 43 from  SYSIBM.SYSDUMMY1".query[Int].unique
			val io2 = program2.transact(xa)
			io2.unsafeRunSync
	}
	def odel2( xa : doobie.util.transactor.Transactor.Aux[IO, Unit]) : Double =  {
			val program2 = sql"select rand from  SYSIBM.SYSDUMMY1".query[Double].unique
					val io2 = program2.transact(xa)
					io2.unsafeRunSync
	}
	def odel3( xa : doobie.util.transactor.Transactor.Aux[IO, Unit]) : List[String] =  {
			println( sql"SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1".query[String].unique.transact(xa).unsafeRunSync)
			sql"select * from VSMADM.TVSL001".query[String].to[List].transact(xa).unsafeRunSync.take(5) //.foreach(println)
	}

	def odel4( xa : doobie.util.transactor.Transactor.Aux[IO, Unit]) : List[String] = {
			sql"select * from VSMADM.TVSL001".query[String].stream.take(5).compile.toList.transact(xa).unsafeRunSync // .foreach(println) 
	}


	def odel5(xa : doobie.util.transactor.Transactor.Aux[IO, Unit]) : (Int, Double) = {
			val c = for {
				a <- sql"select 42 from  SYSIBM.SYSDUMMY1".query[Int].unique
				b <- sql"select  rand from  SYSIBM.SYSDUMMY1".query[Double].unique
			} yield (a, b)
					val d = c.transact( xa)
					d.unsafeRunSync
	}


	def odel6( xa : doobie.util.transactor.Transactor.Aux[IO, Unit]) = {

			import scala.concurrent.ExecutionContext
			import cats.~>
			import cats.data.Kleisli
			import cats.effect.Blocker
			import doobie.free.connection.ConnectionOp
			import java.sql.Connection

			implicit val cs = IO.contextShift(ExecutionContext.global)

			val interpreter = KleisliInterpreter[IO](ExecutionContext.global).ConnectionInterpreter

			val kleisli = 42.pure[ConnectionIO].foldMap(interpreter)

			val io = IO(null: java.sql.Connection) >>= kleisli.run

			io.unsafeRunSync // sneaky; program1 never looks at the connection
	}

}
