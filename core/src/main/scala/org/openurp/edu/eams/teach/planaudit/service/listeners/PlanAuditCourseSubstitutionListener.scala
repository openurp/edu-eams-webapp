package org.openurp.edu.eams.teach.planaudit.service.listeners


import org.beangle.commons.collection.Collections
import org.openurp.edu.base.Course
import org.openurp.edu.teach.grade.domain.impl.GradeComparator
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.planaudit.CourseAuditResult
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.planaudit.service.StdGrade
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CourseSubstitution
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.service.CourseSubstitutionService
import org.openurp.edu.eams.teach.time.util.TermCalculator
import PlanAuditCourseSubstitutionListener._




object PlanAuditCourseSubstitutionListener {

  private val substitutions_str = "substitutions"
}

class PlanAuditCourseSubstitutionListener extends PlanAuditListener {

  
  var courseSubstitutionService: CourseSubstitutionService = _

  private def getGroupKey(courseGroup: CourseGroup): String = courseGroup.name + "c"

  def startPlanAudit(context: PlanAuditContext): Boolean = {
    context.params.put(substitutions_str, courseSubstitutionService.courseSubstitutions(context.result.std))
    true
  }

  def endPlanAudit(context: PlanAuditContext) {
  }

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    val substituted = context.params.get(getGroupKey(planCourse.courseGroup)).asInstanceOf[Set[_]]
    !(substituted.contains(planCourse.course))
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    var substituted = context.params.get(getGroupKey(courseGroup)).asInstanceOf[Set[Course]]
    if (null == substituted) {
      substituted = Collections.newSet[Any]
      context.params.put(getGroupKey(courseGroup), substituted)
    }
    val substitutions = context.params.get(substitutions_str).asInstanceOf[List[CourseSubstitution]]
    val stdGrade = context.stdGrade
    val courseMap = Collections.newMap[Any]
    for (planCourse <- courseGroup.planCourses) {
      courseMap.put(planCourse.course, planCourse)
    }
    val auditTerms = context.auditTerms
    val needCheckTerm = (null != auditTerms && auditTerms.length >= 0)
    for (sc <- substitutions if courseMap.keySet.containsAll(sc.origins) && isSubstitutes(stdGrade, 
      sc)) {
      val substituteGrades = Collections.newBuffer[Any]
      for (c <- sc.substitutes) {
        substituteGrades.addAll(stdGrade.grades(c))
        stdGrade.addNoGradeCourse(c)
      }
      for (ori <- sc.origins) {
        val planCourse = courseMap.get(ori).asInstanceOf[PlanCourse]
        if (needCheckTerm) {
          var inTerm = false
          for (j <- 0 until auditTerms.length) {
            inTerm = TermCalculator.inTerm(planCourse.terms, java.lang.Integer.valueOf(auditTerms(j)))
            if (inTerm) //break
          }
          if (!inTerm) //continue
        }
        val planCourseResult = new CourseAuditResultBean(planCourse)
        planCourseResult.checkPassed(stdGrade.grades(ori), substituteGrades)
        val tempStr = new StringBuffer()
        var iter = substituteGrades.iterator()
        while (iter.hasNext) {
          val grade = iter.next()
          tempStr.append(grade.course.name).append('[')
            .append(grade.course.code)
            .append(']')
          if (iter.hasNext) {
            tempStr.append(',')
          }
        }
        planCourseResult.remark=tempStr.toString
        groupResult.addCourseResult(planCourseResult)
        groupResult.checkPassed(false)
        substituted.add(ori)
      }
    }
    true
  }

  protected def isSubstitutes(stdGrade: StdGrade, substitution: CourseSubstitution): Boolean = {
    val allCourses = Collections.newHashSet(substitution.origins)
    allCourses.addAll(substitution.substitutes)
    val subGrades = Collections.newMap[Any]
    for (course <- allCourses) {
      val grades = stdGrade.grades(course)
      if (Collections.isNotEmpty(grades)) subGrades.put(course, grades.get(0))
    }
    if (GradeComparator.isSubstitute(substitution, subGrades)) {
      for (course <- allCourses) stdGrade.useGrades(course)
      return true
    }
    false
  }
}
