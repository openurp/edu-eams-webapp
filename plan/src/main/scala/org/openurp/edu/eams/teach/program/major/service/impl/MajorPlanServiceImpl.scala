package org.openurp.edu.eams.teach.program.major.service.impl




import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import com.ekingstar.eams.core.Adminclass
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.common.copydao.plan.IPlanCopyDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.helper.ProgramNamingHelper
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import com.ekingstar.eams.teach.util.MajorPlanQueryBuilder
//remove if not needed


class MajorPlanServiceImpl extends BaseServiceImpl with MajorPlanService {

  private var planCommonDao: PlanCommonDao = _

  private var majorPlanCopyDao: IPlanCopyDao = _

  def genMajorPlan(sourcePlan: MajorPlan, genParameter: MajorPlanGenParameter): CoursePlan = {
    majorPlanCopyDao.copyMajorPlan(sourcePlan, genParameter)
  }

  def genMajorPlans(plans: Iterable[MajorPlan], partialParams: MajorPlanGenParameter): List[MajorPlan] = {
    val genedPlans = new ArrayList[MajorPlan](plans.size)
    for (plan <- plans) {
      val t_param = new MajorPlanGenParameter()
      t_param.setGrade(partialParams.getGrade)
      t_param.setEffectiveOn(partialParams.getEffectiveOn)
      t_param.setInvalidOn(partialParams.getInvalidOn)
      t_param.setDuration(partialParams.getDuration)
      t_param.setTermsCount(partialParams.getTermsCount)
      t_param.setDegree(plan.getProgram.getDegree)
      t_param.setDepartment(plan.getProgram.getDepartment)
      t_param.setDirection(plan.getProgram.getDirection)
      t_param.setEducation(plan.getProgram.getEducation)
      t_param.setMajor(plan.getProgram.getMajor)
      t_param.setStdType(plan.getProgram.getStdType)
      t_param.setStudyType(plan.getProgram.getStudyType)
      t_param.setName(ProgramNamingHelper.name(t_param, entityDao))
      genedPlans.add(genMajorPlan(plan, t_param).asInstanceOf[MajorPlan])
    }
    genedPlans
  }

  def getUnusedCourseTypes(plan: MajorPlan): List[CourseType] = {
    planCommonDao.getUnusedCourseTypes(plan)
  }

  def removeMajorPlan(plan: MajorPlan) {
    planCommonDao.removePlan(plan)
  }

  def saveOrUpdateMajorPlan(plan: MajorPlan) {
    planCommonDao.saveOrUpdatePlan(plan)
  }

  def statPlanCredits(planId: java.lang.Long): Float = {
    statPlanCredits(entityDao.get(classOf[MajorPlan], planId))
  }

  def statPlanCredits(plan: MajorPlan): Float = planCommonDao.statPlanCredits(plan)

  def hasCourse(cgroup: MajorCourseGroup, course: Course): Boolean = planCommonDao.hasCourse(cgroup, course)

  def hasCourse(cgroup: MajorCourseGroup, course: Course, planCourse: PlanCourse): Boolean = {
    planCommonDao.hasCourse(cgroup, course, planCourse)
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }

  def setMajorPlanCopyDao(majorPlanCopyDao: IPlanCopyDao) {
    this.majorPlanCopyDao = majorPlanCopyDao
  }

  def getMajorPlanByAdminClass(clazz: Adminclass): MajorPlan = {
    val res = entityDao.search(MajorPlanQueryBuilder.build(clazz))
    if (Collections.isEmpty(res)) null else res.get(0)
  }

  def getPlanCourses(plan: MajorPlan): List[MajorPlanCourse] = {
    if (Collections.isEmpty(plan.getGroups)) {
      return Collections.EMPTY_LIST
    }
    val planCourses = new ArrayList[MajorPlanCourse]()
    var iter = plan.getGroups.iterator()
    while (iter.hasNext) {
      val group = iter.next().asInstanceOf[MajorCourseGroup]
      planCourses.addAll(group.getPlanCourses.asInstanceOf[List[_]])
    }
    planCourses
  }
}
