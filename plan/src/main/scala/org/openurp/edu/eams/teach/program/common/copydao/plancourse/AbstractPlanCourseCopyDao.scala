package org.openurp.edu.eams.teach.program.common.copydao.plancourse



import org.apache.commons.beanutils.PropertyUtils
import org.beangle.commons.lang.Throwables
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.helper.PlanTermCreditTool
//remove if not needed


abstract class AbstractPlanCourseCopyDao extends HibernateEntityDao with IPlanCourseCopyDao {

  def copyPlanCourses(sourcePlanCourses: List[_ <: PlanCourse], courseGroupAttachTo: CourseGroup): List[_ <: PlanCourse] = {
    val res = new ArrayList[PlanCourse]()
    for (planCourse <- sourcePlanCourses) {
      res.add(copyPlanCourse(planCourse, courseGroupAttachTo))
    }
    res
  }

  def copyPlanCourse(sourcePlanCourse: PlanCourse, courseGroupAttachTo: CourseGroup): PlanCourse = {
    val clonePlanCourse = newPlanCourse()
    commonSetting(clonePlanCourse, sourcePlanCourse)
    courseGroupAttachTo.addPlanCourse(clonePlanCourse)
    normalizeTerm(clonePlanCourse, sourcePlanCourse.getCourseGroup.getPlan.getTermsCount, courseGroupAttachTo.getPlan.getTermsCount)
    saveOrUpdate(courseGroupAttachTo)
    clonePlanCourse
  }

  protected def newPlanCourse(): PlanCourse

  private def normalizeTerm(planCourse: PlanCourse, oldTermsCount: Int, newTermsCount: Int) {
    val terms = PlanTermCreditTool.buildPlanCourseTerms(planCourse.getTerms, oldTermsCount, newTermsCount)
    planCourse.setTerms(PlanTermCreditTool.normalizeTerms(terms))
  }

  private def commonSetting(copy: PlanCourse, sourcePlanCourse: PlanCourse) {
    PropertyUtils.copyProperties(copy, sourcePlanCourse)
    copy.setCourseGroup(null)
    copy.setId(null)
  }
}
