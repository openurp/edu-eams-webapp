package org.openurp.edu.eams.teach.program.majorapply.service.impl


import org.beangle.commons.collection.Order
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorCourseGroupModifyApplyDao
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailBeforeBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.component.FakeCourseGroup
import org.openurp.edu.eams.teach.program.majorapply.service.MajorCourseGroupModifyApplyService
//remove if not needed


class MajorCourseGroupModifyApplyServiceImpl extends BaseServiceImpl with MajorCourseGroupModifyApplyService {

  private var MajorCourseGroupModifyApplyDao: MajorCourseGroupModifyApplyDao = _

  def appliesOfPlan(planId: java.lang.Long): List[MajorCourseGroupModifyBean] = {
    val oql = OqlBuilder.from(classOf[MajorCourseGroupModifyBean], "applyBean")
    oql.where("applyBean.majorPlan.id =:planId", planId)
    entityDao.search(oql)
  }

  def myApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorCourseGroupModifyBean] = {
    val oql = OqlBuilder.from(classOf[MajorCourseGroupModifyBean], "apply")
    oql.where("apply.majorPlan.id =:planId", planId).where("apply.proposer.id =:userId", userId)
      .orderBy("apply.flag")
      .orderBy(Order.desc("apply.applyDate"))
    entityDao.search(oql)
  }

  def myReadyModifyApply(planId: java.lang.Long, userId: java.lang.Long): List[MajorCourseGroup] = {
    val query = OqlBuilder.from("org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean apply, org.openurp.edu.eams.teach.program.major.MajorPlan plan")
    query.select("select cgroup").where("apply.majorPlan.id = plan.id")
      .join("plan.groups", "cgroup")
      .where("apply.majorPlan.id = :planId", planId)
      .where("apply.proposer.id = :userId", userId)
      .where("apply.flag = :flag", MajorPlanCourseModifyBean.INITREQUEST)
      .where("exists(select state.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailBeforeBean state " + 
      "where state.apply.id = apply.id and state.courseType=cgroup.courseType)")
    entityDao.search(query)
  }

  def myReadyAddApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorCourseGroupModifyBean] = {
    val oql = OqlBuilder.from(classOf[MajorCourseGroupModifyBean], "apply")
    oql.where("apply.majorPlan.id =:planId", planId).where("apply.proposer.id =:userId", userId)
      .where("apply.flag = :flag", MajorPlanCourseModifyBean.INITREQUEST)
      .where("apply.requisitionType = :requisitionType", MajorPlanCourseModifyBean.ADD)
      .orderBy(Order.desc("apply.applyDate"))
    entityDao.search(oql)
  }

  def saveModifyApply(modifyBean: MajorCourseGroupModifyBean, courseGroupId: java.lang.Long, after: MajorCourseGroupModifyDetailAfterBean) {
    var before: MajorCourseGroupModifyDetailBeforeBean = null
    if (courseGroupId != null) {
      val courseGroup = entityDao.get(classOf[MajorCourseGroup], courseGroupId)
      before = new MajorCourseGroupModifyDetailBeforeBean()
      before.setApply(modifyBean)
      before.setRelation(courseGroup.getRelation)
      before.setCourseType(courseGroup.getCourseType)
      before.setCourseNum(courseGroup.getCourseNum)
      before.setCredits(courseGroup.getCredits)
      before.setParent(new FakeCourseGroup(courseGroup.getParent.asInstanceOf[MajorCourseGroup]))
      before.setRemark(courseGroup.getRemark)
      before.setTermCredits(courseGroup.getTermCredits)
    }
    MajorCourseGroupModifyApplyDao.saveModifyApply(modifyBean, before, after)
  }

  def setMajorCourseGroupModifyApplyDao(MajorCourseGroupModifyApplyDao: MajorCourseGroupModifyApplyDao) {
    this.MajorCourseGroupModifyApplyDao = MajorCourseGroupModifyApplyDao
  }
}
