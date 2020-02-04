package de.ways42.vsl.connection


import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.hikari._

//object Pool {
  

  object HikariApp extends IOApp {

  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO]    // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
//              "org.h2.Driver",                        // driver classname
//              "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",   // connect URL
//              "sa",                                   // username
//              "",                                     // password
					"com.ibm.db2.jcc.DB2Driver", // driver classname
//					"jdbc:db2://172.17.4.39:50001/vslt01", // connect URL (driver-specific)
					"jdbc:db2://172.17.4.39:50013/vslt03", // connect URL (driver-specific)
					"vsmadm",              // user
					"together",                       // password
              ce,                                     // await connection here
              be                                      // execute JDBC operations here
            )
    } yield xa


  def run(args: List[String]): IO[ExitCode] =
    transactor.use { xa =>

      // Construct and run your server here!
      for {
        n <- sql"select 42 from sysibm.sysdummy1".query[Int].unique.transact(xa)
        _ <- IO(println(n))
      } yield ExitCode.Success

    }
  }

//  def main( args : Array[String]) : Unit = HikariApp.run(args.toList)
//}