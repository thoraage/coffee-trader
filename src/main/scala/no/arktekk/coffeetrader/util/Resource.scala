package no.arktekk.coffeetrader.util

import net.liftweb.http.Req

object Resource {
  val mediaType = "application/xml"

  def pathify(req: Req, pathParts: String*) = (req.hostAndPath :: pathParts.toList).mkString("/")

}