package org.openurp.edu.eams.teach.grade.course.service

import org.openurp.edu.eams.teach.grade.course.GradeModifyApply
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.ExamGrade



trait GradeModifyApplyService {

  def getCourseGrade(apply: GradeModifyApply): CourseGrade

  def getExamGrade(apply: GradeModifyApply): ExamGrade
}
