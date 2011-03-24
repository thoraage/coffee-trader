package no.arktekk.coffeetrader

import dispatch.{StatusCode, Request, Http}
import Http._
import org.apache.http.client.methods.HttpOptions
import org.apache.http.HttpStatus
import org.specs.Specification
import xml.Elem

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

  def createOrder = {
    val ordersUri = address + "/order"
    val order = <order>
      <drink>Latte</drink>
    </order>
    val (headers, orderEntity) = Http(ordersUri <:< xmlTypes << order.toString >+ {
      h => (h >:> pass, h <> pass)
    })
    (headers, orderEntity, headers("Location").head)
  }

  def updateOrder(location: String, orderUpdate: Elem) = {
    Http(location <:< xmlTypes <<< orderUpdate.toString >+ {
      h => (h >:> pass, h <> pass)
    })
  }

  "added order to coffee shop" should {
    val (headers, orderEntity, location) = createOrder

    "contain location" in {
      headers must haveKey("Location")
    }

    "possible to retrieve" in {
      Http(location <:< xmlTypes <> pass) must_== orderEntity
    }

    "be updateable" in {
      val options = Http((location <:< xmlTypes).OPTIONS >:> pass)
      options must haveKey("Allow")
      options("Allow").flatMap(_.split(",")).map(_.trim) mustContain ("PUT")
    }

    /*"give head" in {
      Http((location <:< xmlTypes).HEAD >- pass) must_== "doh"
    }*/
  }

  "updated order to coffee shop" should {
    val (_, _, location) = createOrder

    val (headers, orderEntity) = updateOrder(location,
      <order>
        <additions>Shot</additions>
      </order>)

    "contains additions" in {
      orderEntity \\ "additions" must_== <additions>Shot</additions> \\ "additions"
    }

    "cost is 4.0" in {
      (orderEntity \\ "cost").text must_== "4.0"
    }
  }

  "updating finished order to coffee shop" should {
    val (_, _, location) = createOrder

    val (headers, orderEntity) = updateOrder(location,
      <order>
        <finished>true</finished>
      </order>)

    "result in conflict on further updates" in {
      updateOrder(location,
        <order>
          <additions>Shot</additions>
        </order>) must throwA[StatusCode].like { case StatusCode(HttpStatus.SC_CONFLICT, _) => true }
    }
  }

}
// TODO: Query for media-types and operations should work everywhere?
// TODO: Add OPTIONS to dispatch
