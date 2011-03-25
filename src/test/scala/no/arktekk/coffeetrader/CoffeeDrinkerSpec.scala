package no.arktekk.coffeetrader

import dispatch._
import Http._
import org.apache.http.client.methods.HttpOptions
import org.apache.http.HttpStatus
import org.specs.Specification
import xml.Elem
import java.net.{MalformedURLException, URL}

object CoffeeDrinkerSpec extends Specification with DispatchUtil {
  def doAuth(auth: Option[(String, String)], req: Request): Request = auth.map(a => req as_! (a._1, a._2)).getOrElse(req)

  def put(location: String, elem: Elem, auth: Option[(String, String)] = None) = {
    val (headers, (entity, code)) = Http(doAuth(auth, location) <:< xmlTypes <<< elem.toString >+ {
      h => (h >:> pass, h >+ {
        h => (h <> pass, h >?)
      })
    })
    (headers, entity, code)
  }

  "added order to coffee shop" should {
    val (headers, orderEntity, location) = postOrder

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
    val (_, _, location) = postOrder

    val (headers, orderEntity, _) = put(location,
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
    val (_, _, location) = postOrder

    val (headers, orderEntity, _) = put(location,
      <order>
        <finished>true</finished>
      </order>)

    "result in conflict on further updates" in {
      put(location,
        <order>
          <additions>Shot</additions>
        </order>) must throwA[StatusCode].like {
        case StatusCode(HttpStatus.SC_CONFLICT, _) => true
      }
    }
  }

  def putPayment(paymentUrl: String, orderEntity: Elem, auth: Option[(String, String)] = None) = {
    val payment =
      <payment>
        <cardNo>123456789</cardNo>
        <expires>07/07</expires>
        <name>John Citizen</name>
        <amount>3.00</amount>
      </payment>
    put(paymentUrl, payment, auth)
  }

  "paying for beverage" should {
    val (_, orderEntity, _) = postOrder

    val auth = Some(("joe", "doe"))
    val paymentUrl = (orderEntity \\ "next" \\ "@uri").text
    "give us a valid url for payment" in {
      new URL(paymentUrl) mustNot throwA[MalformedURLException]
    }
    val (headers, paymentEntity, _) = putPayment(paymentUrl, orderEntity, auth)

    "contain location" in {
      headers("Location").head must_== paymentUrl
    }

    "possible to retrieve" in {
      Http(doAuth(auth, paymentUrl) <:< xmlTypes <> pass) must_== paymentEntity
    }
  }

  "paying for beverage without authentication" should {
    val (_, orderEntity, _) = postOrder

    val paymentUrl = (orderEntity \\ "next" \\ "@uri").text
    "result in no authorisation error" in {
      putPayment(paymentUrl, orderEntity) must throwA[StatusCode].like {
        case StatusCode(HttpStatus.SC_UNAUTHORIZED, _) => true
      }
    }
  }

  "paying for beverage multiple times" should {
    val (_, orderEntity, _) = postOrder

    val auth = Some(("joe", "doe"))
    val paymentUrl = (orderEntity \\ "next" \\ "@uri").text

    val (_, _, createCode) = putPayment(paymentUrl, orderEntity, auth)
    "first time create" in {
      createCode must_== HttpStatus.SC_CREATED
    }

    val (_, _, updateCode) = putPayment(paymentUrl, orderEntity, auth)
    "second time update" in {
      updateCode must_== HttpStatus.SC_OK
    }
  }

}

// TODO: Should the error reply be in same media type (
// TODO: Query for media-types and operations should work everywhere?
// TODO: Add OPTIONS to dispatch
