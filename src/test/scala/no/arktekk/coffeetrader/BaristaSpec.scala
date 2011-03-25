package no.arktekk.coffeetrader

import dispatch.{StatusCode, Http}
import Http._
import org.apache.http.HttpStatus
import org.specs.Specification
import org.specs.util.TimeConversions._
import util.TimeHelpers._
import org.joda.time.{DateTime, Duration}

object BaristaSpec extends Specification with DispatchUtil {

  "listing drinks" should {
    Http((ordersUrl DELETE) >|)
    postOrder
    postOrder

    val (entries, headers) = Http(ordersUrl <:< atomTypes >+ (h => (h <> pass, h >:> pass)))
    "contain orders" in {
      (entries \\ "entry").size must_== 2
    }

    "should have expiring time" in {
      headers.get("Expires").flatMap(_.headOption).map {
        expires =>
          val duration = new Duration(new DateTime, new DateTime(expiresTimeFormatter.parse(expires)))
          duration.getMillis must beCloseTo(1.minute.at, 5.seconds.at)
      }.getOrElse(error("Missing expires header"))
    }
  }

  "listing drinks with wrong media type" should {
    "lead to unsupported media type" in {
      Http(ordersUrl <:< xmlTypes <> pass) must throwA[StatusCode].like {
        case StatusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, _) => true
      }
    }
  }

  "retrieve drink entry" should {
    val entries = Http(ordersUrl <:< atomTypes <> pass)
    val ids = (entries \\ "entry" \\ "id").map(_.text)
    val entry = Http(ids.head.trim <:< atomTypes <> pass)
    "contain drink" in {
      (entry \\ "drink").text must_== "Latte"
    }
  }

}