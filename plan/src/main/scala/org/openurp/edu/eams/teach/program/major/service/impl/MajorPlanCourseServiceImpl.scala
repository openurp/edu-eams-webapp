package org.openurp.edu.eams.teach.program.major.service.impl

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.major.service.MajorPlanCourseService
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseServiceImpl extends BaseServiceImpl with MajorPlanCourseService {

  private var planCourseCommonDao: PlanCourseCommonDao = _

  def addPlanCourse(planCourse: MajorPlanCourse, plan: MajorPlan) {
    planCourseCommonDao.addPlanCourse(planCourse, plan)
  }

  def removePlanCourse(planCourse: MajorPlanCourse, plan: MajorPlan) {
    planCourseCommonDao.removePlanCourse(planCourse, plan)
  }

  def updatePlanCourse(planCourse: MajorPlanCourse, plan: MajorPlan) {
    planCourseCommonDao.updatePlanCourse(planCourse, plan)
  }

  def setPlanCourseCommonDao(planCourseCommonDao: PlanCourseCommonDao) {
    this.planCourseCommonDao = planCourseCommonDao
  }

  def getMajorPlanCourseDwr(id: java.lang.Long): MajorPlanCourse = {
    entityDao.get(classOf[MajorPlanCourse], id)
  }
}
