package de.ways42.vsl.connection

object JdbcOptions {
  
  /**
   * Wird benoetigt bei: ConnectionIO.toOption bei leerem Result-Set
   * 
   * Den Fehler gibt's auch mit Hikari bei zu wenigen Threads
   */
  val db2Options = ":allowNextOnExhaustedResultSet=1;"
  	    
  //"jdbc:db2://172.17.4.39:50001/vslt01:driverType=4;fullyMaterializeLobData=true;fullyMaterializeInputStreams=true;progressiveStreaming=2;progresssiveLocators=2;"


}