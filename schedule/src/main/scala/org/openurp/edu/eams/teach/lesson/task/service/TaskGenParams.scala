package org.openurp.edu.eams.teach.lesson.task.service

import java.util.HashSet
import java.util.Set
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.code.school.ClassroomType
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(7046399461690215375L)
class TaskGenParams extends LongIdObject {

  @BeanProperty
  var semester: Semester = _

  @BeanProperty
  var courseUnits: Int = _

  @BeanProperty
  var weeks: Int = _

  @BeanProperty
  var startWeek: Int = _

  @BooleanBeanProperty
  var removeGenerated: Boolean = _

  @BooleanBeanProperty
  var allowNoAdminclass: Boolean = _

  @BooleanBeanProperty
  var omitSmallTerm: Boolean = true

  @BeanProperty
  var onlyGenCourseTypes: Set[CourseType] = new HashSet[CourseType]()

  @BeanProperty
  var dontGenCourses: Set[Course] = new HashSet[Course]()

  @BeanProperty
  var onlyGenCourses: Set[Course] = new HashSet[Course]()

  @BooleanBeanProperty
  var ignoreCloseRequest: Boolean = false

  private var classroomType: ClassroomType = _

  def getRoomType(): ClassroomType = classroomType

  def setRoomType(classroomType: ClassroomType) {
    this.classroomType = classroomType
  }
}
