package org.openurp.edu.eams.teach.grade.course.web.helper

import org.openurp.base.Department
import org.openurp.edu.teach.Course

import scala.collection.JavaConversions._

class MakeupCourse(val depart: Department, val course: Course, val count: Number)
    {

  val id = course.getId + "@" + depart.getId

  var status: Int = _

  def getStatus(): Int = status

  def setStatus(status: Int) {
    this.status = status
  }

  def getCourse(): Course = course

  def getCount(): Number = count

  def getDepart(): Department = depart

  def getId(): String = id
}
