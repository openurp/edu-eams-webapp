package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable
import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.metadata.Model
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(6392274306070105614L)
class ElectCourseGroup(@BeanProperty val courseType: CourseType) extends Serializable() {

  @BeanProperty
  var parent: ElectCourseGroup = _

  @BeanProperty
  var requireCredits: Float = _

  @BeanProperty
  var completeCredits: Float = _

  @BeanProperty
  var limitCredits: Float = _

  @BeanProperty
  var electCredits: Float = _

  @BeanProperty
  var takedCredits: Float = _

  @BeanProperty
  val courses = CollectUtils.newHashSet()

  @BeanProperty
  var children: List[ElectCourseGroup] = CollectUtils.newArrayList()

  @BooleanBeanProperty
  var hasLesson: Boolean = false

  def addCourse(course: Course) {
    val _course = Model.newInstance(classOf[Course], course.getId)
    _course.setId(course.getId)
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
