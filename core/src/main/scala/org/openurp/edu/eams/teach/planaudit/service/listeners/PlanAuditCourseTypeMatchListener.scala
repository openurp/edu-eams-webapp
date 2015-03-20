package org.openurp.edu.eams.teach.planaudit.service.listeners




import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.exchange.ExchangeCourse
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.planaudit.CourseAuditResult
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.PlanAuditResult
import org.openurp.edu.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.teach.planaudit.model.PlanAuditStandard
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.planaudit.service.StdGrade
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.time.util.TermCalculator



class PlanAuditCourseTypeMatchListener extends PlanAuditListener {

  protected def addGroupResult(results: Map[CourseType, GroupAuditResult], gr: GroupAuditResult) {
    results.put(gr.courseType, gr)
    for (child <- gr.children) {
      addGroupResult(results, child)
    }
  }

  private var semesterService: SemesterService = _

  def endPlanAudit(context: PlanAuditContext) {
    val auditTerms = context.auditTerms
    val results = CollectUtils.newHashMap()
    val stdGrade = context.stdGrade
    val restCourses = stdGrade.restCourses
    if (!restCourses.isEmpty) {
      val result = context.result
      for (gr <- result.groupResults) {
        addGroupResult(results, gr)
      }
    }
    for (course <- restCourses) {
      val grades = stdGrade.grades(course)
      var courseType: CourseType = null
      if (grades.isEmpty) //continue else courseType = grades.get(0).courseType
      val groupResult = results.get(courseType)
      if (null == groupResult) //continue
      val g = context.coursePlan.group(groupResult.courseType)
      if (null != g && g.isCompulsory) //continue
      stdGrade.useGrades(course)
      val remark = new StringBuilder()
      if (course.isExchange) {
        for (grade <- grades; ec <- grade.exchanges) remark.append(ec.name).append(' ')
      }
      if (null != auditTerms && auditTerms.length > 0 && context.coursePlan != null) {
        var inAuditTerms = false
        for (grade <- grades) {
          if (inAuditTerms) //break
          var term = -1
          term = if (context.coursePlan.invalidOn != null) new TermCalculator(semesterService, 
            grade.semester)
            .term(context.coursePlan.invalidOn, context.coursePlan.invalidOn, true) else new TermCalculator(semesterService, 
            grade.semester)
            .term(context.coursePlan.effectiveOn, java.sql.Date.valueOf("2099-09-09"), true)
          for (j <- 0 until auditTerms.length if String.valueOf(term) == auditTerms(j)) {
            inAuditTerms = true
            //break
          }
        }
        if (!inAuditTerms) //continue
      }
      val courseGroup = context.coursePlan.group(courseType)
      var outOfPlan = false
      if (!CollectUtils.isEmpty(courseGroup.planCourses)) {
        outOfPlan = true
      }
      var existResult: CourseAuditResult = null
      var existed = false
      for (cr <- groupResult.courseResults if cr.course == course) {
        existResult = cr
        existed = true
        //break
      }
      if (existResult == null) existResult = new CourseAuditResultBean()
      existResult.course=course
      existResult.checkPassed(grades)
      if (null != existResult.remark) remark.insert(0, existResult.remark)
      if (outOfPlan) remark.append(" 计划外")
      existResult.remark=remark.toString
      if (!existed) groupResult.addCourseResult(existResult)
      groupResult.checkPassed(true)
    }
  }

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    true
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    val standard = context.standard
    if (null != standard) {
      return !standard.isDisaudit(courseGroup.courseType)
    }
    true
  }

  def startPlanAudit(context: PlanAuditContext): Boolean = true

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }
}
