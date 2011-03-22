package no.arktekk.coffeetrader

import dispatch.{Request, Http}
import Http._
import org.apache.http.client.methods.HttpOptions
import org.specs.Specification

object CoffeeTraderSpec extends Specification {
  def pass[A](a: A) = a

  val address = "http://localhost:8086"
  val mediaType = "application/xml"
  val xmlTypes = Map("Content-Type" -> mediaType, "Accept" -> mediaType)

  implicit def toRichRequest(req: Request) = new RichRequest(req)

  class RichRequest(req: Request) {
    def OPTIONS = req.next {
      Request.mimic(new HttpOptions) _
    }
  }

  "coffee shop" should {
    val ordersUri = address + "/order"
    val order = <order>
      <drink>Latte</drink>
    </order>
    val (headers, orderEntity) = Http(ordersUri <:< xmlTypes << order.toString >+ {
      h => (h >:> pass, h <> pass)
    })

    "created order containing location" in {
      headers must haveKey("Location")
    }

    val location = headers("Location").head
    "order possible to retrieve" in {
      Http(location <:< xmlTypes <> pass) must_== orderEntity
    }

    /*"give head" in {
      Http((location <:< xmlTypes).HEAD >- pass) must_== "doh"
    }*/

    "order is updateable" in {
      val options = Http((location <:< xmlTypes).OPTIONS >:> pass)
      options must haveKey("Allow")
      val actions = options("Allow").flatMap(_.split(",")).map(_.trim)
      actions mustContain("PUT")
    }
  }

  // TODO: spørring på mediatypes og operasjoner bør fungere overalt?
  // TODO: Add OPTIONS to dispatch
}
