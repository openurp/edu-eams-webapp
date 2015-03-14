package org.openurp.edu.eams.teach.service.wrapper

import java.util.List
import org.openurp.base.CourseUnit
import org.openurp.edu.eams.base.util.WeekDay
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class TimeZone {

  @BeanProperty
  var weekStates: Array[String] = _

  @BeanProperty
  var units: List[CourseUnit] = _

  @BeanProperty
  var weeks: List[WeekDay] = _
}
