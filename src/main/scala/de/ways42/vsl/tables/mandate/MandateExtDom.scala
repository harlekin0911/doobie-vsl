package de.ways42.vsl.tables.mandate

object MandateExtDom {
  
  def buildMandateExtDom( ob:Option[BusinessObjectRef], lm:List[Mandate], lp:List[Payment]) : Option[MandateExtDom] = 
    ob.flatMap( x => Some(buildMandateExtDom( x, lm, lp)))
    
  def buildMandateExtDom( b:BusinessObjectRef, lm:List[Mandate], lp:List[Payment]) : MandateExtDom = {
    val mm = lm.groupBy( _.MANDATE_ID)
    val mp = lp.groupBy( _.MANDATE_ID)
    (b, mm.foldLeft(List.empty[MandateDom])( (acc,e) => MandateDom(e._2.head, mp(e._1))::acc))
  }  
  
  def buildValidated( b:BusinessObjectRef, lm:List[Mandate], lp:List[Payment]) : Either[Throwable,MandateExtDom] = 
    validate( b.MANDATE_ID, lm, lp).map( _ => buildMandateExtDom(b, lm, lp))
  
  
  def validate( mid:Long, lm:List[Mandate], lp:List[Payment]) : Either[Throwable, (List[Mandate],List[Payment])] = {
    val fm = lm.filter( _.MANDATE_ID != mid)
    val fp = lm.filter( _.MANDATE_ID != mid)
    
    if ( fm.size > 0 || fp.size > 0) {
      val em = fm.fold("Fehlerhafte Mandate mit abweichender MandatsID: ")( (f,m) => f + " " + m.toString())
      val ep = fp.fold(em + "Fehlerhafte Payments mit abweichender MandatsID: ")((f,p) => f + " " + p.toString())
      
      Left( new Throwable( "Objekte mit falscher MandatsId, " + ep))
    } else 
      Right( (lm, lp))
  }
    
    
  import scala.language.implicitConversions

  implicit def MandateExtDom2MandateExtDomOps( m:MandateExtDom) = new MandateExtDomOps(m)
  
  class MandateExtDomOps ( val m:MandateExtDom) {
  }
}