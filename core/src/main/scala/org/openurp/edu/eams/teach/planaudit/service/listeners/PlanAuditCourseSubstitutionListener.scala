package org.openurp.edu.eams.teach.planaudit.service.listeners


import org.beangle.commons.collection.CollectUtils
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

  private def getGroupKey(courseGroup: CourseGroup): String = courseGroup.getName + "c"

  def startPlanAudit(context: PlanAuditContext): Boolean = {
    context.getParams.put(substitutions_str, courseSubstitutionService.getCourseSubstitutions(context.getResult.getStd))
    true
  }

  def endPlanAudit(context: PlanAuditContext) {
  }

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    val substituted = context.getParams.get(getGroupKey(planCourse.getCourseGroup)).asInstanceOf[Set[_]]
    !(substituted.contains(planCourse.getCourse))
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    var substituted = context.getParams.get(getGroupKey(courseGroup)).asInstanceOf[Set[Course]]
    if (null == substituted) {
      substituted = CollectUtils.newHashSet()
      context.getParams.put(getGroupKey(courseGroup), substituted)
    }
    val substitutions = context.getParams.get(substitutions_str).asInstanceOf[List[CourseSubstitution]]
    val stdGrade = context.getStdGrade
    val courseMap = CollectUtils.newHashMap()
    for (planCourse <- courseGroup.getPlanCourses) {
      courseMap.put(planCourse.getCourse, planCourse)
    }
    val auditTerms = context.getAuditTerms
    val needCheckTerm = (null != auditTerms && auditTerms.length >= 0)
    for (sc <- substitutions if courseMap.keySet.containsAll(sc.getOrigins) && isSubstitutes(stdGrade, 
      sc)) {
      val substituteGrades = CollectUtils.newArrayList()
      for (c <- sc.getSubstitutes) {
        substituteGrades.addAll(stdGrade.grades(c))
        stdGrade.addNoGradeCourse(c)
      }
      for (ori <- sc.getOrigins) {
        val planCourse = courseMap.get(ori).asInstanceOf[PlanCourse]
        if (needCheckTerm) {
          var inTerm = false
          for (j <- 0 until auditTerms.length) {
            inTerm = TermCalculator.inTerm(planCourse.getTerms, java.lang.Integer.valueOf(auditTerms(j)))
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
          tempStr.append(grade.getCourse.getName).append('[')
            .append(grade.getCourse.getCode)
            .append(']')
          if (iter.hasNext) {
            tempStr.append(',')
          }
        }
        planCourseResult.setRemark(tempStr.toString)
        groupResult.addCourseResult(planCourseResult)
        groupResult.checkPassed(false)
        substituted.add(ori)
      }
    }
    true
  }

  protected def isSubstitutes(stdGrade: StdGrade, substitution: CourseSubstitution): Boolean = {
    val allCourses = CollectUtils.newHashSet(substitution.getOrigins)
    allCourses.addAll(substitution.getSubstitutes)
    val subGrades = CollectUtils.newHashMap()
    for (course <- allCourses) {
      val grades = stdGrade.grades(course)
      if (CollectUtils.isNotEmpty(grades)) subGrades.put(course, grades.get(0))
    }
    if (GradeComparator.isSubstitute(substitution, subGrades)) {
      for (course <- allCourses) stdGrade.useGrades(course)
      return true
    }
    false
  }
}
