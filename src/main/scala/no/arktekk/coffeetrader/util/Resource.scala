package no.arktekk.coffeetrader.util

import net.liftweb.http.Req

object Resource {
  val xmlMediaType = "application/xml"
  val atomMediaType = "application/atom+xml"

  def pathify(req: Req, pathParts: String*) = (req.hostAndPath :: pathParts.toList).mkString("/")

}