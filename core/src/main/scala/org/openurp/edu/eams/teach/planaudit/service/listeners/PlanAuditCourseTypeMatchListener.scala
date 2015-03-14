package org.openurp.edu.eams.teach.planaudit.service.listeners

import java.util.Collection
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.exchange.ExchangeCourse
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.planaudit.CourseAuditResult
import org.openurp.edu.eams.teach.planaudit.GroupAuditResult
import org.openurp.edu.eams.teach.planaudit.PlanAuditResult
import org.openurp.edu.eams.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.eams.teach.planaudit.model.PlanAuditStandard
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.planaudit.service.StdGrade
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.time.util.TermCalculator

import scala.collection.JavaConversions._

class PlanAuditCourseTypeMatchListener extends PlanAuditListener {

  protected def addGroupResult(results: Map[CourseType, GroupAuditResult], gr: GroupAuditResult) {
    results.put(gr.getCourseType, gr)
    for (child <- gr.getChildren) {
      addGroupResult(results, child)
    }
  }

  private var semesterService: SemesterService = _

  def endPlanAudit(context: PlanAuditContext) {
    val auditTerms = context.getAuditTerms
    val results = CollectUtils.newHashMap()
    val stdGrade = context.getStdGrade
    val restCourses = stdGrade.getRestCourses
    if (!restCourses.isEmpty) {
      val result = context.getResult
      for (gr <- result.getGroupResults) {
        addGroupResult(results, gr)
      }
    }
    for (course <- restCourses) {
      val grades = stdGrade.grades(course)
      var courseType: CourseType = null
      if (grades.isEmpty) //continue else courseType = grades.get(0).getCourseType
      val groupResult = results.get(courseType)
      if (null == groupResult) //continue
      val g = context.getCoursePlan.getGroup(groupResult.getCourseType)
      if (null != g && g.isCompulsory) //continue
      stdGrade.useGrades(course)
      val remark = new StringBuilder()
      if (course.isExchange) {
        for (grade <- grades; ec <- grade.getExchanges) remark.append(ec.getName).append(' ')
      }
      if (null != auditTerms && auditTerms.length > 0 && context.getCoursePlan != null) {
        var inAuditTerms = false
        for (grade <- grades) {
          if (inAuditTerms) //break
          var term = -1
          term = if (context.getCoursePlan.getInvalidOn != null) new TermCalculator(semesterService, 
            grade.getSemester)
            .getTerm(context.getCoursePlan.getInvalidOn, context.getCoursePlan.getInvalidOn, true) else new TermCalculator(semesterService, 
            grade.getSemester)
            .getTerm(context.getCoursePlan.getEffectiveOn, java.sql.Date.valueOf("2099-09-09"), true)
          for (j <- 0 until auditTerms.length if String.valueOf(term) == auditTerms(j)) {
            inAuditTerms = true
            //break
          }
        }
        if (!inAuditTerms) //continue
      }
      val courseGroup = context.getCoursePlan.getGroup(courseType)
      var outOfPlan = false
      if (!CollectUtils.isEmpty(courseGroup.getPlanCourses)) {
        outOfPlan = true
      }
      var existResult: CourseAuditResult = null
      var existed = false
      for (cr <- groupResult.getCourseResults if cr.getCourse == course) {
        existResult = cr
        existed = true
        //break
      }
      if (existResult == null) existResult = new CourseAuditResultBean()
      existResult.setCourse(course)
      existResult.checkPassed(grades)
      if (null != existResult.getRemark) remark.insert(0, existResult.getRemark)
      if (outOfPlan) remark.append(" 计划外")
      existResult.setRemark(remark.toString)
      if (!existed) groupResult.addCourseResult(existResult)
      groupResult.checkPassed(true)
    }
  }

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    true
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    val standard = context.getStandard
    if (null != standard) {
      return !standard.isDisaudit(courseGroup.getCourseType)
    }
    true
  }

  def startPlanAudit(context: PlanAuditContext): Boolean = true

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }
}
