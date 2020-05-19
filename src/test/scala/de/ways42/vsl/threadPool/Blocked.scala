package de.ways42.vsl.threadPool

import scala.concurrent.ExecutionContext
import doobie.util.ExecutionContexts
import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.Future
import java.util.concurrent.TimeUnit

object Blocked {
  
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  def addOne(x: Int) = Future(x + 1)

  def multiply(x: Int, y: Int) = Future {
    val a = addOne(x)
    val b = addOne(y)
    val result = for (r1 <- a; r2 <- b) yield r1 * r2

    // This can dead-lock due to the limited size 
    // of our thread-pool!
    val d = Duration( 10, TimeUnit.SECONDS) // Duration.Inf
    Await.result(result, d)
  }
  
  def main( args : Array[String]) = multiply ( 3, 4).onComplete( x => println( x.get))
}