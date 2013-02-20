package no.arktekk.coffeetrader

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.Req
import util.MatchLong

object Resources extends RestHelper {

  /*
  serve {
    case req @ Req("order" :: Nil, _, _) =>
      ListResource(req) (
      (xmlMediaType, Get) -> ...
      (atomMediaType, Get) ->
      )
    case req @ Req("order" :: MatchLong(id), _, _) =>
      EntityResource(req, id)
  } */

  // TODO: Media type principles
  //   Expected media type treated prioritised
  //   First match is used
  // TODO: Pluggable behaviour per (media type, method)
  //   Expires implemented through trait?
}