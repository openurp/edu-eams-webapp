package org.openurp.edu.eams.teach.program.majorapply.web.action

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseGroupModifyAuditService
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupModifyAuditAction extends MajorPlanCourseGroupModifyApplyAction {

  private var majorPlanCourseGroupModifyAuditService: MajorPlanCourseGroupModifyAuditService = _

  def approved(): String = {
    val applyId = getLong("applyId")
    if (null == applyId) {
      return forwardError("缺少参数")
    }
    val apply = entityDao.get(classOf[MajorPlanCourseGroupModifyBean], applyId)
    try {
      majorPlanCourseGroupModifyAuditService.approved(apply, entityDao.get(classOf[User], getUserId))
    } catch {
      case e: MajorPlanAuditException => return forwardError(e.getMessage)
    }
    getFlash.put("params", get("params"))
    getFlash.put("backUrl", get("backUrl"))
    if (getBool("from_of_plan")) {
      return redirect(new Action(classOf[MajorPlanModifyAuditAction], "applicationsOfPlan", "&planId=" + get("planId") + "&tab_n=" + get("tab_n")), 
        "info.success.applyIsPass")
    }
    redirect("applications", "info.success.applyIsPass", get("params"))
  }

  def rejected(): String = {
    val applyId = getLong("applyId")
    if (null == applyId) {
      return forwardError("缺少参数")
    }
    val apply = entityDao.get(classOf[MajorPlanCourseGroupModifyBean], applyId)
    majorPlanCourseGroupModifyAuditService.rejected(apply, entityDao.get(classOf[User], getUserId))
    getFlash.put("params", get("params"))
    getFlash.put("backUrl", get("backUrl"))
    if (getBool("from_of_plan")) {
      return redirect(new Action(classOf[MajorPlanModifyAuditAction], "applicationsOfPlan", "&planId=" + get("planId") + "&tab_n=" + get("tab_n")), 
        "info.success.applyIsBack")
    }
    redirect("applications", "info.success.applyIsBack", get("params"))
  }

  def applicationSearch(): String = {
    put("departs", getDeparts)
    put("proposerId", getUserId)
    forward()
  }

  def applications(): String = {
    if (CollectUtils.isEmpty(getProjects) || CollectUtils.isEmpty(getDeparts) || 
      CollectUtils.isEmpty(getStdTypes)) {
      return forwardError("对不起，您没有权限！")
    }
    val query = getQueryBuilder
    query.where("exists (select plan.id from org.openurp.edu.eams.teach.program.major.MajorPlan plan" + 
      " where plan.id = apply.majorPlan.id" + 
      " and plan.program.major.project.id = :projectId" + 
      " and plan.program.department in (:departs)" + 
      " and plan.program.stdType in (:stdTypes)" + 
      ")", getProject.getId, getDeparts, getStdTypes)
    put("applications", entityDao.search(query))
    put("param", get("param"))
    forward()
  }

  def setMajorPlanCourseGroupModifyAuditService(majorPlanCourseGroupModifyAuditService: MajorPlanCourseGroupModifyAuditService) {
    this.majorPlanCourseGroupModifyAuditService = majorPlanCourseGroupModifyAuditService
  }
}
