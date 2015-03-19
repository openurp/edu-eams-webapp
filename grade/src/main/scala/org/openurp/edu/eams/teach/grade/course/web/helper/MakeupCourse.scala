package org.openurp.edu.eams.teach.grade.course.web.helper

import org.openurp.base.Department
import org.openurp.edu.base.Course



class MakeupCourse(val depart: Department, val course: Course, val count: Number)
    {

  val id = course.id + "@" + depart.id

  var status: Int = _

  def getStatus(): Int = status

  def setStatus(status: Int) {
    this.status = status
  }

  def getCourse(): Course = course

  def getCount(): Number = count

  def getDepart(): Department = depart

  def id(): String = id
}
