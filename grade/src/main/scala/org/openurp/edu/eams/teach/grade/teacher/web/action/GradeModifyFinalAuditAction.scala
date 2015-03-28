package org.openurp.edu.eams.teach.grade.teacher.web.action

import java.util.Date


import org.beangle.commons.collection.Collections
import org.beangle.security.blueprint.User
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.eams.teach.grade.course.GradeModifyApply
import org.openurp.edu.eams.teach.grade.course.model.GradeModifyApplyBean.GradeModifyStatus
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.event.CourseGradeModifyEvent
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.ExamGrade



class GradeModifyFinalAuditAction extends GradeModifyAuditSearchAction {

  var calculator: CourseGradeCalculator = _

  var courseGradeService: CourseGradeService = _

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
    val courseGrades = Collections.newMap[Any]
    val statuses = Collections.newBuffer[Any]
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
        val courseGradeId = courseGrade.id
        if (courseGrades.keySet.contains(courseGradeId)) {
          courseGrade = courseGrades.get(courseGradeId)
        } else {
          courseGrades.put(courseGradeId, courseGrade)
        }
        val examGrade = courseGrade.getExamGrade(gradeModifyApply.gradeType)
        val state = courseGradeService.getState(courseGrade.getLesson)
        examGrade.setExamStatus(gradeModifyApply.getExamStatus)
        if (examGrade.getExamStatus != null && ExamStatus.NORMAL == examGrade.getExamStatus.id) {
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
    publish(new CourseGradeModifyEvent(Collections.newBuffer[Any](courseGrades.values)))
    redirect("search", "info.save.success", get("params"))
  }
}
