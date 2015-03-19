package org.openurp.edu.eams.teach.program.major.dao.hibernate


import org.apache.commons.lang3.Range
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import com.ekingstar.eams.core.Major
import com.ekingstar.eams.core.code.industry.Education
import com.ekingstar.eams.core.service.SemesterService
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.dao.MajorCourseGroupDao
import org.openurp.edu.eams.teach.program.major.dao.MajorPlanDao
import com.ekingstar.eams.util.DataRealmLimit
//remove if not needed


class MajorPlanDaoHibernate extends HibernateEntityDao with MajorPlanDao {

  private var planCommonDao: PlanCommonDao = _

  def getMajorPlanList(grade: String, major: Major, level: Education): List[MajorPlan] = {
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.where("plan.program.grade=:grade", grade).where("plan.program.major=:major", major)
      .where("plan.program.education=:education", level)
    search(query)
  }

  protected var semesterService: SemesterService = _

  protected var MajorCourseGroupDao: MajorCourseGroupDao = _

  protected def getMajorPlan(id: java.lang.Long): MajorPlan = get(classOf[MajorPlan], id)

  def getMajorPlans(planIds: Array[Long]): List[MajorPlan] = {
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.where("plan.id in (:ids)", planIds)
    search(query)
  }

  def getCreditByTerm(plan: MajorPlan, term: Int): java.lang.Float = {
    val termRange = Range.between(1, plan.getTermsCount.intValue())
    if (!termRange.contains(term)) {
      throw new RuntimeException("term out range")
    } else {
      null
    }
  }

  def addDataRealmLimt(criteria: Criteria, attr: Array[String], limit: DataRealmLimit) {
    if (null == limit || null == attr || null == limit.getDataRealm) {
      return
    }
    if (attr.length > 0) {
      if (Strings.isNotEmpty(limit.getDataRealm.getStudentTypeIdSeq) && 
        Strings.isNotEmpty(attr(0))) {
        criteria.add(Restrictions.in(attr(0), Strings.splitToLong(limit.getDataRealm.getStudentTypeIdSeq)))
      }
    }
    if (attr.length > 1) {
      if (Strings.isNotEmpty(limit.getDataRealm.getDepartmentIdSeq) && 
        Strings.isNotEmpty(attr(1))) {
        criteria.add(Restrictions.in(attr(1), Strings.splitToLong(limit.getDataRealm.getDepartmentIdSeq)))
      }
    }
  }

  def setMajorCourseGroupDao(MajorCourseGroupDao: MajorCourseGroupDao) {
    this.MajorCourseGroupDao = MajorCourseGroupDao
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }
}
