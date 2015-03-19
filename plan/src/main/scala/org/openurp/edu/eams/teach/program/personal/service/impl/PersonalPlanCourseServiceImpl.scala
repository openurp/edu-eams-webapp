package org.openurp.edu.eams.teach.program.personal.service.impl

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseCommonDao
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourse
import org.openurp.edu.eams.teach.program.personal.service.PersonalPlanCourseService
//remove if not needed


class PersonalPlanCourseServiceImpl extends BaseServiceImpl with PersonalPlanCourseService {

  private var planCourseCommonDao: PlanCourseCommonDao = _

  def addPlanCourse(planCourse: PersonalPlanCourse, plan: PersonalPlan) {
    planCourseCommonDao.addPlanCourse(planCourse, plan)
  }

  def removePlanCourse(planCourse: PersonalPlanCourse, plan: PersonalPlan) {
    planCourseCommonDao.removePlanCourse(planCourse, plan)
  }

  def getPersonalPlanCourseDwr(id: java.lang.Long): PersonalPlanCourse = {
    entityDao.get(classOf[PersonalPlanCourse], id)
  }

  def updatePlanCourse(planCourse: PersonalPlanCourse, plan: PersonalPlan) {
    planCourseCommonDao.updatePlanCourse(planCourse, plan)
  }

  def setPlanCourseCommonDao(planCourseCommonDao: PlanCourseCommonDao) {
    this.planCourseCommonDao = planCourseCommonDao
  }
}
