package org.openurp.edu.eams.weekstate

import org.openurp.edu.eams.date.EamsWeekday

import scala.collection.JavaConversions._

trait RelativeWeekStateBuilder {

  def build(weekIndecies: Array[Int], weekday: EamsWeekday): BasicWeekState

  def build(weekIndecies: Array[Integer], weekday: EamsWeekday): BasicWeekState

  def build(weekIndecies: String, weekday: EamsWeekday): BasicWeekState

  def parse(weekState: java.lang.Long): Array[Integer]

  def parse(weekState: String): Array[Integer]
}
