package de.ways42.vsl.connection


import org.scalatest.funsuite.AnyFunSuite //TestSuite

import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import java.util.concurrent.TimeUnit
import monix.execution.Callback




class TestConnectAsync  extends AnyFunSuite  { // with GeneratorDrivenPropertyChecks  { // with Matchers { // with PropertyChecks {
  
  val xa = Connect.usingOwnMonad()
    

  test( "Test-ConnectAsync-1") {
  		
    import monix.execution.Scheduler.Implicits.global
    sql"select 42 from sysibm.sysdummy1".query[Int].unique.transact( xa).runAsync( x => { assert( x.contains( 42)); println( "executed")})
  }
  
  test( "Test-ConnectAsync-parallel") {

    import monix.eval.Task
    import monix.execution.Scheduler.Implicits.global
    val t1 = sql"select lv_vtg_nr from tvsl002".query[String].to[List].transact(xa)//.executeAsync
    val t2 = Fragment.const( "select lv_vtg_nr from tvsl001").query[String].to[List].transact(xa)//.executeAsync
    val t3 = t1::t2::Nil
    var hans : List[List[String]] = Nil
    val r = Task.gather( t3 ).map( _.toList ).runSyncUnsafe(scala.concurrent.duration.Duration(300, TimeUnit.SECONDS)) //runAsync( x => { assert( x.isRight == true);  x match { case Right(l) => hans = l; println( hans)}})
    println( "hans: " + hans)
    println( "r: " + r.head.size + "'" + r.tail.head.size)
	}
  
  test( "TestConnectAsync-parallel-list") {
    val mxa = Connect.usingOwnMonad();

    import monix.eval.Task
    import monix.execution.Scheduler.Implicits.global
    
    
    val t1 = sql"select lv_vtg_nr from tvsl002".query[String].to[List]//.transact(xa).unsafeRunSync //runAsync(x => { println("Bin da")})
    
    val t2 = Fragment.const( "select lv_vtg_nr from tvsl001").query[String].to[List]//.transact(xa).runAsync(cb)
    
    val d = (t1.transact(xa), t1.transact(xa)).parMapN(_ :: _).runAsync( x => { println("Bin da"); println( x.getOrElse(Nil).size)})
    Thread.sleep(5000)
    val c = (t1.transact(xa), t1.transact(xa)).parMapN(_ :: _).runSyncUnsafe(scala.concurrent.duration.Duration(300, TimeUnit.SECONDS))
    //System.sleep (50)
    
   
    println ( "c: " + c.size)
    
    val t3 = t1::t2::Nil
    val cb1 = Callback.safe(cb)
//    var hans : List[List[String]] = Nil
//    Task.gather( t3 ).map( _.toList ).foreach(x => { hans = x; println( "odel")})
//    Task.gather( t3 ).map( _.toList ).runAsync( x => { assert( x.isRight == false);  x match { case Right(l) => hans = l; println( hans)}})
//     val l = Task.gather( t3 ).runAsync( cb1)
     //l.asRight
//    println( "hans: " + hans )
      println( "cb.b: " + cb.b )
	}
  
  import monix.execution.Callback
  
  class MyCB extends Callback[Throwable, List[String]]  {
    var b : List[List[String]] = Nil
    def onSuccess(value: List[String]): Unit = { b = value::b; println(value)}
    def onError(ex: Throwable): Unit = System.err.println(ex)
  }
  
  val cb : MyCB = new MyCB()
  
  test ( "Glaub-ichs") {
    import monix.execution.Scheduler.Implicits.global
    // A Future type that is also Cancelable
    import monix.execution.CancelableFuture    
    import monix.eval.Task
    
	  val task = Task { 1 + 1 }

	  // Tasks get evaluated only on runAsync!
	  // Callback style:
	  val cancelable = task.runAsync { result =>
	    result match {
	      case Right(value) =>
	        println(value)
	      case Left(ex) =>
	        System.out.println(s"ERROR: ${ex.getMessage}")
	      }
	    }
    }
  }
