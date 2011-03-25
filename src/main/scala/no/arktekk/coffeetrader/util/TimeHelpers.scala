package no.arktekk.coffeetrader.util

import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import java.text.SimpleDateFormat

object TimeHelpers {

  lazy val isoTimeFormat = ISODateTimeFormat.dateTimeNoMillis

  def expiresTimeFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z")

}