package no.arktekk.coffeetrader

import org.joda.time.DateTime
import xml.Elem

case class Entity(elem: Elem, created: DateTime = new DateTime)

trait Entities {
  var entities = Map[Long, Entity]()

  def findAllEntries = entities

  def removeAll {
    entities = Map[Long, Entity]()
  }

  def apply(id: Long) = entities.get(id)

  def apply(entry: (Long, Elem)) {
    entities = entities + (entry._1 -> Entity(entry._2))
  }

}