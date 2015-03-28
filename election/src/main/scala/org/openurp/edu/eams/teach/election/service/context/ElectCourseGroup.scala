package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable


import org.beangle.commons.collection.Collections
import org.beangle.commons.entity.metadata.Model
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType




@SerialVersionUID(6392274306070105614L)
class ElectCourseGroup( val courseType: CourseType) extends Serializable() {

  
  var parent: ElectCourseGroup = _

  
  var requireCredits: Float = _

  
  var completeCredits: Float = _

  
  var limitCredits: Float = _

  
  var electCredits: Float = _

  
  var takedCredits: Float = _

  
  val courses = Collections.newSet[Any]

  
  var children: List[ElectCourseGroup] = Collections.newBuffer[Any]

  
  var hasLesson: Boolean = false

  def addCourse(course: Course) {
    val _course = Model.newInstance(classOf[Course], course.id)
    _course.setId(course.id)
    _course.setCode(course.getCode)
    _course.setName(course.getName)
    _course.setEngName(course.getEngName)
    _course.setCredits(course.getCredits)
    courses.add(_course)
  }

  def addElectCourse(course: Course) {
    electCredits += course.getCredits
    if (null != parent) {
      parent.addElectCourse(course)
    }
  }

  def addTakedCourse(course: Course) {
    takedCredits += course.getCredits
    if (null != parent) {
      parent.addTakedCourse(course)
    }
  }

  def removeElectCourse(course: Course) {
    electCredits -= course.getCredits
    if (electCredits < 0) electCredits = 0
    if (null != parent) {
      parent.removeElectCourse(course)
    }
  }

  def setHasLesson(hasLesson: Boolean) {
    this.hasLesson = hasLesson
    if (null != parent && hasLesson) {
      parent.setHasLesson(true)
    }
  }

  def isOverMaxCredit(credits: Float): Boolean = {
    if (electCredits + credits > limitCredits) return true
    if (null != parent) return parent.isOverMaxCredit(credits)
    false
  }
}
