package org.openurp.edu.eams.teach.schedule.model

import org.openurp.edu.eams.base.Semester
import org.openurp.edu.teach.Course
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class TaskGroupCopyParam {

  @BeanProperty
  var toSemester: Semester = _

  @BeanProperty
  var copyTeacher: java.lang.Boolean = _

  @BeanProperty
  var replaceCourse: Course = _

  def this(toSemester: Semester, copyTeacher: Boolean, replaceCourse: Course) {
    super()
    this.toSemester = toSemester
    this.copyTeacher = copyTeacher
    this.replaceCourse = replaceCourse
  }
}
