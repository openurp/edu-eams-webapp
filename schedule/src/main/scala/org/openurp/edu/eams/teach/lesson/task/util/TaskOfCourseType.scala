package org.openurp.edu.eams.teach.lesson.task.util

import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.code.CourseType




class TaskOfCourseType {

  
  var courseType: CourseType = _

  
  var adminclass: Adminclass = _

  
  var credits: java.lang.Float = _

  def this(courseType: CourseType, adminClass: Adminclass, credit: java.lang.Float) {
    this()
    this.courseType = courseType
    this.adminclass = adminClass
    this.credits = credit
  }

  override def toString(): String = {
    (if ((null == courseType)) "null" else courseType.getName) + 
      " " + 
      (if ((null == adminclass)) "null" else adminclass.getName)
  }
}
