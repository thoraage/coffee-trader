package no.arktekk.coffeetrader

import dispatch.{StatusCode, Http}
import Http._
import org.apache.http.HttpStatus
import org.specs.Specification

object BaristaSpec extends Specification with DispatchUtil {

  "listing drinks" should {
    Http((ordersUrl DELETE) >|)
    postOrder
    postOrder

    val entries = Http(ordersUrl <:< atomTypes <> pass)
    "contain orders" in {
      (entries \\ "entry").size must_== 2
    }
  }

  "listing drinks with wrong media type" should {
    "lead to unsupported media type" in {
      Http(ordersUrl <:< xmlTypes <> pass) must throwA[StatusCode].like {
        case StatusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, _) => true
      }
    }
  }

  // TODO: Add Expires
  "retrieve drink entry" should {
    val entries = Http(ordersUrl <:< atomTypes <> pass)
    val ids = (entries \\ "entry" \\ "id").map(_.text)
    val entry = Http(ids.head.trim <:< atomTypes <> pass)
    "contain drink" in {
      (entry \\ "drink").text must_== "Latte"
    }
  }

}