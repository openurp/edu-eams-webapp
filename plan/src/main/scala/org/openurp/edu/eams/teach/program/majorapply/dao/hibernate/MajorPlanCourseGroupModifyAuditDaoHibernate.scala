package org.openurp.edu.eams.teach.program.majorapply.dao.hibernate

import java.util.Date
import org.beangle.orm.hibernate.HibernateEntityDao
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseGroupBean
import org.openurp.edu.eams.teach.program.major.service.MajorPlanCourseGroupService
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorPlanCourseGroupModifyAuditDao
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupModifyAuditDaoHibernate extends HibernateEntityDao with MajorPlanCourseGroupModifyAuditDao {

  private var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  def rejected(apply: MajorPlanCourseGroupModifyBean, assessor: User) {
    apply.setAssessor(assessor)
    apply.setFlag(MajorPlanCourseGroupModifyBean.REFUSE)
    apply.setReplyDate(new Date())
    saveOrUpdate(apply)
  }

  def approved(apply: MajorPlanCourseGroupModifyBean, assessor: User) {
    val plan = get(classOf[MajorPlan], apply.getMajorPlan.getId)
    if (plan == null) {
      throw new MajorPlanAuditException("您要修改的专业培养计划已经不存在。")
    }
    var parent: MajorPlanCourseGroup = null
    if (apply.getNewPlanCourseGroup != null && apply.getNewPlanCourseGroup.getParent != null) {
      parent = get(classOf[MajorPlanCourseGroup], apply.getNewPlanCourseGroup.getParent.getId)
      if (null == parent) {
        throw new MajorPlanAuditException("父课程组已不存在")
      }
    }
    val courseGroup = new MajorPlanCourseGroupBean()
    if (MajorPlanCourseGroupModifyBean.DELETE == apply.getRequisitionType) {
      val typeId = apply.getOldPlanCourseGroup.getCourseType.getId
      val group = planCourseGroupCommonDao.getCourseGroupByCourseType(courseGroup, plan.getId, typeId).asInstanceOf[MajorPlanCourseGroup]
      if (group != null) {
        planCourseGroupCommonDao.removeCourseGroup(group)
      } else {
        throw new MajorPlanAuditException("课程组不存在:" + apply.getOldPlanCourseGroup.getCourseType.getName)
      }
    } else if (MajorPlanCourseGroupModifyBean.ADD == apply.getRequisitionType) {
      val typeId = apply.getNewPlanCourseGroup.getCourseType.getId
      var group = planCourseGroupCommonDao.getCourseGroupByCourseType(courseGroup, plan.getId, typeId).asInstanceOf[MajorPlanCourseGroup]
      if (group != null) {
        throw new MajorPlanAuditException("课程组已存在:" + apply.getNewPlanCourseGroup.getCourseType.getName)
      }
      group = new MajorPlanCourseGroupBean()
      group.setPlan(plan)
      group.setCourseType(apply.getNewPlanCourseGroup.getCourseType)
      group.setCourseNum(apply.getNewPlanCourseGroup.getCourseNum)
      group.setTermCredits(apply.getNewPlanCourseGroup.getTermCredits)
      group.setCredits(apply.getNewPlanCourseGroup.getCredits)
      group.setRelation(apply.getNewPlanCourseGroup.getRelation)
      group.setRemark(apply.getNewPlanCourseGroup.getRemark)
      group.setIndexno("--")
      planCourseGroupCommonDao.addCourseGroupToPlan(group, parent, plan)
    } else if (MajorPlanCourseGroupModifyBean.MODIFY == apply.getRequisitionType) {
      val typeId = apply.getOldPlanCourseGroup.getCourseType.getId
      val oldGroup = planCourseGroupCommonDao.getCourseGroupByCourseType(courseGroup, plan.getId, typeId).asInstanceOf[MajorPlanCourseGroup]
      if (oldGroup == null) {
        throw new MajorPlanAuditException("课程组不存在:" + apply.getOldPlanCourseGroup.getCourseType.getName)
      }
      planCourseGroupCommonDao.updateCourseGroupParent(oldGroup, parent, plan)
      oldGroup.setCourseType(apply.getNewPlanCourseGroup.getCourseType)
      oldGroup.setCourseNum(apply.getNewPlanCourseGroup.getCourseNum)
      oldGroup.setTermCredits(apply.getNewPlanCourseGroup.getTermCredits)
      oldGroup.setCredits(apply.getNewPlanCourseGroup.getCredits)
      oldGroup.setRelation(apply.getNewPlanCourseGroup.getRelation)
      oldGroup.setRemark(apply.getNewPlanCourseGroup.getRemark)
      planCourseGroupCommonDao.saveOrUpdateCourseGroup(oldGroup)
      saveOrUpdate(plan)
    }
    apply.setAssessor(assessor)
    apply.setFlag(MajorPlanCourseGroupModifyBean.ACCEPT)
    apply.setReplyDate(new Date())
    saveOrUpdate(apply)
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }
}
