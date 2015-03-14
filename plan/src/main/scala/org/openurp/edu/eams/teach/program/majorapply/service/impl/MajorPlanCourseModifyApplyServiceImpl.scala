package org.openurp.edu.eams.teach.program.majorapply.service.impl

import java.util.List
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorPlanCourseModifyApplyDao
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailBeforeBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseModifyApplyService
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseModifyApplyServiceImpl extends BaseServiceImpl with MajorPlanCourseModifyApplyService {

  private var majorPlanCourseModifyApplyDao: MajorPlanCourseModifyApplyDao = _

  def saveModifyApply(apply: MajorPlanCourseModifyBean, before: MajorPlanCourseModifyDetailBeforeBean, after: MajorPlanCourseModifyDetailAfterBean) {
    majorPlanCourseModifyApplyDao.saveModifyApply(apply, before, after)
  }

  def setMajorPlanCourseModifyApplyDao(majorPlanCourseModifyApplyDao: MajorPlanCourseModifyApplyDao) {
    this.majorPlanCourseModifyApplyDao = majorPlanCourseModifyApplyDao
  }

  def myApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseModifyBean] = {
    val query = OqlBuilder.from(classOf[MajorPlanCourseModifyBean], "apply")
    query.where("apply.majorPlan.id = :planId", planId)
      .where("apply.proposer.id = :userId", userId)
      .orderBy("apply.flag")
      .orderBy(Order.desc("apply.applyDate"))
    entityDao.search(query)
  }

  def myReadyAddApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseModifyBean] = {
    val query = OqlBuilder.from(classOf[MajorPlanCourseModifyBean], "apply")
    query.where("apply.majorPlan.id = :planId", planId)
      .where("apply.proposer.id = :userId", userId)
      .where("apply.flag = :flag", MajorPlanCourseModifyBean.INITREQUEST)
      .where("apply.requisitionType = :requisitionType", MajorPlanCourseModifyBean.ADD)
      .orderBy(Order.desc("apply.applyDate"))
    entityDao.search(query)
  }

  def appliesOfPlan(planId: java.lang.Long): List[MajorPlanCourseModifyBean] = {
    val query = OqlBuilder.from(classOf[MajorPlanCourseModifyBean], "apply")
    query.where("apply.majorPlan.id = :planId", planId)
      .orderBy("apply.flag")
      .orderBy(Order.desc("apply.applyDate"))
      .limit(null)
    entityDao.search(query)
  }

  def myReadyModifyApply(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourse] = {
    val query = OqlBuilder.from("org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean apply, org.openurp.edu.eams.teach.program.major.MajorPlan plan")
    query.select("select pcourse").where("apply.majorPlan.id = plan.id")
      .join("plan.groups", "cgroup")
      .join("cgroup.planCourses", "pcourse")
      .where("apply.majorPlan.id = :planId", planId)
      .where("apply.proposer.id = :userId", userId)
      .where("apply.flag = :flag", MajorPlanCourseModifyBean.INITREQUEST)
      .where("exists(select state.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailBeforeBean state where state.apply.id = apply.id and state.course=pcourse.course)")
    entityDao.search(query)
  }
}
