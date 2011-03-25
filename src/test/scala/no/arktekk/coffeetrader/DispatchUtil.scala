package no.arktekk.coffeetrader

import dispatch.{Http, Handler, Handlers, Request}
import Http._
import org.apache.http.client.methods.HttpOptions

trait DispatchUtil {
  def pass[A](a: A) = a

  val address = "http://localhost:8086"
  val ordersUrl = (address :: "order" :: Nil).mkString("/")

  val xmlMediaType = "application/xml"
  val xmlTypes = Map("Content-Type" -> xmlMediaType, "Accept" -> xmlMediaType)
  val atomMediaType = "application/atom+xml"
  val atomTypes = Map("Content-Type" -> atomMediaType, "Accept" -> atomMediaType)

  implicit def toRichRequest(req: Request) = new RichRequest(req)

  class RichRequest(req: Request) {
    def OPTIONS = req.next {
      Request.mimic(new HttpOptions) _
    }
  }

  implicit def toRichHandlers(handlers: Handlers) = new RichHandlers(handlers)

  class RichHandlers(handlers: Handlers) {
    def >? = Handler(handlers.request, (code, res, ent) => code)
  }

  def postOrder = {
    val order =
      <order>
        <drink>Latte</drink>
      </order>
    val (headers, orderEntity) = Http(ordersUrl <:< xmlTypes << order.toString >+ {
      h => (h >:> pass, h <> pass)
    })
    (headers, orderEntity, headers("Location").head)
  }

}