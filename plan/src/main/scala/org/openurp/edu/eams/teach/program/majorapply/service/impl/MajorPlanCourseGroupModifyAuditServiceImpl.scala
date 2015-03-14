package org.openurp.edu.eams.teach.program.majorapply.service.impl

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseGroupBean
import org.openurp.edu.eams.teach.program.major.service.MajorPlanCourseGroupService
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorPlanCourseGroupModifyAuditDao
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseGroupModifyAuditService
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupModifyAuditServiceImpl extends BaseServiceImpl with MajorPlanCourseGroupModifyAuditService {

  private var majorPlanCourseGroupModifyAuditDao: MajorPlanCourseGroupModifyAuditDao = _

  protected var majorPlanCourseGroupService: MajorPlanCourseGroupService = _

  protected var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  def approved(apply: MajorPlanCourseGroupModifyBean, assessor: User) {
    majorPlanCourseGroupModifyAuditDao.approved(apply, assessor)
    if (MajorPlanCourseGroupModifyBean.ADD == apply.getRequisitionType) {
      var parent: MajorPlanCourseGroup = null
      val plan = entityDao.get(classOf[MajorPlan], apply.getMajorPlan.getId)
      if (apply.getNewPlanCourseGroup != null && apply.getNewPlanCourseGroup.getParent != null) {
        parent = entityDao.get(classOf[MajorPlanCourseGroup], apply.getNewPlanCourseGroup.getParent.getId)
      }
      val typeId = apply.getNewPlanCourseGroup.getCourseType.getId
      val group = planCourseGroupCommonDao.getCourseGroupByCourseType(new MajorPlanCourseGroupBean(), 
        plan.getId, typeId).asInstanceOf[MajorPlanCourseGroup]
      if (MajorPlanCourseGroupModifyBean.ADD == apply.getRequisitionType) {
        var indexno = 0
        indexno = if (parent != null) parent.getChildren.size + 1 else plan.getTopCourseGroups.size + 1
        majorPlanCourseGroupService.move(group, parent, indexno)
      }
    }
  }

  def rejected(apply: MajorPlanCourseGroupModifyBean, assessor: User) {
    majorPlanCourseGroupModifyAuditDao.rejected(apply, assessor)
  }

  def setMajorPlanCourseGroupModifyAuditDao(majorPlanCourseGroupModifyAuditDao: MajorPlanCourseGroupModifyAuditDao) {
    this.majorPlanCourseGroupModifyAuditDao = majorPlanCourseGroupModifyAuditDao
  }

  def setMajorPlanCourseGroupService(majorPlanCourseGroupService: MajorPlanCourseGroupService) {
    this.majorPlanCourseGroupService = majorPlanCourseGroupService
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }
}
