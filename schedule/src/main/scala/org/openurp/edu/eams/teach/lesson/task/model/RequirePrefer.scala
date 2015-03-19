package org.openurp.edu.eams.teach.lesson.task.model

import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.base.Teacher
import org.openurp.edu.base.Course




@SerialVersionUID(-7335843714667994840L)
@Deprecated
class RequirePrefer extends LongIdObject {

  
  var teacher: Teacher = _

  
  var course: Course = _

  def this(teacher: Teacher, course: Course) {
    this()
    this.teacher = teacher
    this.course = course
  }
}
