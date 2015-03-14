package org.openurp.edu.eams.teach.planaudit.service.listeners

import java.util.Collection
import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.planaudit.CourseAuditResult
import org.openurp.edu.eams.teach.planaudit.GroupAuditResult
import org.openurp.edu.eams.teach.planaudit.PlanAuditResult
import org.openurp.edu.eams.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.eams.teach.planaudit.model.GroupAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.planaudit.service.StdGrade
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.GroupRelation
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.model.ExpressionGroupRelation

import scala.collection.JavaConversions._

class PlanAuditCommonElectiveListener extends PlanAuditListener {

  def endPlanAudit(context: PlanAuditContext) {
    val result = context.getResult
    val stdGrade = context.getStdGrade
    val electiveType = context.getStandard.getConvertTargetCourseType
    if (null == electiveType) return
    var groupResult = result.getGroupResult(electiveType)
    if (null == groupResult) {
      val groupRs = new GroupAuditResultBean()
      groupRs.setCourseType(electiveType)
      groupRs.setName(electiveType.getName)
      val groupRelation = new ExpressionGroupRelation()
      groupRelation.setRelation(ExpressionGroupRelation.AND)
      groupRs.setRelation(groupRelation)
      groupResult = groupRs
      result.addGroupResult(groupResult)
    }
    val restCourses = stdGrade.getRestCourses
    for (course <- restCourses) {
      val courseResult = new CourseAuditResultBean()
      courseResult.setCourse(course)
      val grades = stdGrade.useGrades(course)
      if (!grades.isEmpty && 
        grades.get(0).getCourseType.getId != electiveType.getId) {
        courseResult.setRemark("计划外")
      }
      courseResult.checkPassed(grades)
      groupResult.addCourseResult(courseResult)
    }
    processConvertCredits(groupResult, result, context)
    groupResult.checkPassed(true)
  }

  protected def processConvertCredits(target: GroupAuditResult, result: PlanAuditResult, context: PlanAuditContext) {
    val parents = CollectUtils.newHashSet()
    val sibling = CollectUtils.newHashSet()
    var start = target.getParent
    while (null != start && !parents.contains(start)) {
      parents.add(start)
      start = start.getParent
    }
    val parent = target.getParent
    if (null != parent) {
      sibling.addAll(parent.getChildren)
      sibling.remove(target)
    }
    var otherConverted = 0f
    var siblingConverted = 0f
    for (gr <- result.getGroupResults) {
      if (!context.getStandard.isConvertable(gr.getCourseType)) //continue
      if (gr == target || parents.contains(gr)) //continue
      if (sibling.contains(gr)) {
        siblingConverted += if (gr.isPassed) gr.getAuditStat.getCreditsCompleted - gr.getAuditStat.getCreditsRequired else 0f
      } else if (null == gr.getParent) {
        otherConverted += if (gr.isPassed) gr.getAuditStat.getCreditsCompleted - gr.getAuditStat.getCreditsRequired else 0f
      }
    }
    target.getAuditStat.setCreditsConverted(otherConverted + siblingConverted)
    for (r <- parents) r.getAuditStat.setCreditsConverted(otherConverted)
  }

  def startPlanAudit(context: PlanAuditContext): Boolean = true

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    true
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    true
  }
}
