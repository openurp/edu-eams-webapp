package org.openurp.edu.eams.teach.grade.service

import org.openurp.edu.base.Student
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType

import scala.collection.JavaConversions._

trait GradeCourseTypeProvider {

  def getCourseType(std: Student, course: Course, defaultCourseType: CourseType): CourseType
}
