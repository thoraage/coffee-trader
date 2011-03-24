package no.arktekk.coffeetrader

import net.liftweb.http.rest.RestHelper
import net.liftweb.common.Box
import util.MatchLong
import util.Resource._
import xml.Elem
import net.liftweb.http.{CreatedResponse, XmlResponse, NotFoundResponse, LiftResponse}

object PaymentResource extends RestHelper with RestExtensions {

  def findAndDo(orderId: Long, f: Elem => LiftResponse): Box[LiftResponse] =
    Box(Orders(orderId.toLong).map(f).orElse(Some(NotFoundResponse())))

  serve {
    case XmlPut("payment" :: "order" :: MatchLong(orderId) :: Nil, (requestElem, req)) =>
      findAndDo(orderId, {
        elem =>
          val exists = Payments(orderId) != None
          Payments(orderId -> requestElem)
          val location = pathify(req, req.path.partPath: _*)
          if (exists)
            XmlResponse(requestElem, mediaType)
          else
            new CreatedResponse(requestElem, mediaType) {
              override def headers = ("Location" -> location) :: super.headers
            }
      })

    case XmlGet("payment" :: "order" :: MatchLong(orderId) :: Nil, req) =>
      Box(Payments(orderId).map(e => XmlResponse(e, mediaType)).orElse(Some(NotFoundResponse())))
  }

}