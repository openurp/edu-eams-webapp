package org.openurp.edu.eams.teach.grade.course.service.impl


import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.eams.teach.grade.course.GradeModifyApply
import org.openurp.edu.eams.teach.grade.course.service.GradeModifyApplyService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.ExamGrade



class GradeModifyApplyServiceImpl extends BaseServiceImpl with GradeModifyApplyService {

  def getCourseGrade(apply: GradeModifyApply): CourseGrade = {
    val builder = OqlBuilder.from(classOf[CourseGrade], "grade")
    builder.where("grade.semester  = :semester", apply.getSemester)
    builder.where("grade.project  = :project", apply.getProject)
    builder.where("grade.std  = :std", apply.getStd)
    builder.where("grade.course  = :course", apply.getCourse)
    val grades = entityDao.search(builder)
    if (grades.isEmpty) null else grades.get(0)
  }

  def getExamGrade(apply: GradeModifyApply): ExamGrade = {
    val builder = OqlBuilder.from(classOf[ExamGrade], "grade")
    builder.where("grade.courseGrade.semester  = :semester", apply.getSemester)
    builder.where("grade.courseGrade.project  = :project", apply.getProject)
    builder.where("grade.courseGrade.std  = :std", apply.getStd)
    builder.where("grade.courseGrade.course  = :course", apply.getCourse)
    builder.where("grade.gradeType = :gradeType", apply.gradeType)
    val grades = entityDao.search(builder)
    if (grades.isEmpty) null else grades.get(0)
  }
}
