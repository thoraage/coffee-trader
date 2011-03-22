package no.arktekk.coffeetrader

import net.liftweb.http.rest.RestHelper
import java.util.Random
import net.liftweb.common.{Box, Full}
import net.liftweb.http._
import xml.{NodeSeq, Node, Elem}

object OrderResource extends RestHelper with RestExtensions {
  val mediaType = "application/xml"
  var orders = Map[Long, Elem]()
  val random = new Random

  def addChildren(elem: Elem, nodes: NodeSeq) = elem.copy(child = elem.child ++ nodes.toSeq)

  def findAndDo(orderId: String, f: Elem => LiftResponse): Box[LiftResponse] =
    Box(orders.get(orderId.toLong).map(f).orElse(Some(NotFoundResponse())))

  serve {
    case XmlPost("order" :: Nil, (requestElem, req)) =>
      val orderId = random.nextLong
      val location = pathify(req, orderId.toString)
      val elem = addChildren(requestElem, <cost>3.0</cost><next rel="pay" uri={location} type={mediaType}/>.toSeq)
      orders = orders + (orderId -> elem)
      Full(new CreatedResponse(elem, mediaType) {
        override def headers = ("Location" -> location) :: super.headers
      })
    case XmlGet("order" :: orderId :: Nil, req) =>
      Box(orders.get(orderId.toLong).map(e => XmlResponse(e, mediaType)).orElse(Some(NotFoundResponse())))
    case Options("order" :: orderId :: Nil, req) =>
      findAndDo(orderId, e => OptionsResponse("GET", "PUT"))
  }

}

// TODO: The RestHelper should add location to created response
// TODO: CreatedResponse / XmlResponse inconsequent response type
// TODO: Add OPTIONS on LiftResponse
