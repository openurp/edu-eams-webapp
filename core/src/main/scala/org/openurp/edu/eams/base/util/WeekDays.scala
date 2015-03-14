package org.openurp.edu.eams.base.util


import scala.collection.JavaConversions._

object WeekDays {

  val MAX = 7

  val Sunday = new WeekDay(new java.lang.Integer(7), "星期日", "Sun")

  var All: Array[WeekDay] = Array(new WeekDay(new java.lang.Integer(1), "星期一", "Mon"), new WeekDay(new java.lang.Integer(2), 
    "星期二", "Tue"), new WeekDay(new java.lang.Integer(3), "星期三", "Wed"), new WeekDay(new java.lang.Integer(4), 
    "星期四", "Thur"), new WeekDay(new java.lang.Integer(5), "星期五", "Fri"), new WeekDay(new java.lang.Integer(6), 
    "星期六", "Sat"), Sunday)

  def get(weekId: Int): WeekDay = All(weekId - 1)
}
