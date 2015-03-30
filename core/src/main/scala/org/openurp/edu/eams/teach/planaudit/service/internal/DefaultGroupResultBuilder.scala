package org.openurp.edu.eams.teach.planaudit.service.internal

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Course
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.model.GroupAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator

class DefaultGroupResultBuilder extends GroupResultBuilder {

  def buildResult(context: PlanAuditContext, group: CourseGroup): GroupAuditResult = {
    val result = new GroupAuditResultBean()
    var creditsRequired = group.credits
    if (context.auditTerms != null && context.auditTerms.length != 0) {
      creditsRequired = 0
      var groupCourseCredits = 0f
      var creditsNeedCompare = false
      val auditedCourses = Collections.newSet[Any]
      for (i <- 0 until context.auditTerms.length) {
        val term = java.lang.Integer.valueOf(context.auditTerms(i))
        creditsRequired += PlanUtils.getGroupCredits(group, term)
        if (group.children.isEmpty && !group.planCourses.isEmpty && group.compulsory) {
          creditsNeedCompare = true
          for (
            planCourse <- group.planCourses if !auditedCourses.contains(planCourse.course) && Strings.isNotEmpty(planCourse.terms) &&
              TermCalculator.inTerm(planCourse.terms, term)
          ) {
            groupCourseCredits += planCourse.course.credits
            auditedCourses.add(planCourse.course)
          }
        }
      }
      if (creditsNeedCompare) {
        creditsRequired = if (java.lang.Float.compare(creditsRequired, groupCourseCredits) <
          0) creditsRequired else groupCourseCredits
      }
    }
    result.auditStat.creditsRequired = creditsRequired
    if (context.partial) {
      result.auditStat.numRequired = 0
    } else {
      result.auditStat.numRequired = group.courseNum
    }
    result.courseType = group.courseType
    result.name = group.name
    result.groupNum = group.groupNum
    result.planResult = context.result
    result
  }
}
