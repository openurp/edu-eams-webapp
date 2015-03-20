package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable



import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson




@SerialVersionUID(8922874191136161897L)
class ElectCoursePlan extends Serializable {

  val courseIds = CollectUtils.newHashMap()

  val groups = CollectUtils.newHashMap()

  
  val tops = CollectUtils.newArrayList()

  def getCourseIds(): Map[Long, Integer] = courseIds

  def addGroup(group: ElectCourseGroup) {
    groups.put(group.getCourseType.id, group)
    if (null == group.getParent) tops.add(group)
  }

  def getGroups(): Map[Integer, ElectCourseGroup] = groups

  def getOrCreateGroup(course: Course, defaultType: CourseType): ElectCourseGroup = {
    var courseTypeId = getCourseIds.get(course.id)
    if (null == courseTypeId) courseTypeId = defaultType.id
    var group = getGroups.get(courseTypeId)
    if (null == group) {
      group = new ElectCourseGroup(new CourseType(courseTypeId))
      getGroups.put(courseTypeId, group)
    }
    group
  }

  def getGroup(course: Course, defaultType: CourseType): ElectCourseGroup = {
    var courseTypeId = getCourseIds.get(course.id)
    if (null == courseTypeId) courseTypeId = defaultType.id
    getGroups.get(courseTypeId)
  }

  def isOverMaxCredit(lesson: Lesson): Boolean = {
    val course = lesson.getCourse
    var courseTypeId = courseIds.get(course.id)
    if (null == courseTypeId) courseTypeId = lesson.getCourseType.id
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
