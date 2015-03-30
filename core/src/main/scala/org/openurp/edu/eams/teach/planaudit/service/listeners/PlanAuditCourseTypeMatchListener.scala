package org.openurp.edu.eams.teach.planaudit.service.listeners

import org.beangle.commons.collection.Collections
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType
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

  protected def addGroupResult(results: collection.mutable.Map[CourseType, GroupAuditResult], gr: GroupAuditResult) {
    results.put(gr.courseType, gr)
    for (child <- gr.children) {
      addGroupResult(results, child)
    }
  }

  var semesterService: SemesterService = _

  def endPlanAudit(context: PlanAuditContext) {
    val auditTerms = context.auditTerms
    val results = Collections.newMap[CourseType, GroupAuditResult]
    val stdGrade = context.stdGrade
    val restCourses = stdGrade.getRestCourses
    if (!restCourses.isEmpty) {
      val result = context.result
      for (gr <- result.groupResults) {
        addGroupResult(results, gr)
      }
    }

    for (course <- restCourses if !stdGrade.getGrades(course).isEmpty) {
      val grades = stdGrade.getGrades(course)
      val courseType = grades(0).courseType
      val groupResult = results.get(courseType).orNull
      var auditable = true
      if (null == groupResult) auditable = false
      else {
        val g = context.coursePlan.group(groupResult.courseType)
        if (null != g && g.compulsory) auditable = false
      }
      if (null != auditTerms && auditTerms.length > 0 && context.coursePlan != null) {
        var inAuditTerms = false
        for (grade <- grades) {
          val term = if (context.coursePlan.endOn != null) {
            new TermCalculator(semesterService, grade.semester).getTerm(context.coursePlan.beginOn, context.coursePlan.endOn, true)
          } else {
            new TermCalculator(semesterService, grade.semester)
              .getTerm(context.coursePlan.beginOn, java.sql.Date.valueOf("2099-09-09"), true)
          }
          inAuditTerms = auditTerms.exists { t => String.valueOf(term) == t }
        }
        if (!inAuditTerms) auditable = false
      }

      if (auditable) {
        stdGrade.useGrades(course)
        val remark = new StringBuilder()
        //      if (course.isExchange) {
        //        for (grade <- grades; ec <- grade.exchanges) remark.append(ec.name).append(' ')
        //      }
        val courseGroup = context.coursePlan.group(courseType)
        var outOfPlan = false
        if (!Collections.isEmpty(courseGroup.planCourses)) {
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
        existResult.course = course
        existResult.checkPassed(grades)
        if (null != existResult.remark) remark.insert(0, existResult.remark)
        if (outOfPlan) remark.append(" 计划外")
        existResult.remark = remark.toString
        if (!existed) groupResult.addCourseResult(existResult)
        groupResult.checkPassed(true)
      }
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
}
