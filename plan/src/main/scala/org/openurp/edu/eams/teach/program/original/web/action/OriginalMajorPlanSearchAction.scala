package org.openurp.edu.eams.teach.program.original.web.action

import com.ekingstar.eams.teach.code.school.CourseHourType
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.common.service.PlanCompareService
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanAuditService
import org.openurp.edu.eams.teach.program.original.OriginalPlan
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import com.ekingstar.eams.web.action.common.RestrictionSupportAction
//remove if not needed
import scala.collection.JavaConversions._

class OriginalMajorPlanSearchAction extends RestrictionSupportAction {

  private var majorPlanAuditService: MajorPlanAuditService = _

  private var planCompareService: PlanCompareService = _

  private var planCommonDao: PlanCommonDao = _

  private var coursePlanProvider: CoursePlanProvider = _

  def info(): String = {
    val originalPlanId = getLongId("plan")
    if (null == originalPlanId) {
      return forwardError("error.model.id.needed")
    }
    put("plan", entityDao.get(classOf[OriginalPlan], originalPlanId))
    put("weekHour", get("weekHour"))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  def infoByExecutivePlan(): String = {
    val majorPlanId = getLongId("plan")
    if (null == majorPlanId) {
      return forwardError("error.model.id.needed")
    }
    put("plan", majorPlanAuditService.getOriginalMajorPlan(majorPlanId))
    put("weekHour", get("weekHour"))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward("info")
  }

  def compareWithExecutivePlan(): String = {
    val originalPlanId = getLongId("plan")
    val originalMajorPlan = entityDao.get(classOf[OriginalPlan], originalPlanId)
    val majorPlan = coursePlanProvider.getMajorPlan(originalMajorPlan.getProgram)
    put("majorPlan", majorPlan)
    put("originalPlan", originalMajorPlan)
    put("diffResult", planCompareService.diff(majorPlan, originalMajorPlan))
    put("majorCourseTypes", planCommonDao.getUsedCourseTypes(majorPlan))
    put("stdCourseTypes", planCommonDao.getUsedCourseTypes(originalMajorPlan))
    forward()
  }

  def setMajorPlanAuditService(majorPlanAuditService: MajorPlanAuditService) {
    this.majorPlanAuditService = majorPlanAuditService
  }

  def setPlanCompareService(planCompareService: PlanCompareService) {
    this.planCompareService = planCompareService
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }

  def setCoursePlanProvider(coursePlanProvider: CoursePlanProvider) {
    this.coursePlanProvider = coursePlanProvider
  }
}
