package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(8922874191136161897L)
class ElectCoursePlan extends Serializable {

  val courseIds = CollectUtils.newHashMap()

  val groups = CollectUtils.newHashMap()

  @BeanProperty
  val tops = CollectUtils.newArrayList()

  def getCourseIds(): Map[Long, Integer] = courseIds

  def addGroup(group: ElectCourseGroup) {
    groups.put(group.getCourseType.getId, group)
    if (null == group.getParent) tops.add(group)
  }

  def getGroups(): Map[Integer, ElectCourseGroup] = groups

  def getOrCreateGroup(course: Course, defaultType: CourseType): ElectCourseGroup = {
    var courseTypeId = getCourseIds.get(course.getId)
    if (null == courseTypeId) courseTypeId = defaultType.getId
    var group = getGroups.get(courseTypeId)
    if (null == group) {
      group = new ElectCourseGroup(new CourseType(courseTypeId))
      getGroups.put(courseTypeId, group)
    }
    group
  }

  def getGroup(course: Course, defaultType: CourseType): ElectCourseGroup = {
    var courseTypeId = getCourseIds.get(course.getId)
    if (null == courseTypeId) courseTypeId = defaultType.getId
    getGroups.get(courseTypeId)
  }

  def isOverMaxCredit(lesson: Lesson): Boolean = {
    val course = lesson.getCourse
    var courseTypeId = courseIds.get(course.getId)
    if (null == courseTypeId) courseTypeId = lesson.getCourseType.getId
    val group = groups.get(courseTypeId)
    if (null == group) {
      return true
    }
    group.isOverMaxCredit(course.getCredits)
  }

  def filter(courses: Set[Course]) {
    for ((key, value) <- groups) {
      value.getCourses.retainAll(courses)
    }
  }

  def getEmptyGroupCourseTypeIds(): Set[Integer] = {
    val courseTypeIds = CollectUtils.newHashSet()
    for ((key, value) <- groups if value.getCourses.isEmpty) courseTypeIds.add(key)
    courseTypeIds
  }
}
