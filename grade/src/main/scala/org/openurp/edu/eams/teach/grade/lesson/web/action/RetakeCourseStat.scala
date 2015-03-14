package org.openurp.edu.eams.teach.grade.lesson.web.action

import org.openurp.edu.teach.Course
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class RetakeCourseStat(@BeanProperty val course: Course, @BeanProperty val unpassed: Number)
    {

  @BeanProperty
  var freespace: Int = _

  private var newspace: Int = _

  def getNewspace(): Int = {
    if (this.newspace == 0) {
      this.newspace = if (unpassed.intValue() > freespace) unpassed.intValue() - freespace else 0
    }
    newspace
  }

  def setNewspace(newspace: Int) {
    this.newspace = newspace
  }
}
