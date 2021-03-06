package org.openurp.edu.eams.teach.planaudit.service.observers

import java.util.Date

import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.planaudit.CourseAuditResult
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.PlanAuditResult
import org.openurp.edu.teach.planaudit.adapters.GroupResultAdapter
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext

class PlanAuditPersistObserver extends BaseServiceImpl with PlanAuditObserver {

  def notifyStart() {
  }

  def finish() {
  }

  def notifyBegin(context: PlanAuditContext, index: Int): Boolean = true

  private def getResult(std: Student): PlanAuditResult = {
    val query = OqlBuilder.from(classOf[PlanAuditResult], "planResult")
    query.where("planResult.std = :std", std)
    val results = entityDao.search(query)
    if (results.size > 0) {
      return results(0)
    }
    null
  }

  def notifyEnd(context: PlanAuditContext, index: Int) {
    val newResult = context.result
    var existedResult = getResult(context.std)
    if (null != existedResult) {
      existedResult.remark = newResult.remark
      //      existedResult.updatedAt = new Date()
      existedResult.auditor = newResult.auditor
      val existedcreditsCompleted = existedResult.auditStat.creditsCompleted
      existedResult.auditStat = newResult.auditStat
      existedResult.partial = newResult.partial
      existedResult.gpa = newResult.gpa
      existedResult.partial = newResult.partial
      var updatePassed = true
      val existedPassed = existedResult.passed
      if (null != existedResult.departOpinion || null != existedResult.finalOpinion) {
        updatePassed = false
        if (existedResult.passed &&
          newResult.auditStat.creditsCompleted < existedcreditsCompleted) {
          updatePassed = true
        }
      }
      val updates = new StringBuilder()
      mergeGroupResult(existedResult, new GroupResultAdapter(existedResult), new GroupResultAdapter(newResult),
        updates)
      if (!updatePassed) {
        existedResult.passed = existedPassed
      } else {
        existedResult.passed = newResult.passed
      }
      if (updates.length > 0) updates.deleteCharAt(updates.length - 1)
      existedResult.updates = updates.toString
    } else {
      existedResult = newResult
    }
    entityDao.saveOrUpdate(existedResult)
    context.result = existedResult
  }

  private def mergeGroupResult(existedResult: PlanAuditResult,
    target: GroupAuditResult,
    source: GroupAuditResult,
    updates: StringBuilder) {
    val delta = source.auditStat.creditsCompleted - target.auditStat.creditsCompleted
    if (java.lang.Float.compare(delta, 0) != 0) {
      updates.append(source.name)
      if (delta > 0) updates.append('+').append(delta) else updates.append(delta)
      updates.append(';')
    }
    target.auditStat = source.auditStat
    target.passed = source.passed
    val targetGroupResults = Collections.newMap[String, GroupAuditResult]
    val sourceGroupResults = Collections.newMap[String, GroupAuditResult]
    for (result <- target.children) targetGroupResults.put(result.name, result)
    for (result <- source.children) sourceGroupResults.put(result.name, result)
    val targetCourseResults = Collections.newMap[Course, CourseAuditResult]
    val sourceCourseResults = Collections.newMap[Course, CourseAuditResult]
    for (courseResult <- target.courseResults) targetCourseResults.put(courseResult.course, courseResult)
    for (courseResult <- source.courseResults) sourceCourseResults.put(courseResult.course, courseResult)
    val removed = Collections.subtract(targetGroupResults.keySet, sourceGroupResults.keySet)
    for (groupName <- removed) {
      val gg = targetGroupResults(groupName)
      gg.detach()
      target.removeChild(gg)
    }
    val added = Collections.subtract(sourceGroupResults.keySet, targetGroupResults.keySet)
    for (groupName <- added) {
      val groupResult = sourceGroupResults.get(groupName).asInstanceOf[GroupAuditResult]
      target.addChild(groupResult)
      groupResult.attachTo(existedResult)
    }
    val common = Collections.intersection(sourceGroupResults.keySet, targetGroupResults.keySet)
    for (groupName <- common) {
      mergeGroupResult(existedResult, targetGroupResults.get(groupName).orNull, sourceGroupResults.get(groupName).orNull,
        updates)
    }
    val removedCourses = Collections.subtract(targetCourseResults.keySet, sourceCourseResults.keySet)
    for (course <- removedCourses) {
      val courseResult = targetCourseResults.get(course).asInstanceOf[CourseAuditResult]
      target.courseResults -= (courseResult)
    }
    val addedCourses = Collections.subtract(sourceCourseResults.keySet, targetCourseResults.keySet)
    for (course <- addedCourses) {
      val courseResult = sourceCourseResults.get(course).asInstanceOf[CourseAuditResult]
      courseResult.groupResult.courseResults -= (courseResult)
      courseResult.groupResult = target
      target.courseResults += (courseResult)
    }
    val commonCourses = Collections.intersection(sourceCourseResults.keySet, targetCourseResults.keySet)
    for (course <- commonCourses) {
      val targetCourseResult = targetCourseResults(course)
      val sourceCourseResult = sourceCourseResults(course)
      targetCourseResult.passed = sourceCourseResult.passed
      targetCourseResult.scores = sourceCourseResult.scores
      targetCourseResult.compulsory = sourceCourseResult.compulsory
      targetCourseResult.remark = sourceCourseResult.remark
    }
  }
}
