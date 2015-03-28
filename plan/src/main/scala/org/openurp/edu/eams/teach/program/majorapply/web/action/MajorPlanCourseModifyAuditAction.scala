package org.openurp.edu.eams.teach.program.majorapply.web.action

import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.core.Project
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseModifyAuditService
//remove if not needed


class MajorPlanCourseModifyAuditAction extends MajorPlanCourseModifyApplyAction {

  var majorPlanCourseModifyAuditService: MajorPlanCourseModifyAuditService = _

  def approved(): String = {
    val applyId = getLong("applyId")
    if (null == applyId) {
      return forwardError("缺少参数")
    }
    val apply = entityDao.get(classOf[MajorPlanCourseModifyBean], applyId)
    try {
      majorPlanCourseModifyAuditService.approved(apply, entityDao.get(classOf[User], getUserId))
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
    val apply = entityDao.get(classOf[MajorPlanCourseModifyBean], applyId)
    try {
      majorPlanCourseModifyAuditService.rejected(apply, entityDao.get(classOf[User], getUserId))
    } catch {
      case e: MajorPlanAuditException => return forwardError(e.getMessage)
    }
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
    forward()
  }

  def applications(): String = {
    if (Collections.isEmpty(getProjects) || Collections.isEmpty(getDeparts) || 
      Collections.isEmpty(getStdTypes)) {
      return forwardError("对不起，您没有权限！")
    }
    val project = getProject
    val query = getQueryBuilder
    query.where("exists (select plan.id from org.openurp.edu.eams.teach.program.major.MajorPlan plan" + 
      " where plan.id=apply.majorPlan.id" + 
      " and plan.program.major.project = :project" + 
      " and plan.program.department in (:departs)" + 
      " and plan.program.stdType in (:stdTypes)" + 
      ")", project, getDeparts, getStdTypes)
    put("applies", entityDao.search(query))
    put("param", get("param"))
    forward()
  }
}
