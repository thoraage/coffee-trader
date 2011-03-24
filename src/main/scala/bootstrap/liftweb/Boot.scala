package bootstrap.liftweb

import net.liftweb.common.{Logger, Full}
import net.liftweb.util.LoggingAutoConfigurer
import no.arktekk.coffeetrader.{PaymentResource, OrderResource}
import net.liftweb.http.auth.{userRoles, HttpBasicAuthentication, AuthRole}
import net.liftweb.http.{Req, LiftRules}

class Boot {
  Logger.setup = Full(LoggingAutoConfigurer())

  val systemRole = AuthRole("system")
  val (webshopUser, webshopPwd) = ("joe", "doe")
  LiftRules.authentication = HttpBasicAuthentication("lift") {
    case (`webshopUser`, `webshopPwd`, _) =>
      userRoles(systemRole :: Nil)
      true

  }
  LiftRules.httpAuthProtectedResource.append{
    case Req("payment" :: _, _, _) => Full(systemRole)
  }

  LiftRules.dispatch.append(OrderResource)
  LiftRules.dispatch.append(PaymentResource)
}