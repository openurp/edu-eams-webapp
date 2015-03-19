package org.openurp.edu.eams.teach.program.majorapply.service.impl

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorCourseGroupBean
import org.openurp.edu.eams.teach.program.major.service.MajorCourseGroupService
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorCourseGroupModifyAuditDao
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorCourseGroupModifyAuditService
//remove if not needed


class MajorCourseGroupModifyAuditServiceImpl extends BaseServiceImpl with MajorCourseGroupModifyAuditService {

  private var MajorCourseGroupModifyAuditDao: MajorCourseGroupModifyAuditDao = _

  protected var MajorCourseGroupService: MajorCourseGroupService = _

  protected var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  def approved(apply: MajorCourseGroupModifyBean, assessor: User) {
    MajorCourseGroupModifyAuditDao.approved(apply, assessor)
    if (MajorCourseGroupModifyBean.ADD == apply.getRequisitionType) {
      var parent: MajorCourseGroup = null
      val plan = entityDao.get(classOf[MajorPlan], apply.getMajorPlan.id)
      if (apply.getNewPlanCourseGroup != null && apply.getNewPlanCourseGroup.getParent != null) {
        parent = entityDao.get(classOf[MajorCourseGroup], apply.getNewPlanCourseGroup.getParent.id)
      }
      val typeId = apply.getNewPlanCourseGroup.getCourseType.id
      val group = planCourseGroupCommonDao.getCourseGroupByCourseType(new MajorCourseGroupBean(), 
        plan.id, typeId).asInstanceOf[MajorCourseGroup]
      if (MajorCourseGroupModifyBean.ADD == apply.getRequisitionType) {
        var indexno = 0
        indexno = if (parent != null) parent.getChildren.size + 1 else plan.getTopCourseGroups.size + 1
        MajorCourseGroupService.move(group, parent, indexno)
      }
    }
  }

  def rejected(apply: MajorCourseGroupModifyBean, assessor: User) {
    MajorCourseGroupModifyAuditDao.rejected(apply, assessor)
  }

  def setMajorCourseGroupModifyAuditDao(MajorCourseGroupModifyAuditDao: MajorCourseGroupModifyAuditDao) {
    this.MajorCourseGroupModifyAuditDao = MajorCourseGroupModifyAuditDao
  }

  def setMajorCourseGroupService(MajorCourseGroupService: MajorCourseGroupService) {
    this.MajorCourseGroupService = MajorCourseGroupService
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }
}
