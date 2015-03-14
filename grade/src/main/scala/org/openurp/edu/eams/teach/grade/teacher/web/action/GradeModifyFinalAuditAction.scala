package org.openurp.edu.eams.teach.grade.teacher.web.action

import java.util.Date
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.code.industry.ExamStatus
import org.openurp.edu.eams.teach.grade.course.GradeModifyApply
import org.openurp.edu.eams.teach.grade.course.model.GradeModifyApplyBean.GradeModifyStatus
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.event.CourseGradeModifyEvent
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGrade

import scala.collection.JavaConversions._

class GradeModifyFinalAuditAction extends GradeModifyAuditSearchAction {

  private var calculator: CourseGradeCalculator = _

  private var courseGradeService: CourseGradeService = _

  override def search(): String = {
    put("gradeModifyApplys", search(getQueryBuilder))
    put("status", GradeModifyStatus.valueOf(get("applyStatus")))
    forward()
  }

  def updateStatus(): String = {
    val applies = getModels(classOf[GradeModifyApply], getLongIds("gradeModifyApply"))
    val passed = getBool("passed")
    val date = new Date()
    val user = entityDao.get(classOf[User], getUserId)
    val courseGrades = CollectUtils.newHashMap()
    val statuses = CollectUtils.newArrayList()
    statuses.add(GradeModifyStatus.NOT_AUDIT)
    statuses.add(GradeModifyStatus.DEPART_AUDIT_PASSED)
    statuses.add(GradeModifyStatus.DEPART_AUDIT_UNPASSED)
    statuses.add(GradeModifyStatus.GRADE_DELETED)
    statuses.add(GradeModifyStatus.ADMIN_AUDIT_UNPASSED)
    for (gradeModifyApply <- applies) {
      if (statuses.contains(gradeModifyApply.getStatus)) {
        //continue
      }
      gradeModifyApply.setFinalAuditer(user.getFullname + "(" + user.getName + ")")
      gradeModifyApply.setUpdatedAt(date)
      gradeModifyApply.setStatus(if (passed) GradeModifyStatus.FINAL_AUDIT_PASSED else GradeModifyStatus.FINAL_AUDIT_UNPASSED)
      if (passed) {
        var courseGrade = gradeModifyApplyService.getCourseGrade(gradeModifyApply)
        if (null == courseGrade) {
          gradeModifyApply.setStatus(GradeModifyStatus.GRADE_DELETED)
          //continue
        }
        val courseGradeId = courseGrade.getId
        if (courseGrades.keySet.contains(courseGradeId)) {
          courseGrade = courseGrades.get(courseGradeId)
        } else {
          courseGrades.put(courseGradeId, courseGrade)
        }
        val examGrade = courseGrade.getExamGrade(gradeModifyApply.gradeType)
        val state = courseGradeService.getState(courseGrade.getLesson)
        examGrade.setExamStatus(gradeModifyApply.getExamStatus)
        if (examGrade.getExamStatus != null && ExamStatus.NORMAL == examGrade.getExamStatus.getId) {
          examGrade.setScore(gradeModifyApply.getScore)
        }
        examGrade.setOperator(user.getName)
        examGrade.setUpdatedAt(date)
        courseGrade.setOperator(user.getName)
        courseGrade.setUpdatedAt(date)
        calculator.calc(courseGrade, state)
      }
    }
    try {
      entityDao.saveOrUpdate(applies, courseGrades.values)
    } catch {
      case e: Exception => return redirect("search", "info.save.failure", get("params"))
    }
    publish(new CourseGradeModifyEvent(CollectUtils.newArrayList(courseGrades.values)))
    redirect("search", "info.save.success", get("params"))
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }

  def setCourseGradeCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }
}
