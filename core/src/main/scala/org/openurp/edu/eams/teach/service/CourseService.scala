package org.openurp.edu.eams.teach.service


import org.openurp.edu.base.Course


trait CourseService {

  def getCourseByIdDwr(id: java.lang.Long): Course

  def getCourse(code: String): Course

  def saveOrUpdate(course: Course): Unit

  def searchCoursesByCodeOrName(codeOrName: String): List[Course]

  def searchCourseByProjectAndCodeOrName(studentCode: String, codeOrName: String, projectId: String): List[Course]
}
