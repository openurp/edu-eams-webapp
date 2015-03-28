package org.openurp.edu.eams.teach.program.major.web.action

import java.util.Date


import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.security.blueprint.User
import com.ekingstar.eams.core.CommonAuditState
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.helper.ProgramCollector
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.MajorPlanComment
import org.openurp.edu.eams.teach.program.major.guard.MajorProgramOperateGuard
import org.openurp.edu.eams.teach.program.major.guard.MajorProgramOperateType
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCommentBean
import org.openurp.edu.eams.teach.program.major.service.MajorPlanAuditService
//remove if not needed


class MajorPlanAuditAction extends MajorPlanSearchAction {

  var majorPlanAuditService: MajorPlanAuditService = _

  var majorProgramBasicGuard: MajorProgramOperateGuard = _

  def index(): String = {
    setDataRealm(hasStdTypeCollege)
    put("educations", getEducations)
    put("stateList", CommonAuditState.values)
    put("SUBMITTED", CommonAuditState.SUBMITTED)
    put("ACCEPTED", CommonAuditState.ACCEPTED)
    put("REJECTED", CommonAuditState.REJECTED)
    forward()
  }

  override def info(): String = {
    super.info()
    put("SUBMITTED", CommonAuditState.SUBMITTED)
    put("ACCEPTED", CommonAuditState.ACCEPTED)
    put("REJECTED", CommonAuditState.REJECTED)
    forward()
  }

  override def search(): String = {
    if (Collections.isEmpty(getProjects) || Collections.isEmpty(getDeparts) || 
      Collections.isEmpty(getStdTypes)) {
      return forwardError("对不起，您没有权限！")
    }
    val query = majorPlanSearchHelper.buildPlanQuery()
    query.where("plan.program.major.project in (:projects)", getProjects)
      .where("plan.program.department in (:departs)", getDeparts)
      .where("plan.program.stdType in (:stdTypes)", getStdTypes)
    if (Collections.isNotEmpty(getEducations)) {
      query.where("plan.program.education in (:educations)", getEducations)
    }
    val plans = entityDao.search(query)
    put("plans", plans)
    put("SUBMITTED", CommonAuditState.SUBMITTED)
    put("ACCEPTED", CommonAuditState.ACCEPTED)
    forward()
  }

  def readyAddReturnReason(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("error.model.ids.needed")
    }
    val stateStr = get("auditState")
    if (null == stateStr) {
      return forwardError("error.parameters.needed")
    }
    put("planId", planIds(0))
    put("auditState", stateStr)
    forward()
  }

  def planReturnReasonList(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("error.model.ids.needed")
    }
    val query = OqlBuilder.from(classOf[MajorPlanComment], "re")
    query.where("re.majorPlan.id =" + planIds(0))
    query.limit(getPageLimit)
    val planReturnReasons = entityDao.search(query)
    put("planReturnReasons", planReturnReasons)
    forward()
  }

  def audit(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("error.model.ids.needed")
    }
    val stateStr = get("auditState")
    if (null == stateStr) {
      return forwardError("error.parameters.needed")
    }
    val state = CommonAuditState.valueOf(stateStr.toUpperCase())
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    guard(MajorProgramOperateType.AUDIT, plans)
    majorPlanAuditService.audit(plans, state)
    if (stateStr == "REJECTED") {
      val reason = new MajorPlanCommentBean()
      reason.setReason(get("reason"))
      reason.setMajorPlan(plans.get(0).asInstanceOf[MajorPlan])
      val date = new Date()
      reason.setCreatedAt(date)
      reason.setUpdatedAt(date)
      entityDao.saveOrUpdate(reason)
    }
    redirect("search", "info.save.success")
  }

  def revokedAudit(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("error.model.ids.needed")
    }
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    guard(MajorProgramOperateType.AUDIT, plans)
    majorPlanAuditService.revokeAccepted(plans)
    redirect("search", "info.save.success")
  }

  private def fillDataRealmContext(context: Map[String, Any]) {
    context.put("realm/checkMe", true)
    context.put("realm/user", entityDao.get(classOf[User], getUserId))
    context.put("realm/project", getProject)
    context.put("realm/stdTypes", getStdTypes)
    context.put("realm/departs", getDeparts)
    context.put("realm/educations", getEducations)
  }

  private def guard(operType: MajorProgramOperateType, plans: List[MajorPlan]) {
    val context = Collections.newMap[Any]
    fillDataRealmContext(context)
    val programs = Collections.collect(plans, ProgramCollector.INSTANCE).asInstanceOf[List[_]]
    majorProgramBasicGuard.guard(operType, programs, context)
  }

  private def guard(operType: MajorProgramOperateType, plan: MajorPlan) {
    val context = Collections.newMap[Any]
    fillDataRealmContext(context)
    majorProgramBasicGuard.guard(operType, plan.getProgram, context)
  }
}
