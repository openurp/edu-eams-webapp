package org.openurp.edu.eams.teach.program.majorapply.dao.hibernate

import java.util.Date
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorCourseGroupBean
import org.openurp.edu.eams.teach.program.major.service.MajorCourseGroupService
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorCourseGroupModifyAuditDao
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
//remove if not needed


class MajorCourseGroupModifyAuditDaoHibernate extends HibernateEntityDao with MajorCourseGroupModifyAuditDao {

  private var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  def rejected(apply: MajorCourseGroupModifyBean, assessor: User) {
    apply.setAssessor(assessor)
    apply.setFlag(MajorCourseGroupModifyBean.REFUSE)
    apply.setReplyDate(new Date())
    saveOrUpdate(apply)
  }

  def approved(apply: MajorCourseGroupModifyBean, assessor: User) {
    val plan = get(classOf[MajorPlan], apply.getMajorPlan.id)
    if (plan == null) {
      throw new MajorPlanAuditException("您要修改的专业培养计划已经不存在。")
    }
    var parent: MajorCourseGroup = null
    if (apply.getNewPlanCourseGroup != null && apply.getNewPlanCourseGroup.getParent != null) {
      parent = get(classOf[MajorCourseGroup], apply.getNewPlanCourseGroup.getParent.id)
      if (null == parent) {
        throw new MajorPlanAuditException("父课程组已不存在")
      }
    }
    val courseGroup = new MajorCourseGroupBean()
    if (MajorCourseGroupModifyBean.DELETE == apply.getRequisitionType) {
      val typeId = apply.getOldPlanCourseGroup.getCourseType.id
      val group = planCourseGroupCommonDao.getCourseGroupByCourseType(courseGroup, plan.id, typeId).asInstanceOf[MajorCourseGroup]
      if (group != null) {
        planCourseGroupCommonDao.removeCourseGroup(group)
      } else {
        throw new MajorPlanAuditException("课程组不存在:" + apply.getOldPlanCourseGroup.getCourseType.getName)
      }
    } else if (MajorCourseGroupModifyBean.ADD == apply.getRequisitionType) {
      val typeId = apply.getNewPlanCourseGroup.getCourseType.id
      var group = planCourseGroupCommonDao.getCourseGroupByCourseType(courseGroup, plan.id, typeId).asInstanceOf[MajorCourseGroup]
      if (group != null) {
        throw new MajorPlanAuditException("课程组已存在:" + apply.getNewPlanCourseGroup.getCourseType.getName)
      }
      group = new MajorCourseGroupBean()
      group.setPlan(plan)
      group.setCourseType(apply.getNewPlanCourseGroup.getCourseType)
      group.setCourseNum(apply.getNewPlanCourseGroup.getCourseNum)
      group.setTermCredits(apply.getNewPlanCourseGroup.getTermCredits)
      group.setCredits(apply.getNewPlanCourseGroup.getCredits)
      group.setRelation(apply.getNewPlanCourseGroup.getRelation)
      group.setRemark(apply.getNewPlanCourseGroup.getRemark)
      group.setIndexno("--")
      planCourseGroupCommonDao.addCourseGroupToPlan(group, parent, plan)
    } else if (MajorCourseGroupModifyBean.MODIFY == apply.getRequisitionType) {
      val typeId = apply.getOldPlanCourseGroup.getCourseType.id
      val oldGroup = planCourseGroupCommonDao.getCourseGroupByCourseType(courseGroup, plan.id, typeId).asInstanceOf[MajorCourseGroup]
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
    apply.setFlag(MajorCourseGroupModifyBean.ACCEPT)
    apply.setReplyDate(new Date())
    saveOrUpdate(apply)
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }
}
