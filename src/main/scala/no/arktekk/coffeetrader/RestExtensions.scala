package no.arktekk.coffeetrader

import net.liftweb.http._

trait RestExtensions {

  protected object Options {
    def unapply(r: Req): Option[(List[String], Req)] =
      if (options_?(r))
        Some(r.path.partPath -> r) else None
    def options_?(r: Req) = r.requestType match {
      case rt: UnknownRequest => rt.method == "OPTIONS"
      case _ => false
    }
  }
  case class OptionsResponse(methods: String*) extends LiftResponse with HeaderDefaults {
    def toResponse = InMemoryResponse(Array(), ("Allow" -> methods.mkString(", ")) :: headers, cookies, 204)
  }

  def pathify(req: Req, pathParts: String*) = (req.hostAndPath :: req.path.partPath ::: pathParts.toList).mkString("/")

}