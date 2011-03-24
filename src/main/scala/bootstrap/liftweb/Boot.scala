package bootstrap.liftweb

import net.liftweb.common.{Logger, Full}
import net.liftweb.http.LiftRules
import net.liftweb.util.LoggingAutoConfigurer
import no.arktekk.coffeetrader.{PaymentResource, OrderResource}

class Boot {
  Logger.setup = Full(LoggingAutoConfigurer())
  LiftRules.dispatch.append(OrderResource)
  LiftRules.dispatch.append(PaymentResource)
}