package org.openurp.edu.eams.teach.program.common.dao.impl


import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import com.ekingstar.eams.teach.Course
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.common.helper.ProgramHibernateClassGetter
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
//remove if not needed


class PlanCourseCommonDaoHibernate extends HibernateEntityDao with PlanCourseCommonDao {

  private var planCommonDao: PlanCommonDao = _

  private var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  def addPlanCourse(planCourse: PlanCourse, plan: CoursePlan) {
    val myGroup = get(ProgramHibernateClassGetter.hibernateClass(planCourse.getCourseGroup), planCourse.getCourseGroup.id)
    myGroup.addPlanCourse(planCourse)
    saveOrUpdate(planCourse)
    saveOrUpdate(myGroup)
    planCourse.setCourse(get(classOf[Course], planCourse.getCourse.id))
    planCourseGroupCommonDao.updateGroupTreeCredits(planCourseGroupCommonDao.getTopGroup(myGroup))
    plan.setCredits(planCommonDao.statPlanCredits(plan))
    saveOrUpdate(plan)
  }

  def removePlanCourse(planCourse: PlanCourse, plan: CoursePlan) {
    val myGroup = planCourse.getCourseGroup
    myGroup.getPlanCourses.remove(planCourse)
    planCourse.setCourseGroup(null)
    remove(planCourse)
    planCourseGroupCommonDao.updateGroupTreeCredits(planCourseGroupCommonDao.getTopGroup(myGroup))
    plan.setCredits(planCommonDao.statPlanCredits(plan))
    saveOrUpdate(plan)
  }

  def updatePlanCourse(planCourse: PlanCourse, plan: CoursePlan) {
    val group = planCourse.getCourseGroup
    saveOrUpdate(planCourse)
    saveOrUpdate(group)
    planCourseGroupCommonDao.updateGroupTreeCredits(planCourseGroupCommonDao.getTopGroup(group))
    plan.setCredits(planCommonDao.statPlanCredits(plan))
    saveOrUpdate(plan)
  }

  def getMajorPlanCourseByCourse(majorPlan: MajorPlan, course: Course): MajorPlanCourse = {
    val query = OqlBuilder.from(ProgramHibernateClassGetter.hibernateClass(majorPlan), "plan")
    query.select("planCourse").join("plan.groups", "cgroup")
      .join("cgroup.planCourses", "planCourse")
      .where("planCourse.course=:course", course)
      .where("plan.id = :planId", majorPlan.id)
    val courses = search(query)
    if (null == courses || courses.size == 0) {
      return null
    }
    courses.get(0)
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }
}
