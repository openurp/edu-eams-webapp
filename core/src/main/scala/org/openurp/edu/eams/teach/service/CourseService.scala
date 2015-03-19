package org.openurp.edu.eams.teach.service


import org.openurp.edu.base.Course
import org.openurp.edu.base.CourseExtInfo



trait CourseService {

  def getCourseByIdDwr(id: java.lang.Long): Course

  def getCourse(code: String): Course

  def getCourseExtInfo(courseId: java.lang.Long): CourseExtInfo

  def saveOrUpdate(course: Course): Unit

  def saveOrUpdate(course: Course, extInfo: CourseExtInfo): Unit

  def searchCoursesByCodeOrName(codeOrName: String): List[Course]

  def searchCourseByProjectAndCodeOrName(studentCode: String, codeOrName: String, projectId: String): List[Course]
}
