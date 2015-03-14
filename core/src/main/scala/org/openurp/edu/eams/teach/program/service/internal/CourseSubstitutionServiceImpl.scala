package org.openurp.edu.eams.teach.program.service.internal

import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.teach.plan.CourseSubstitution
import org.openurp.edu.teach.plan.MajorCourseSubstitution
import org.openurp.edu.teach.plan.StdCourseSubstitution
import org.openurp.edu.eams.teach.program.service.CourseSubstitutionService

import scala.collection.JavaConversions._

class CourseSubstitutionServiceImpl extends BaseServiceImpl with CourseSubstitutionService {

  def getCourseSubstitutions(student: Student): List[CourseSubstitution] = {
    val substituteList = getStdCourseSubstitutions(student)
    substituteList.addAll(getMajorCourseSubstitutions(student))
    substituteList
  }

  def getStdCourseSubstitutions(student: Student): List[StdCourseSubstitution] = {
    val query = OqlBuilder.from(classOf[StdCourseSubstitution], "substitution")
    query.where("substitution.std=:std", student)
    entityDao.search(query)
  }

  def getMajorCourseSubstitutions(student: Student): List[MajorCourseSubstitution] = {
    val query = OqlBuilder.from(classOf[MajorCourseSubstitution], "substitution")
    query.where("substitution.grades like :grade", "%" + student.grade + "%")
    query.where("substitution.project = :project", student.getProject)
    query.where("substitution.education = :education", student.education)
    query.where("substitution.stdType is null or substitution.stdType = :stdType", student.getType)
    if (null == student.major) {
      query.where("substitution.major is null")
    } else {
      query.where("substitution.major is null or substitution.major = :major", student.major)
    }
    if (null == student.direction) {
      query.where("substitution.direction is null")
    } else {
      query.where("substitution.direction is null or substitution.direction = :direction", student.direction)
    }
    query.cacheable()
    entityDao.search(query)
  }
}
