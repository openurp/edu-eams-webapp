package org.openurp.edu.eams.teach.grade.service.impl

import java.util.List
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.eams.teach.grade.service.GpaService
import org.openurp.edu.teach.grade.CourseGrade
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class DefaultGpaService extends GpaService {

  @BeanProperty
  var gpaPolicy: GpaPolicy = new DefaultGpaPolicy()

  private var courseGradeProvider: CourseGradeProvider = _

  def getGpa(std: Student): java.lang.Float = {
    gpaPolicy.calcGpa(courseGradeProvider.getPublished(std))
  }

  def getGpa(std: Student, grades: List[CourseGrade]): java.lang.Float = gpaPolicy.calcGpa(grades)

  def getGpa(std: Student, semester: Semester): java.lang.Float = {
    gpaPolicy.calcGpa(courseGradeProvider.getPublished(std, semester))
  }

  def setCourseGradeProvider(provider: CourseGradeProvider) {
    this.courseGradeProvider = provider
  }
}
