package org.openurp.edu.eams.teach.program.majorapply.web.action

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.core.Major
import com.ekingstar.eams.core.code.industry.Education
import com.ekingstar.eams.teach.code.school.CourseCategory
import org.openurp.edu.eams.teach.program.major.web.action.MajorPlanSearchAction
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorCourseGroupModifyApplyService
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseModifyApplyService
//remove if not needed


class MajorPlanModifyAuditAction extends MajorPlanSearchAction {

  private var majorPlanCourseModifyApplyService: MajorPlanCourseModifyApplyService = _

  private var MajorCourseGroupModifyApplyService: MajorCourseGroupModifyApplyService = _

  def index(): String = {
    indexSetting()
    forward()
  }

  override def search(): String = {
    super.search()
    put("ACCEPTED", CommonAuditState.ACCEPTED)
    forward()
  }

  protected override def indexSetting() {
    put("stateList", CommonAuditState.values)
    put("courseCategorys", baseCodeService.getCodes(classOf[CourseCategory]))
    put("majors", baseInfoService.getBaseInfos(classOf[Major]))
    put("educations", baseCodeService.getCodes(classOf[Education]))
    put("auditLimitDescription", "只能对『未提交审核』和『审核不通过』的计划提交审核")
    put("ACCEPTED", CommonAuditState.ACCEPTED)
  }

  def applicationsOfPlan(): String = {
    val planId = getLong("planId")
    val query1 = OqlBuilder.from(classOf[MajorPlanCourseModifyBean], "apply")
    query1.where("apply.majorPlan.id = :planId", planId)
      .orderBy("apply.flag")
      .orderBy(Order.asc("apply.applyDate"))
      .limit(null)
    put("planCourseModifyApplications", entityDao.search(query1))
    val query2 = OqlBuilder.from(classOf[MajorCourseGroupModifyBean], "apply")
    query2.where("apply.majorPlan.id = :planId", planId)
      .orderBy("apply.flag")
      .orderBy(Order.asc("apply.applyDate"))
      .limit(null)
    put("courseGroupModifyApplications", entityDao.search(query2))
    forward()
  }

  def applicationSearch(): String = {
    put("departs", getDeparts)
    forward()
  }

  def setMajorPlanCourseModifyApplyService(majorPlanCourseModifyApplyService: MajorPlanCourseModifyApplyService) {
    this.majorPlanCourseModifyApplyService = majorPlanCourseModifyApplyService
  }

  def setMajorCourseGroupModifyApplyService(MajorCourseGroupModifyApplyService: MajorCourseGroupModifyApplyService) {
    this.MajorCourseGroupModifyApplyService = MajorCourseGroupModifyApplyService
  }
}
