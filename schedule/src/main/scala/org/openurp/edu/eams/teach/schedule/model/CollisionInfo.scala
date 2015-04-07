package org.openurp.edu.eams.teach.schedule.model


import org.beangle.commons.collection.Collections


class CollisionInfo {

  
  var resource: AnyRef = _

  
  var times: Seq[CourseTime] = Collections.newBuffer[Any]

  
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
    setTimes(CourseTime.mergeYearWeekTimes(getTimes))
  }
}
