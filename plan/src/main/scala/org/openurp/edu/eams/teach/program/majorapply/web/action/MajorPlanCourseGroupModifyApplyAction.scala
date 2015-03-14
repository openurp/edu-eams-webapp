package org.openurp.edu.eams.teach.program.majorapply.web.action

import java.io.UnsupportedEncodingException
import java.util.Collection
import java.util.List
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.exporter.CourseGroupModifyPropertyExtractor
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseGroupModifyApplyService
import com.ekingstar.eams.web.action.common.RestrictionSupportAction
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupModifyApplyAction extends RestrictionSupportAction {

  private var majorPlanService: MajorPlanService = _

  private var planCommonDao: PlanCommonDao = _

  private var majorPlanCourseGroupModifyApplyService: MajorPlanCourseGroupModifyApplyService = _

  def applyAdd(): String = {
    val plan = getMajorPlan
    val unusedCourseTypeList = baseCodeService.getCodes(classOf[CourseType])
    put("plan", plan)
    put("unusedCourseTypeList", unusedCourseTypeList)
    put("requisitionType", MajorPlanCourseGroupModifyBean.ADD)
    forward()
  }

  def saveAddApply(): String = {
    val plan = getMajorPlan
    saveApplyInfo()
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[MajorPlanModifyApplyAction], "apply", "planId=" + plan.getId), "info.save.success")
  }

  def applyRemove(): String = {
    val courseGroup = getMajorPlanCourseGroup
    val credits = Strings.split(courseGroup.getTermCredits)
    put("plan", courseGroup.getPlan)
    put("courseGroup", courseGroup)
    put("credits", credits)
    put("requisitionType", MajorPlanCourseGroupModifyBean.DELETE)
    forward()
  }

  def saveRemoveApply(): String = {
    val plan = getMajorPlan
    saveApplyInfo()
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[MajorPlanModifyApplyAction], "apply", "planId=" + plan.getId), "info.save.success")
  }

  def applyModify(): String = {
    val courseGroup = getMajorPlanCourseGroup
    val credits = Strings.split(courseGroup.getTermCredits)
    val unusedCourseTypeList = baseCodeService.getCodes(classOf[CourseType])
    put("plan", courseGroup.getPlan)
    put("courseGroup", courseGroup)
    put("credits", credits)
    put("unusedCourseTypeList", unusedCourseTypeList)
    put("requisitionType", MajorPlanCourseGroupModifyBean.MODIFY)
    forward()
  }

  def saveModifyApply(): String = {
    val plan = getMajorPlan
    saveApplyInfo()
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[MajorPlanModifyApplyAction], "apply", "planId=" + plan.getId), "info.save.success")
  }

  def remove(): String = {
    val applyId = getLong("applyId")
    val apply = entityDao.get(classOf[MajorPlanCourseGroupModifyBean], applyId)
    if (null == apply) {
      return forwardError("申请不存在")
    }
    if (apply.getProposer.getId != getUserId) {
      return forwardError("不能取消不是你的申请")
    }
    if (apply.getFlag.compareTo(MajorPlanCourseModifyBean.INITREQUEST) != 
      0) {
      return forwardError("不能取消已经审核过的申请")
    }
    entityDao.remove(apply)
    getFlash.put("params", get("params"))
    getFlash.put("backUrl", get("backUrl"))
    if (getBool("from_of_plan")) {
      return redirect(new Action(classOf[MajorPlanModifyApplyAction], "applicationsOfPlan", "&planId=" + get("planId") + "&tab_n=" + get("tab_n")), 
        "info.success.cancelApply")
    }
    redirect("myApplications", "info.success.cancelApply")
  }

  def info(): String = {
    val applyId = getLong("applyId")
    if (applyId == null) {
      return forwardError("没有找到该申请记录,error: apply id is not found!")
    }
    val apply = entityDao.get(classOf[MajorPlanCourseGroupModifyBean], applyId)
    put("majorPlan", entityDao.get(classOf[MajorPlan], apply.getMajorPlan.getId))
    put("apply", apply)
    if (null != apply.getOldPlanCourseGroup && null != apply.getOldPlanCourseGroup.getParent) {
      put("oldParent", entityDao.get(classOf[MajorPlanCourseGroup], apply.getOldPlanCourseGroup.getParent.getId))
    }
    if (null != apply.getNewPlanCourseGroup && null != apply.getNewPlanCourseGroup.getParent) {
      put("newParent", entityDao.get(classOf[MajorPlanCourseGroup], apply.getNewPlanCourseGroup.getParent.getId))
    }
    if (apply.getOldPlanCourseGroup != null) {
      put("oldCredits", Strings.split(apply.getOldPlanCourseGroup.getTermCredits))
    }
    if (apply.getNewPlanCourseGroup != null) {
      put("newCredits", Strings.split(apply.getNewPlanCourseGroup.getTermCredits))
    }
    forward()
  }

  def mySearch(): String = {
    put("departs", getDeparts)
    put("proposerId", getUserId)
    forward()
  }

  def myApplications(): String = {
    val query = getQueryBuilder
    query.where("apply.proposer.id = :userId", getUserId)
    put("applications", entityDao.search(query))
    put("param", get("param"))
    forward()
  }

  protected def getQueryBuilder(): OqlBuilder[MajorPlanCourseGroupModifyBean] = {
    val query = OqlBuilder.from(classOf[MajorPlanCourseGroupModifyBean], "apply")
    populateConditions(query)
    val requestDate = get("requestDate")
    if (Strings.isNotBlank(requestDate)) {
      query.where("str(year(apply.applyDate)) like :requestDate", "%" + requestDate + "%")
    }
    var courseTypeName = get("courseTypeName")
    courseTypeName = if (Strings.isBlank(courseTypeName)) "%" else "%" + courseTypeName + "%"
    val subquery = new StringBuilder()
    subquery.append("(exists(").append(" select state.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailBeforeBean state")
      .append(" where state.apply.id = apply.id")
      .append(" and state.courseType.name like :courseTypeName1")
      .append(") or exists(")
      .append(" select state.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailAfterBean state")
      .append(" where state.apply.id = apply.id")
      .append(" and state.courseType.name like :courseTypeName2")
      .append("))")
    query.where(subquery.toString, courseTypeName, courseTypeName)
    query.orderBy(Order.desc("apply.applyDate"))
    query.orderBy(Order.asc("apply.flag"))
    query.limit(getPageLimit)
    query
  }

  private def getMajorPlan(): MajorPlan = {
    val planId = getLong("planId")
    if (null == planId) throw new RuntimeException("not found planId")
    entityDao.get(classOf[MajorPlan], planId)
  }

  private def getMajorPlanCourseGroup(): MajorPlanCourseGroup = {
    val courseGroupId = getLong("courseGroupId")
    if (null == courseGroupId) throw new RuntimeException("not found courseGroupId")
    entityDao.get(classOf[MajorPlanCourseGroup], courseGroupId)
  }

  private def saveApplyInfo() {
    val modifyBean = populate(classOf[MajorPlanCourseGroupModifyBean], "modifyApply")
    modifyBean.setProposer(entityDao.get(classOf[User], getUserId))
    val courseGroupId = getLong("courseGroupId")
    var after = populate(classOf[MajorPlanCourseGroupModifyDetailAfterBean], "after")
    if (MajorPlanCourseModifyBean.DELETE == modifyBean.getRequisitionType) {
      after = null
    }
    majorPlanCourseGroupModifyApplyService.saveModifyApply(modifyBean, courseGroupId, after)
  }

  protected def getExportDatas(): Collection[_] = {
    entityDao.search(getQueryBuilder.limit(null))
  }

  protected def getPropertyExtractor(): PropertyExtractor = {
    val extractor = new CourseGroupModifyPropertyExtractor(getTextResource)
    extractor.setEntityDao(entityDao)
    extractor.setTextResource(getTextResource)
    extractor
  }

  def setMajorPlanService(majorPlanService: MajorPlanService) {
    this.majorPlanService = majorPlanService
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }

  def setMajorPlanCourseGroupModifyApplyService(majorPlanCourseGroupModifyApplyService: MajorPlanCourseGroupModifyApplyService) {
    this.majorPlanCourseGroupModifyApplyService = majorPlanCourseGroupModifyApplyService
  }
}
