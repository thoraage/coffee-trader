package no.arktekk.coffeetrader.util

object MatchLong {
  def unapply(str: String): Option[Long] = try {
    Some(str.toLong)
  } catch {
    case _: NumberFormatException => None
  }
}
