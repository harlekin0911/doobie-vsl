package de.ways42.vsl

object TestResults {
  
  object Vertrag {
  
    val alle   = 457232
  
    object Aufrecht {
      
      val alle = 239742
      val bfr  = 72583
  
      val bpfl = 158981
      
      val bpflNurVertrag = 8169
      val bpflNurVers = 10
    }
  
     val nichtTerminierteAbgelaufene = 59909
  
     val nichtTerminierteAbgelaufeneMitPayment =  46638
  }
  val  outOfDate     = 114786
  val  outOfDateTerm = 61330
  
  object VertragUndRolle {
    
    val alle        = 445090
    val ohneVertrag = 2523
    val ohneMandat  = 155834
    val beides      = 158357
    
    object Aufrecht {
      val alle        = 358432
      val ohneVertrag = 118689
      val ohneMandat  = 69181
    }
  }
  
  object VslMandate {
     object AlleOhneMandate {
       val alle = 155514 
     }
     object Aktive {
       val alle         = 457288 // vorher 371795
       val ohneMandate  = 154332 // vorher 68839   
       val ohneVertrag  = 13363   
       val aufrecht     = 239743 
       val bfr          = 72583   
       val bpfl         = 158980
       val bfrAufrecht  = 72583    
       val bfrNotValid  = 0        
       val reserve      = 0 
      val bpflNurVertrag = 8169
      val bpflNurVers    = 10

     }
  }
  
}