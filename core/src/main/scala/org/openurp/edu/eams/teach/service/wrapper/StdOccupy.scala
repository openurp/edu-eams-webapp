package org.openurp.edu.eams.teach.service.wrapper

import org.openurp.edu.base.Student
import org.openurp.edu.teach.Course

import scala.collection.JavaConversions._

class StdOccupy {

  var std: Student = _

  var course: Course = _

  var remark: String = _

  def getStd(): Student = std

  def setStd(std: Student) {
    this.std = std
  }

  def getCourse(): Course = course

  def setCourse(course: Course) {
    this.course = course
  }

  def getRemark(): String = remark

  def setRemark(remark: String) {
    this.remark = remark
  }
}
