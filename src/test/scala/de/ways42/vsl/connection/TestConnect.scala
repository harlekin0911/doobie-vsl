package de.ways42.vsl.connection


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import java.util.concurrent.TimeUnit
import monix.execution.Callback




class TestConnect  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
    val xa = Connect( "VSMADM", "together")

    test ( "Connect-1") {
			val program1 = 42.pure[ConnectionIO]
			assert ( program1.transact(xa).unsafeRunSync() == 42 )      
    }
    
    test ( "Connect-2") {
			val program2 = sql"select 43 from  SYSIBM.SYSDUMMY1".query[Int].unique
			val io2 = program2.transact(xa)
			assert( io2.unsafeRunSync == 43)
    }
    
    test ( "Connect-3") {
			val program2 = sql"select rand from  SYSIBM.SYSDUMMY1".query[Double].unique
			val io2 = program2.transact(xa)
			assert ( io2.unsafeRunSync match { case _ : Double => true/*; case _ => false*/ })
      
    }
    
    test ( "Connect-4") {
			assert( sql"SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1".query[String].unique.transact(xa).unsafeRunSync.trim() == "VSMADM")
			assert( sql"select * from VSMADM.TVSL001".query[String].to[List].transact(xa).unsafeRunSync.take(5).size == 5)    
    }
    
    test ( "Connect-5") {
			assert( sql"select * from VSMADM.TVSL001".query[String].stream.take(5).compile.toList.transact(xa).unsafeRunSync.size == 5) 
    }
    test ( "Connect-6") {
			val c = for {
				a <- sql"select 42 from  SYSIBM.SYSDUMMY1".query[Int].unique
				b <- sql"select  rand from  SYSIBM.SYSDUMMY1".query[Double].unique
			} yield (a, b)
					
			val d = c.transact( xa)
			assert ( d.unsafeRunSync._1 == 42)
      
    }
    test ( "Connect-7") {

			import scala.concurrent.ExecutionContext
			import cats.~>
			import cats.data.Kleisli
			import cats.effect.Blocker
			import doobie.free.connection.ConnectionOp
			import java.sql.Connection

			implicit val cs = IO.contextShift(ExecutionContext.global)

			val interpreter = KleisliInterpreter[IO](Blocker.liftExecutionContext(ExecutionContext.global)).ConnectionInterpreter

			val kleisli = 42.pure[ConnectionIO].foldMap(interpreter)

			val io = IO(null: java.sql.Connection) >>= kleisli.run

			io.unsafeRunSync // sneaky; program1 never looks at the connection
      
    }
}
