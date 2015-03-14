package org.openurp.edu.eams.teach.program.majorapply.service.impl

import java.util.List
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorPlanCourseGroupModifyApplyDao
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailBeforeBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.component.FakeCourseGroup
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseGroupModifyApplyService
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupModifyApplyServiceImpl extends BaseServiceImpl with MajorPlanCourseGroupModifyApplyService {

  private var majorPlanCourseGroupModifyApplyDao: MajorPlanCourseGroupModifyApplyDao = _

  def appliesOfPlan(planId: java.lang.Long): List[MajorPlanCourseGroupModifyBean] = {
    val oql = OqlBuilder.from(classOf[MajorPlanCourseGroupModifyBean], "applyBean")
    oql.where("applyBean.majorPlan.id =:planId", planId)
    entityDao.search(oql)
  }

  def myApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseGroupModifyBean] = {
    val oql = OqlBuilder.from(classOf[MajorPlanCourseGroupModifyBean], "apply")
    oql.where("apply.majorPlan.id =:planId", planId).where("apply.proposer.id =:userId", userId)
      .orderBy("apply.flag")
      .orderBy(Order.desc("apply.applyDate"))
    entityDao.search(oql)
  }

  def myReadyModifyApply(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseGroup] = {
    val query = OqlBuilder.from("org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean apply, org.openurp.edu.eams.teach.program.major.MajorPlan plan")
    query.select("select cgroup").where("apply.majorPlan.id = plan.id")
      .join("plan.groups", "cgroup")
      .where("apply.majorPlan.id = :planId", planId)
      .where("apply.proposer.id = :userId", userId)
      .where("apply.flag = :flag", MajorPlanCourseModifyBean.INITREQUEST)
      .where("exists(select state.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailBeforeBean state " + 
      "where state.apply.id = apply.id and state.courseType=cgroup.courseType)")
    entityDao.search(query)
  }

  def myReadyAddApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseGroupModifyBean] = {
    val oql = OqlBuilder.from(classOf[MajorPlanCourseGroupModifyBean], "apply")
    oql.where("apply.majorPlan.id =:planId", planId).where("apply.proposer.id =:userId", userId)
      .where("apply.flag = :flag", MajorPlanCourseModifyBean.INITREQUEST)
      .where("apply.requisitionType = :requisitionType", MajorPlanCourseModifyBean.ADD)
      .orderBy(Order.desc("apply.applyDate"))
    entityDao.search(oql)
  }

  def saveModifyApply(modifyBean: MajorPlanCourseGroupModifyBean, courseGroupId: java.lang.Long, after: MajorPlanCourseGroupModifyDetailAfterBean) {
    var before: MajorPlanCourseGroupModifyDetailBeforeBean = null
    if (courseGroupId != null) {
      val courseGroup = entityDao.get(classOf[MajorPlanCourseGroup], courseGroupId)
      before = new MajorPlanCourseGroupModifyDetailBeforeBean()
      before.setApply(modifyBean)
      before.setRelation(courseGroup.getRelation)
      before.setCourseType(courseGroup.getCourseType)
      before.setCourseNum(courseGroup.getCourseNum)
      before.setCredits(courseGroup.getCredits)
      before.setParent(new FakeCourseGroup(courseGroup.getParent.asInstanceOf[MajorPlanCourseGroup]))
      before.setRemark(courseGroup.getRemark)
      before.setTermCredits(courseGroup.getTermCredits)
    }
    majorPlanCourseGroupModifyApplyDao.saveModifyApply(modifyBean, before, after)
  }

  def setMajorPlanCourseGroupModifyApplyDao(majorPlanCourseGroupModifyApplyDao: MajorPlanCourseGroupModifyApplyDao) {
    this.majorPlanCourseGroupModifyApplyDao = majorPlanCourseGroupModifyApplyDao
  }
}
