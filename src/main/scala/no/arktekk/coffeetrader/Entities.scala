package no.arktekk.coffeetrader

import xml.Elem

trait Entities {
  var entities = Map[Long, Elem]()

  def findAllEntries = entities

  def removeAll {
    entities = Map[Long, Elem]()
  }

  def apply(id: Long) = entities.get(id)

  def apply(entry: (Long, Elem)) {
    entities = entities + entry
  }

}