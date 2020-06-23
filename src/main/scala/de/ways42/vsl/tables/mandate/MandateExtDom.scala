package de.ways42.vsl.tables.mandate

object MandateExtDom {
  
  def buildMandateExtDom( ob:Option[BusinessObjectRef], m:Mandate, lp:List[Payment]) : Option[MandateExtDom] = 
    ob.flatMap( x => Some(buildMandateExtDom( x, m, lp)))
    
  def buildMandateExtDom( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : MandateExtDom = (b, (m, lp))
  
  
  def buildValidated( b:BusinessObjectRef, m:Mandate, lp:List[Payment]) : Either[Throwable,MandateExtDom] = 
    validate( b.MANDATE_ID, m, lp).map( _ => buildMandateExtDom(b, m, lp))
  
  
  def validate( mid:Long, m:Mandate, lp:List[Payment]) : Either[Throwable, (Mandate,List[Payment])] = {
    
    def valM( mid:Long, m:Mandate) : Either[Throwable,Mandate] = 
      if ( mid != m.MANDATE_ID) Left(new Throwable("Fehlerhaftes Mandat mit abweichender Mandats_id:" + m.toString()))
      else Right(m)
      
    MandateDom.validate( mid, lp).fold( 
        t => valM( mid, m) match { case Left(t2) => Left(new Throwable( t.getMessage + t2.getMessage)); case _ => Right((m,lp))}, 
        _ => valM( mid, m) match { case Right(_)  => Right( m, lp); case Left(t) => Left(t)})
  }
    
    
  import scala.language.implicitConversions

  implicit def MandateExtDom2MandateExtDomOps( m:MandateExtDom) = new MandateExtDomOps(m)
  
  class MandateExtDomOps ( val m:MandateExtDom) {
  }
}