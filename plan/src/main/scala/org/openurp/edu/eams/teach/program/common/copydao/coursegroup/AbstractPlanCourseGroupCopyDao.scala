package org.openurp.edu.eams.teach.program.common.copydao.coursegroup


import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.common.copydao.plancourse.IPlanCourseCopyDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.helper.PlanTermCreditTool
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


abstract class AbstractPlanCourseGroupCopyDao extends HibernateEntityDao with IPlanCourseGroupCopyDao {

  protected var planCourseCopyDao: IPlanCourseCopyDao = _

  protected var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  def newCourseGroup(sourceCourseGroup: CourseGroup): CourseGroup

  def copyCourseGroup(sourceCourseGroup: CourseGroup, parentAttachTo: CourseGroup, planAttachTo: CoursePlan): CourseGroup = {
    val cloneGroup = newCourseGroup(sourceCourseGroup)
    commonSetting(cloneGroup, sourceCourseGroup)
    normalizeTerms(cloneGroup, planAttachTo.getTermsCount)
    if (parentAttachTo == null) {
      planCourseGroupCommonDao.addCourseGroupToPlan(cloneGroup, planAttachTo)
    } else {
      if (parentAttachTo.getPlan.id != planAttachTo.id) {
        throw new RuntimeException("parentAttachTo.coursePlan must be same with planAttachTo")
      }
      planCourseGroupCommonDao.addCourseGroupToPlan(cloneGroup, parentAttachTo, planAttachTo)
    }
    planCourseCopyDao.copyPlanCourses(sourceCourseGroup.getPlanCourses.asInstanceOf[List[_]], cloneGroup)
    for (child <- sourceCourseGroup.getChildren) {
      copyCourseGroup(child.asInstanceOf[MajorCourseGroup], cloneGroup, planAttachTo)
    }
    saveOrUpdate(cloneGroup)
    cloneGroup
  }

  private def normalizeTerms(group: CourseGroup, newTermsCount: java.lang.Integer) {
    val creditsTerms = PlanTermCreditTool.transformToFloat(group.getTermCredits)
    val oldTermsCount = creditsTerms.length
    val newCreditPerTerms = PlanTermCreditTool.buildCourseGroupTermCredits(creditsTerms, oldTermsCount, 
      newTermsCount)
    group.setTermCredits(newCreditPerTerms)
  }

  private def commonSetting(newGroup: CourseGroup, sourceCourseGroup: CourseGroup) {
    newGroup.setRelation(sourceCourseGroup.getRelation)
    newGroup.setCourseType(sourceCourseGroup.getCourseType)
    newGroup.setCredits(sourceCourseGroup.getCredits)
    newGroup.setTermCredits(sourceCourseGroup.getTermCredits)
    newGroup.setRemark(sourceCourseGroup.getRemark)
    newGroup.setCourseNum(sourceCourseGroup.getCourseNum)
    newGroup.setIndexno(sourceCourseGroup.getIndexno)
  }

  def setPlanCourseCopyDao(planCourseCopyDao: IPlanCourseCopyDao) {
    this.planCourseCopyDao = planCourseCopyDao
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }
}
