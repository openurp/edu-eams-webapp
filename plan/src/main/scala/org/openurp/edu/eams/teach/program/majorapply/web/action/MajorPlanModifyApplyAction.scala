package org.openurp.edu.eams.teach.program.majorapply.web.action

import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.core.Major
import com.ekingstar.eams.teach.code.school.CourseCategory
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.web.action.MajorPlanSearchAction
import org.openurp.edu.eams.teach.program.majorapply.service.MajorCourseGroupModifyApplyService
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseModifyApplyService
//remove if not needed


class MajorPlanModifyApplyAction extends MajorPlanSearchAction {

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
    put("educations", getEducations)
    put("auditLimitDescription", "只能对『未提交审核』和『审核不通过』的计划提交审核")
    put("ACCEPTED", CommonAuditState.ACCEPTED)
  }

  def apply(): String = {
    val planId = getLong("planId")
    val plan = entityDao.get(classOf[MajorPlan], planId)
    if (null == plan) {
      return forwardError("error.model.notExist")
    }
    put("plan", plan)
    put("readyBeAuditedCourseGroups", MajorCourseGroupModifyApplyService.myReadyModifyApply(planId, 
      getUserId))
    put("readyBeAuditedPlanCourses", majorPlanCourseModifyApplyService.myReadyModifyApply(planId, getUserId))
    put("readyGroupAddApplies", MajorCourseGroupModifyApplyService.myReadyAddApplies(planId, getUserId))
    put("readyCourseAddApplies", majorPlanCourseModifyApplyService.myReadyAddApplies(planId, getUserId))
    forward()
  }

  def applicationsOfPlan(): String = {
    val userId = getUserId
    val planId = getLong("planId")
    put("planCourseModifyApplications", majorPlanCourseModifyApplyService.myApplies(planId, userId))
    put("courseGroupModifyApplications", MajorCourseGroupModifyApplyService.myApplies(planId, userId))
    forward()
  }

  def myApplications(): String = {
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
