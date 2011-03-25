package no.arktekk.coffeetrader

import net.liftweb.http.rest.RestHelper
import java.util.Random
import net.liftweb.http._
import util.MatchLong
import util.Resource._
import xml.{NodeSeq, Elem}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import net.liftweb.common.{Box, Full}

object OrderResource extends RestHelper with RestExtensions {
  val random = new Random

  def addChildren(elem: Elem, nodes: NodeSeq) = elem.copy(child = elem.child ++ nodes.toSeq)

  def replace(elem: Elem, name: String, nodes: NodeSeq) = elem.copy(child = nodes ++ elem.child.filter(_.label != name))

  def findAndDo(orderId: Long, f: Elem => LiftResponse): Box[LiftResponse] =
    Box(Orders(orderId.toLong).map(f).orElse(Some(NotFoundResponse())))

  serve {
    case XmlPost("order" :: Nil, (requestElem, req)) =>
      val orderId = random.nextLong
      val location = pathify(req, req.path.partPath :+ orderId.toString: _*)
      val paymentLocation = pathify(req, "payment", "order", orderId.toString)
      val elem = addChildren(requestElem, <cost>3.0</cost> <next rel="pay" uri={paymentLocation} type={xmlMediaType}/>.toSeq)
      Orders(orderId -> elem)
      Full(new CreatedResponse(elem, xmlMediaType) {
        override def headers = ("Location" -> location) :: super.headers
      })
    case XmlGet("order" :: MatchLong(orderId) :: Nil, req) =>
      Box(Orders(orderId).map(e => XmlResponse(e, xmlMediaType)).orElse(Some(NotFoundResponse())))
    case Options("order" :: MatchLong(orderId) :: Nil, req) =>
      findAndDo(orderId, e => OptionsResponse("GET", "PUT"))
  }
  // Doh https://lampsvn.epfl.ch/trac/scala/ticket/1133
  serve {
    case Delete("order" :: Nil, _) =>
      Orders.removeAll
      Full(NoContentResponse())
    case Get("order" :: Nil, req) =>
      if (req.contentType.getOrElse("") != atomMediaType)
        Full(UnsupportedContentTypeResponse(atomMediaType))
      else {
        val response = <feed>
          <title>Coffee Queue</title>
          <updated>
            {(new DateTime).toString(ISODateTimeFormat.dateTimeNoMillis)}
          </updated>
          <author>
            <name>Coffee Trader</name>
          </author>
          <id>urn:coffee-maker:coffee-queue</id>{import Function.tupled
          Orders.findAllEntries map tupled {
            (id, elem) =>
              val location = pathify(req, req.path.partPath :+ id.toString: _*)
              <entry>
                  <link rel="alternate" type={xmlMediaType} uri={location}/>
                <id>
                  {location}
                </id>
              </entry>
          }}
        </feed>
        Full(XmlResponse(response, atomMediaType))
      }
  }
  serve {
    case XmlPut("order" :: MatchLong(orderId) :: Nil, (requestElem, req)) =>
      Box(Orders(orderId.toLong).map {
        elem =>
          if ((elem \\ "finished").text == "true") {
            ConflictResponse("Coffee is finished")
          } else {
            var e = addChildren(elem, requestElem.child)
            if ((requestElem \\ "additions") != NodeSeq.Empty) {
              e = replace(e, "cost", <cost>4.0</cost>)
            }
            Orders(orderId -> e)
            XmlResponse(e, xmlMediaType)
          }
      }.orElse(Some(NotFoundResponse())))
  }

}

// TODO: What about Expect: 100-Continue
// TODO: The RestHelper should add location to created response
// TODO: CreatedResponse / XmlResponse inconsequent response type
// TODO: Add OPTIONS on LiftResponse
// TODO: Instead of matching path & media-type & method => only match path and treat everything there
