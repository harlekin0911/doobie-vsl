package de.ways42.vsl

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import monix.eval.Task


object Connect {


	def apply( user : String, passwd : String) : doobie.util.transactor.Transactor.Aux[IO, Unit] = {

		import scala.concurrent.ExecutionContext

		// We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
		// is where nonblocking operations will be executed.
		implicit val cs = IO.contextShift(ExecutionContext.global)

		// A transactor that gets connections from java.sql.DriverManager and excutes blocking operations
		// on an unbounded pool of daemon threads. See the chapter on connection handling for more info.

		val xa = Transactor.fromDriverManager[IO](
				"com.ibm.db2.jcc.DB2Driver",           // driver classname
				"jdbc:db2://172.17.4.39:50001/vslt01", // connect URL (driver-specific)
				user, 
				passwd
		    //Blocker.liftExecutionContext( ExecutionContext.global) // just for testing		
				)
				
		xa
	}


	def usingOwnMonad() : doobie.util.transactor.Transactor.Aux[Task, Unit] = {
			import monix.execution.Scheduler.Implicits.global 

			Transactor.fromDriverManager[Task]( 
					"com.ibm.db2.jcc.DB2Driver", // driver classname
					"jdbc:db2://172.17.4.39:50001/vslt01", // connect URL (driver-specific)
					"vsmadm",              // user
					"together"                       // password
					)
	}
}