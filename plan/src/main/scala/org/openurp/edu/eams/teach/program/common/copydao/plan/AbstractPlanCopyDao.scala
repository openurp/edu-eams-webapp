package org.openurp.edu.eams.teach.program.common.copydao.plan


import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.common.copydao.coursegroup.IPlanCourseGroupCopyDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
import org.openurp.edu.eams.teach.program.original.OriginalPlan
//remove if not needed


abstract class AbstractPlanCopyDao extends HibernateEntityDao with IPlanCopyDao {

  protected var courseGroupCopyDao: IPlanCourseGroupCopyDao = _

  protected var planCommonDao: PlanCommonDao = _

  def copyMajorPlan(sourcePlan: MajorPlan, genParameter: MajorPlanGenParameter): CoursePlan = {
    var newProgram: Program = null
    newProgram = if (sourcePlan == null) newProgram(null, genParameter) else newProgram(sourcePlan.getProgram, 
      genParameter)
    saveOrUpdate(newProgram)
    val newPlan = newPlan(sourcePlan)
    if (genParameter != null) {
      newPlan.setTermsCount(genParameter.getTermsCount)
    } else {
      newPlan.setTermsCount(sourcePlan.getTermsCount)
    }
    newPlan.setGroups(new ArrayList[CourseGroup]())
    if (newPlan.isInstanceOf[MajorPlan]) {
      newPlan.asInstanceOf[MajorPlan].setProgram(newProgram)
    } else if (newPlan.isInstanceOf[OriginalPlan]) {
      newPlan.asInstanceOf[OriginalPlan].setProgram(newProgram)
    }
    saveOrUpdate(newPlan)
    if (sourcePlan != null) {
      for (sourceCourseGroup <- sourcePlan.getTopCourseGroups) {
        courseGroupCopyDao.copyCourseGroup(sourceCourseGroup.asInstanceOf[MajorCourseGroup], null, 
          newPlan)
      }
    }
    planCommonDao.saveOrUpdatePlan(newPlan)
    newPlan
  }

  protected def newProgram(originalProgram: Program, genParameter: MajorPlanGenParameter): Program

  protected def newPlan(plan: MajorPlan): CoursePlan

  def setCourseGroupCopyDao(courseGroupCopyDao: IPlanCourseGroupCopyDao) {
    this.courseGroupCopyDao = courseGroupCopyDao
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }
}
