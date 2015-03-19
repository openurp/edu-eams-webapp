package org.openurp.edu.eams.teach.schedule.model

import org.openurp.base.Semester
import org.openurp.edu.base.Course




class TaskGroupCopyParam {

  
  var toSemester: Semester = _

  
  var copyTeacher: java.lang.Boolean = _

  
  var replaceCourse: Course = _

  def this(toSemester: Semester, copyTeacher: Boolean, replaceCourse: Course) {
    super()
    this.toSemester = toSemester
    this.copyTeacher = copyTeacher
    this.replaceCourse = replaceCourse
  }
}
