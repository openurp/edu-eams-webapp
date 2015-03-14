package org.openurp.edu.eams.teach.service

import java.util.List
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.CourseExtInfo

import scala.collection.JavaConversions._

trait CourseService {

  def getCourseByIdDwr(id: java.lang.Long): Course

  def getCourse(code: String): Course

  def getCourseExtInfo(courseId: java.lang.Long): CourseExtInfo

  def saveOrUpdate(course: Course): Unit

  def saveOrUpdate(course: Course, extInfo: CourseExtInfo): Unit

  def searchCoursesByCodeOrName(codeOrName: String): List[Course]

  def searchCourseByProjectAndCodeOrName(studentCode: String, codeOrName: String, projectId: String): List[Course]
}
