package org.openurp.edu.eams.teach.grade.service.internal

import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.eams.teach.grade.service.impl.BestGradeFilter
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

class BestGradeCourseGradeProviderImpl extends BaseServiceImpl with CourseGradeProvider {

  private var bestGradeFilter: BestGradeFilter = _

  def getPublished(std: Student, semesters: Semester*): List[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.std = :std", std)
    query.where("grade.status =:status", Grade.Status.PUBLISHED)
    if (null != semesters && semesters.length > 0) {
      query.where("grade.semester in(:semesters)", semesters)
    }
    query.orderBy("grade.semester.beginOn")
    bestGradeFilter.filter(entityDao.search(query))
  }

  def getAll(std: Student, semesters: Semester*): List[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.std = :std", std)
    if (null != semesters && semesters.length > 0) {
      query.where("grade.semester in(:semesters)", semesters)
    }
    query.orderBy("grade.semester.beginOn")
    bestGradeFilter.filter(entityDao.search(query))
  }

  def getPublished(stds: Collection[Student], semesters: Semester*): Map[Student, List[CourseGrade]] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.std in (:stds)", stds)
    query.where("grade.status =:status", Grade.Status.PUBLISHED)
    if (null != semesters && semesters.length > 0) {
      query.where("grade.semester in(:semesters)", semesters)
    }
    val allGrades = entityDao.search(query)
    val gradeMap = CollectUtils.newHashMap()
    for (std <- stds) {
      gradeMap.put(std, new ArrayList[CourseGrade]())
    }
    for (g <- allGrades) gradeMap.get(g.getStd).add(g)
    for (std <- stds) {
      gradeMap.put(std, bestGradeFilter.filter(gradeMap.get(std)))
    }
    gradeMap
  }

  def getAll(stds: Collection[Student], semesters: Semester*): Map[Student, List[CourseGrade]] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.std in (:stds)", stds)
    if (null != semesters && semesters.length > 0) {
      query.where("grade.semester in(:semesters)", semesters)
    }
    val allGrades = entityDao.search(query)
    val gradeMap = CollectUtils.newHashMap()
    for (std <- stds) {
      gradeMap.put(std, new ArrayList[CourseGrade]())
    }
    for (g <- allGrades) gradeMap.get(g.getStd).add(g)
    for (std <- stds) {
      gradeMap.put(std, bestGradeFilter.filter(gradeMap.get(std)))
    }
    gradeMap
  }

  def getPassedStatus(std: Student): Map[Long, Boolean] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "cg")
    query.where("cg.std = :std", std)
    query.select("cg.course.id,cg.passed")
    val rs = entityDao.search(query).asInstanceOf[List[Array[Any]]]
    val courseMap = CollectUtils.newHashMap()
    for (obj <- rs) {
      val courseId = obj(0).asInstanceOf[java.lang.Long]
      if (null != obj(1)) {
        if (courseMap.containsKey(courseId) && true == courseMap.get(courseId)) {
          //continue
        } else {
          courseMap.put(courseId, obj(1).asInstanceOf[java.lang.Boolean])
        }
      } else {
        courseMap.put(courseId, false)
      }
    }
    courseMap
  }

  def setBestGradeFilter(bestGradeFilter: BestGradeFilter) {
    this.bestGradeFilter = bestGradeFilter
  }
}
