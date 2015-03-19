package org.openurp.edu.eams.teach.program.personal.service.impl


import org.beangle.commons.dao.impl.BaseServiceImpl
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.common.copydao.coursegroup.IPlanCourseGroupCopyDao
import org.openurp.edu.eams.teach.program.common.copydao.plancourse.IPlanCourseCopyDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.common.service.PlanCompareService
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourse
import org.openurp.edu.eams.teach.program.personal.exception.PersonalPlanSyncException
import org.openurp.edu.eams.teach.program.personal.service.PersonalPlanCompareService
//remove if not needed


class PersonalPlanCompareServiceImpl extends BaseServiceImpl with PersonalPlanCompareService {

  private var planCommonDao: PlanCommonDao = _

  private var planCourseCommonDao: PlanCourseCommonDao = _

  private var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  private var planCompareService: PlanCompareService = _

  private var personalPlanCourseGroupCopyDao: IPlanCourseGroupCopyDao = _

  private var personalPlanCourseCopyDao: IPlanCourseCopyDao = _

  def diffPersonalAndMajorPlan(majorPlan: MajorPlan, stdMajorPlan: PersonalPlan): Map[CourseType, Array[List[_ <: PlanCourse]]] = {
    planCompareService.diff(majorPlan, stdMajorPlan)
  }

  def copyCourseGroups(fromPlan: MajorPlan, toPlan: PersonalPlan, courseTypeIds: List[Integer]) {
    copyCourseGroups(fromPlan, toPlan, courseTypeIds, true)
  }

  def copyPlanCourses(fromPlan: MajorPlan, toPlan: PersonalPlan, courseTypePlanCourseIds: List[Array[Number]]) {
    for (courseTypePlanCourseId <- courseTypePlanCourseIds) {
      val courseTypeId = courseTypePlanCourseId(0).intValue()
      val `type` = entityDao.get(classOf[CourseType], courseTypeId)
      if (fromPlan.getGroup(`type`) == null) {
        throw new PersonalPlanSyncException("fromPlan doesn't have this kind of course type's course group: " + 
          `type`.toString)
      }
      if (toPlan.getGroup(`type`) == null) {
        copyCourseGroups(fromPlan, toPlan, Collections.singletonList(courseTypeId), false)
      }
      val sourcePlanCourse = entityDao.get(classOf[MajorPlanCourse], courseTypePlanCourseId(1).asInstanceOf[java.lang.Long])
      if (sourcePlanCourse == null) {
        throw new PersonalPlanSyncException("Cannot find PlanCourse")
      }
      personalPlanCourseCopyDao.copyPlanCourse(sourcePlanCourse, toPlan.getGroup(`type`).asInstanceOf[MajorCourseGroup])
      toPlan.setCredits(planCommonDao.statPlanCredits(toPlan))
      planCommonDao.saveOrUpdatePlan(toPlan)
      entityDao.refresh(toPlan)
    }
  }

  private def copyCourseGroups(fromPlan: MajorPlan, 
      toPlan: PersonalPlan, 
      courseTypeIds: List[Integer], 
      copyPlanCourses: Boolean) {
    for (typeId <- courseTypeIds) {
      val `type` = entityDao.get(classOf[CourseType], typeId)
      if (fromPlan.getGroup(`type`) == null) {
        throw new PersonalPlanSyncException("源计划不存在课程类别：" + `type`.toString)
      }
      if (toPlan.getGroup(`type`) != null) {
        deleteCourseGroups(toPlan, Collections.singletonList(typeId))
      }
      val sourceGroup = fromPlan.getGroup(`type`)
      if (sourceGroup == null) {
        throw new PersonalPlanSyncException("源计划不存在课程类别：" + `type`.toString)
      }
      var copy: CourseGroup = null
      if (sourceGroup.getParent != null) {
        if (toPlan.getGroup(sourceGroup.getParent.getCourseType) == 
          null) {
          throw new PersonalPlanSyncException("请先复制父课程组：" + sourceGroup.getParent.getCourseType.getName)
        }
        copy = personalPlanCourseGroupCopyDao.copyCourseGroup(sourceGroup, toPlan.getGroup(sourceGroup.getParent.getCourseType), 
          toPlan)
      } else {
        copy = personalPlanCourseGroupCopyDao.copyCourseGroup(sourceGroup.asInstanceOf[MajorCourseGroup], 
          null, toPlan)
      }
      if (copy == null) {
        throw new PersonalPlanSyncException("复制课程组失败：" + `type`.toString)
      }
      if (!copyPlanCourses) {
        copy.getPlanCourses.clear()
      }
      planCourseGroupCommonDao.updateGroupTreeCredits(planCourseGroupCommonDao.getTopGroup(copy))
      toPlan.setCredits(planCommonDao.statPlanCredits(toPlan))
      entityDao.saveOrUpdate(toPlan)
      entityDao.refresh(toPlan)
    }
  }

  def deleteCourseGroups(plan: PersonalPlan, courseTypeIds: List[Integer]) {
    for (typeId <- courseTypeIds) {
      val `type` = entityDao.get(classOf[CourseType], typeId)
      val group = plan.getGroup(`type`)
      if (group == null) {
        throw new PersonalPlanSyncException("源计划不存在课程类别：" + `type`.toString)
      }
      planCourseGroupCommonDao.removeCourseGroup(group.asInstanceOf[MajorCourseGroup])
      plan.setCredits(planCommonDao.statPlanCredits(plan))
      entityDao.saveOrUpdate(plan)
      entityDao.refresh(plan)
    }
  }

  def deletePlanCourses(plan: PersonalPlan, courseTypePlanCourseIds: List[Array[Number]]) {
    for (courseTypePlanCourseId <- courseTypePlanCourseIds) {
      val `type` = entityDao.get(classOf[CourseType], courseTypePlanCourseId(0).intValue())
      val group = plan.getGroup(`type`)
      if (group == null) {
        throw new PersonalPlanSyncException("源计划不存在课程类别：" + `type`.toString)
      }
      val toBeRemoved = entityDao.get(classOf[PersonalPlanCourse], courseTypePlanCourseId(1).longValue())
      if (toBeRemoved == null) {
        throw new PersonalPlanSyncException("无法找到计划课程")
      }
      planCourseCommonDao.removePlanCourse(toBeRemoved, plan)
      entityDao.refresh(plan)
    }
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }

  def setPlanCourseCommonDao(planCourseCommonDao: PlanCourseCommonDao) {
    this.planCourseCommonDao = planCourseCommonDao
  }

  def setPlanCompareService(planCompareService: PlanCompareService) {
    this.planCompareService = planCompareService
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }

  def setPersonalPlanCourseGroupCopyDao(personalPlanCourseGroupCopyDao: IPlanCourseGroupCopyDao) {
    this.personalPlanCourseGroupCopyDao = personalPlanCourseGroupCopyDao
  }

  def setPersonalPlanCourseCopyDao(personalPlanCourseCopyDao: IPlanCourseCopyDao) {
    this.personalPlanCourseCopyDao = personalPlanCourseCopyDao
  }
}
