package de.ways42.vsl.service

import java.util.GregorianCalendar
import java.util.Calendar

object TimeService {

  def vadf( gc : GregorianCalendar) : (Int,Int) = {
    val y = gc.get(Calendar.YEAR) - 1900
    val m = gc.get(Calendar.MONTH)
    val d = gc.get(Calendar.DAY_OF_MONTH)
    
    val h  = gc.get(Calendar.HOUR_OF_DAY)
    val mm = gc.get(Calendar.MINUTE)
    val s  = gc.get(Calendar.SECOND)
    
    ( y*1000 + m*100 + d, h*1000 + mm*100 + s)
  }

  def vadf() : (Int,Int) = vadf( new GregorianCalendar())
  
  def getTimestamp( gc : GregorianCalendar) : java.sql.Timestamp = new java.sql.Timestamp( gc.getTimeInMillis);
    
  def getTimestamp() : java.sql.Timestamp = new java.sql.Timestamp( (new GregorianCalendar()).getTimeInMillis);
  
  def getCurrentDate() : java.util.Date = (new GregorianCalendar()).getTime()
  
  def getCurrentTimeYearsBefore( y:Int) : java.util.Date = {
    val c = new GregorianCalendar()
    c.set( Calendar.YEAR, c.get(Calendar.YEAR) - y)
    c.getTime()
  }
  
}