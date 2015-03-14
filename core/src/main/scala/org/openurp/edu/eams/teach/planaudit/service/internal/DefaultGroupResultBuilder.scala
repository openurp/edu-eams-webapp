package org.openurp.edu.eams.teach.planaudit.service.internal

import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.planaudit.GroupAuditResult
import org.openurp.edu.eams.teach.planaudit.model.GroupAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator

import scala.collection.JavaConversions._

class DefaultGroupResultBuilder extends GroupResultBuilder {

  def buildResult(context: PlanAuditContext, group: CourseGroup): GroupAuditResult = {
    val result = new GroupAuditResultBean()
    var creditsRequired = group.getCredits
    if (context.getAuditTerms != null && context.getAuditTerms.length != 0) {
      creditsRequired = 0
      var groupCourseCredits = 0f
      var creditsNeedCompare = false
      val auditedCourses = CollectUtils.newHashSet()
      for (i <- 0 until context.getAuditTerms.length) {
        val term = java.lang.Integer.valueOf(context.getAuditTerms()(i))
        creditsRequired += PlanUtils.getGroupCredits(group, term)
        if (group.getChildren.isEmpty && !group.getPlanCourses.isEmpty && 
          group.isCompulsory) {
          creditsNeedCompare = true
          for (planCourse <- group.getPlanCourses if !auditedCourses.contains(planCourse.getCourse) && Strings.isNotEmpty(planCourse.getTerms) && 
            TermCalculator.inTerm(planCourse.getTerms, term)) {
            groupCourseCredits += planCourse.getCourse.getCredits
            auditedCourses.add(planCourse.getCourse)
          }
        }
      }
      if (creditsNeedCompare) {
        creditsRequired = if (java.lang.Float.compare(creditsRequired, groupCourseCredits) < 
          0) creditsRequired else groupCourseCredits
      }
    }
    result.getAuditStat.setCreditsRequired(creditsRequired)
    if (context.isPartial) {
      result.getAuditStat.setNumRequired(0)
    } else {
      result.getAuditStat.setNumRequired(group.getCourseNum)
    }
    result.setCourseType(group.getCourseType)
    result.setName(group.getName)
    result.setRelation(group.getRelation)
    result.setPlanResult(context.getResult)
    result
  }
}
