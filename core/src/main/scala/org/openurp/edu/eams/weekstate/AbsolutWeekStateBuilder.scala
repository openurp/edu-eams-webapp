package org.openurp.edu.eams.weekstate

import org.openurp.edu.eams.date.EamsWeekday

import scala.collection.JavaConversions._

trait AbsolutWeekStateBuilder {

  def build(weekIndecies: Array[Int], weekday: EamsWeekday): Array[BasicWeekState]

  def build(weekIndecies: Array[Integer], weekday: EamsWeekday): Array[BasicWeekState]

  def build(weekIndecies: String, weekday: EamsWeekday): Array[BasicWeekState]

  def parse(weekState: java.lang.Long): Array[Integer]

  def parse(weekState: String): Array[Integer]
}
