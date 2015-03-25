package org.openurp.edu.eams.teach.grade.teacher.web.action

import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.model.GradeModifyApplyBean
import org.openurp.edu.eams.teach.grade.course.model.GradeModifyApplyBean.GradeModifyStatus
import org.openurp.edu.eams.teach.grade.course.service.GradeModifyApplyService
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class GradeModifyAuditSearchAction extends SemesterSupportAction {

  var gradeModifyApplyService: GradeModifyApplyService = _

  override def getEntityName(): String = classOf[GradeModifyApplyBean].getName

  protected override def indexSetting() {
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType]))
    put("examStatuses", baseCodeService.getCodes(classOf[ExamStatus]))
    put("statuses", GradeModifyStatus.values)
    put("GA_ID", GradeTypeConstants.GA_ID)
    put("FINAL_ID", GradeTypeConstants.FINAL_ID)
  }

  override def search(): String = {
    put("gradeModifyApplys", search(getQueryBuilder))
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(getEntityName, "gradeModifyApply")
    populateConditions(builder)
    val applyStatus = get("applyStatus")
    if (Strings.isNotEmpty(applyStatus)) {
      builder.where("gradeModifyApply.status = :status", GradeModifyStatus.valueOf(applyStatus))
    }
    builder.where("exists(from org.openurp.edu.teach.grade.CourseGrade cg " + 
      "where cg.project = gradeModifyApply.project " + 
      "and cg.semester = gradeModifyApply.semester " + 
      "and cg.course = gradeModifyApply.course " + 
      "and cg.lesson.teachDepart in (:departs))", getDeparts)
    builder.where("gradeModifyApply.semester = :semester", putSemester(null))
    builder.where("gradeModifyApply.project = :project", getProject)
    builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    builder
  }
}
