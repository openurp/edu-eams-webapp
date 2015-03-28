package org.openurp.edu.eams.teach.program.common.dao.impl



import org.apache.commons.lang3.Range
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.ems.dictionary.service.BaseCodeService
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.common.helper.ProgramHibernateClassGetter
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.util.PlanUtils
//remove if not needed


class PlanCommonDaoHibernate extends HibernateEntityDao with PlanCommonDao {

  private var baseCodeService: BaseCodeService = _

  def removePlan(plan: CoursePlan) {
    remove(get(ProgramHibernateClassGetter.hibernateClass(plan), plan.id))
  }

  def saveOrUpdatePlan(plan: CoursePlan) {
    saveSetting(plan)
    saveOrUpdate(plan)
    plan.setCredits(statPlanCredits(plan))
    saveOrUpdate(plan)
  }

  protected def saveSetting(plan: CoursePlan) {
    if (plan.isInstanceOf[MajorPlan]) {
      val mplan = plan.asInstanceOf[MajorPlan]
      if (null == mplan.getProgram.getAuditState) {
        mplan.getProgram.setAuditState(CommonAuditState.UNSUBMITTED)
      }
    } else if (plan.isInstanceOf[PersonalPlan]) {
      val pplan = plan.asInstanceOf[PersonalPlan]
      pplan.setAuditState(CommonAuditState.UNSUBMITTED)
    }
  }

  def statPlanCredits(plan: CoursePlan): Float = {
    var totalCredits = 0
    if (plan.getTopCourseGroups != null) {
      for (group <- plan.getTopCourseGroups) {
        totalCredits += group.getCredits
      }
    }
    totalCredits
  }

  def hasCourse(cgroup: CourseGroup, course: Course): Boolean = hasCourse(cgroup, course, null)

  def hasCourse(cgroup: CourseGroup, course: Course, planCourse: PlanCourse): Boolean = {
    val query = OqlBuilder.from(ProgramHibernateClassGetter.hibernateClass(cgroup), "cGroup")
    query.select("select distinct planCourse").join("cGroup.planCourses", "planCourse")
      .where("cGroup.id = :cGroupId", cgroup.id)
      .where("planCourse.course = :course", course)
    if (planCourse != null) {
      query.where("planCourse <> :planCourse", planCourse)
    }
    val res = search(query)
    !res.isEmpty
  }

  def getUsedCourseTypes(plan: CoursePlan): List[CourseType] = {
    val query = OqlBuilder.from(ProgramHibernateClassGetter.hibernateClass(plan), "plan")
    query.select("courseGroup.courseType").join("plan.groups", "courseGroup")
      .where("plan.id=:planId", plan.id)
    search(query).asInstanceOf[List[CourseType]]
  }

  def getUnusedCourseTypes(plan: CoursePlan): List[CourseType] = {
    val allCourseTypes = baseCodeService.getCodes(classOf[CourseType])
    val usedCourseTypes = getUsedCourseTypes(plan)
    val list = new ArrayList[CourseType](allCourseTypes)
    for (courseType <- usedCourseTypes) {
      list.remove(courseType)
    }
    list
  }

  def getDuplicatePrograms(program: Program): List[Program] = {
    val query = OqlBuilder.from(classOf[Program], "program")
    if (program.isPersisted) {
      query.where("program.id <> :me", program.id)
    }
    query.where("program.grade = :grade", program.getGrade)
    query.where("program.education.id = :educationId", program.getEducation.id)
    query.where("program.stdType.id = :stdTypeId", program.getStdType.id)
    query.where("program.department.id = :departmentId", program.getDepartment.id)
    query.where("program.major.id = :majorId", program.getMajor.id)
    if (program.getDirection != null && program.getDirection.id != null) {
      query.where("program.direction.id = :directionId", program.getDirection.id)
    } else {
      query.where("program.direction is null")
    }
    search(query)
  }

  def isDuplicate(program: Program): Boolean = {
    Collections.isNotEmpty(getDuplicatePrograms(program))
  }

  def getCreditByTerm(plan: MajorPlan, term: Int): java.lang.Float = {
    val termRange = Range.between(1, plan.getTermsCount.intValue())
    if (!termRange.contains(term)) {
      throw new RuntimeException("term out range")
    } else {
      var credit = 0
      for (group <- plan.getGroups) {
        credit += PlanUtils.getGroupCredits(group, term)
      }
      new java.lang.Float(credit)
    }
  }

  def setBaseCodeService(baseCodeService: BaseCodeService) {
    this.baseCodeService = baseCodeService
  }
}
