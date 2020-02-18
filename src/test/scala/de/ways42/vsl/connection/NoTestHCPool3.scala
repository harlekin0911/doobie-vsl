package de.ways42.vsl.connection




import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._
import java.util.concurrent.TimeUnit
import monix.execution.Callback
import scala.concurrent.Await





object NoTestHCPool3    { 
  
  def main( args : Array[String]) : Unit = {
    
//    t1
    t2
    t3
//    t4
    t5
    t6
    t7
//    t8
//    t9
    t10
    //Thread.sleep(10000)
    println("awake again")

  }
  
  val xap = HCPool2.xa
    
  import monix.eval.Task
	import monix.execution.Scheduler.Implicits.global
	import scala.concurrent.duration.Duration

	val t = (s:String ) => xap.use { xa =>
	    //val q = sql"select 42 from sysibm.sysdummy1".query[Int].unique.transact( xa)
	    val q = sql"select lv_vtg_nr from vsmadm.tvsl001".query[String].to[List].transact( xa)
	    //val q2 = q.runAsync( x => { assert( x.contains( 42)); println( "executed-1")})//.map( _=> ExitCode.Success))
	    //val q3 = q.runSyncUnsafe(scala.concurrent.duration.Duration(100, TimeUnit.SECONDS))
	    println( "inner-executed-t")
	    q.flatMap( n => {println(s + " flatmap: " + n.length);Task(n)})//.map( _=> ExitCode.Success))
	  } 

  // query
   def t1 =  {
    	t("t1").runSyncUnsafe(Duration(5000, TimeUnit.SECONDS))
    	println( "finished: t1" )
    }  
    
    // keine query
    def t2 =  {
    	t("t2").foreach{x =>println(x.length);println( "executed-t2: " + x.length)}                                     
    	println( "finished: t2" )
    }
    // keine query
    def t3 =  {
    	t("t3").runAsync( x => { assert( ! x.contains( Nil)); println( "executed-t3: "/* + x*/)}) 
    	println( "finished: t3" )
    }
    
    // query
    def t4 =  {
    	val f = t("t4").runToFuture                                       
    	Await.result( f, Duration(5000, TimeUnit.SECONDS))
    	println( "finished: t4" )
    }
    // keine query
    def t5 =  {
    	t("t5").runToFuture.foreach{ x => println(x.length);println( "executed-t5: " + x.length)}                         
    	println( "finished: t5" )
    }
    
    // keine query
    def t6 =  {
    	val cancelable = t("t6").runAsync { result =>                   
    	  result match {
    	    case Right(value) =>
    	      println(value.length)
    	      println( "executed-t6: " + value.length)
    	    case Left(ex) =>
    	      System.out.println(s"ERROR: ${ex.getMessage}")
    	      println( "executed-t6: " + ex)
    	  }
    	}
    	println( "finished: t6" )

    }
    
    // keine query
    def t7 =  {
	    t("t7").runSyncStep match {                                   
        case Left(future) =>
          // No luck, this Task really wants async execution
          future.foreach(r => println( "Async: " + r.length))
        case Right(result) =>
          println(s"Got lucky: $result.length")
      }
    	println( "finished: t7" )	    
    }
    // query
    def t8 =  {
      import scala.concurrent.duration._                           
	    Await.result( t("t8").executeAsync.runToFuture, 5000.seconds)
    	println( "finished: t8" )	    
    }
    // query mit await nicht mit foreach
    def t9 =  {
	    import monix.execution.Scheduler                            
	    import scala.concurrent.duration._
	    val s = Scheduler.io( "odel")
	    Await.result(t("t9").executeOn(s).runToFuture, 5.days)
    	println( "finished: t9" )
    }
    def t10 =  {
      import scala.util.Success
      t("t10").runToFuture.onComplete({case Success(n) => println(n.length); println( "executed-t10: " + n.length)})
    	println( "finished: t10" )
    }
    
}

    
  