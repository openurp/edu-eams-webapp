package org.openurp.edu.eams.teach.schedule.model

import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.teach.lesson.CourseTime
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class CollisionInfo {

  @BeanProperty
  var resource: AnyRef = _

  @BeanProperty
  var times: List[CourseTime] = CollectUtils.newArrayList()

  @BeanProperty
  var reason: String = _

  def this(resource: AnyRef, time: CourseTime) {
    this()
    this.resource = resource
    val newTime = new CourseTime(time)
    newTime.setWeekStateNum(0L)
    this.times.add(newTime)
  }

  def this(resource: AnyRef, time: CourseTime, reason: String) {
    super()
    this.resource = resource
    val newTime = new CourseTime(time)
    newTime.setWeekStateNum(0L)
    this.times.add(newTime)
    this.reason = reason
  }

  def add(time: CourseTime) {
    val newTime = new CourseTime(time)
    newTime.setWeekStateNum(0L)
    if (!times.contains(newTime)) {
      this.times.add(newTime)
    }
  }

  def mergeTimes() {
    setTimes(CourseTime.mergeTimeUnits(getTimes))
  }
}
