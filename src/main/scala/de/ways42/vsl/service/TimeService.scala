package de.ways42.vsl.service

import java.util.GregorianCalendar
import java.util.Calendar

object TimeService {
  
  def vadf() : (Int,Int) = {
    val d  = new GregorianCalendar()
    val y = d.get(Calendar.YEAR) - 1900
    val m = d.get(Calendar.MONTH)
    val t = d.get(Calendar.DAY_OF_MONTH)
    
    val h  = d.get(Calendar.HOUR_OF_DAY)
    val mm = d.get(Calendar.MINUTE)
    val s  = d.get(Calendar.SECOND)
    
    ( y*1000 + m*100 + t, h*1000 + mm*100 + s)

  }
}