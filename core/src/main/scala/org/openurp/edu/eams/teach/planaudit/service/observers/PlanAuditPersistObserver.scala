package org.openurp.edu.eams.teach.planaudit.service.observers

import java.util.Date
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.planaudit.CourseAuditResult
import org.openurp.edu.eams.teach.planaudit.GroupAuditResult
import org.openurp.edu.eams.teach.planaudit.PlanAuditResult
import org.openurp.edu.eams.teach.planaudit.adapters.GroupResultAdapter
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext

import scala.collection.JavaConversions._

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
      return results.get(0)
    }
    null
  }

  def notifyEnd(context: PlanAuditContext, index: Int) {
    val newResult = context.getResult
    var existedResult = getResult(context.getStd)
    if (null != existedResult) {
      existedResult.setRemark(newResult.getRemark)
      existedResult.setUpdatedAt(new Date())
      existedResult.setAuditor(newResult.getAuditor)
      val existedcreditsCompleted = existedResult.getAuditStat.getCreditsCompleted
      existedResult.setAuditStat(newResult.getAuditStat)
      existedResult.setPartial(newResult.isPartial)
      existedResult.setGpa(newResult.getGpa)
      existedResult.setPartial(newResult.isPartial)
      var updatePassed = true
      val existedPassed = existedResult.isPassed
      if (null != existedResult.getDepartOpinion || null != existedResult.getFinalOpinion) {
        updatePassed = false
        if (existedResult.isPassed && 
          newResult.getAuditStat.getCreditsCompleted < existedcreditsCompleted) {
          updatePassed = true
        }
      }
      val updates = new StringBuilder()
      mergeGroupResult(existedResult, new GroupResultAdapter(existedResult), new GroupResultAdapter(newResult), 
        updates)
      if (!updatePassed) {
        existedResult.setPassed(existedPassed)
      } else {
        existedResult.setPassed(newResult.isPassed)
      }
      if (updates.length > 0) updates.deleteCharAt(updates.length - 1)
      existedResult.setUpdates(updates.toString)
    } else {
      existedResult = newResult
    }
    entityDao.saveOrUpdate(existedResult)
    context.setResult(existedResult)
  }

  private def mergeGroupResult(existedResult: PlanAuditResult, 
      target: GroupAuditResult, 
      source: GroupAuditResult, 
      updates: StringBuilder) {
    val delta = source.getAuditStat.getCreditsCompleted - target.getAuditStat.getCreditsCompleted
    if (java.lang.Float.compare(delta, 0) != 0) {
      updates.append(source.getName)
      if (delta > 0) updates.append('+').append(delta) else updates.append(delta)
      updates.append(';')
    }
    target.setAuditStat(source.getAuditStat)
    target.setPassed(source.isPassed)
    val targetGroupResults = CollectUtils.newHashMap()
    val sourceGroupResults = CollectUtils.newHashMap()
    for (result <- target.getChildren) targetGroupResults.put(result.getName, result)
    for (result <- source.getChildren) sourceGroupResults.put(result.getName, result)
    val targetCourseResults = CollectUtils.newHashMap()
    val sourceCourseResults = CollectUtils.newHashMap()
    for (courseResult <- target.getCourseResults) targetCourseResults.put(courseResult.getCourse, courseResult)
    for (courseResult <- source.getCourseResults) sourceCourseResults.put(courseResult.getCourse, courseResult)
    val removed = CollectUtils.subtract(targetGroupResults.keySet, sourceGroupResults.keySet)
    for (groupName <- removed) {
      val gg = targetGroupResults.get(groupName)
      gg.detach()
      target.removeChild(gg)
    }
    val added = CollectUtils.subtract(sourceGroupResults.keySet, targetGroupResults.keySet)
    for (groupName <- added) {
      val groupResult = sourceGroupResults.get(groupName).asInstanceOf[GroupAuditResult]
      target.addChild(groupResult)
      groupResult.attachTo(existedResult)
    }
    val common = CollectUtils.intersection(sourceGroupResults.keySet, targetGroupResults.keySet)
    for (groupName <- common) {
      mergeGroupResult(existedResult, targetGroupResults.get(groupName), sourceGroupResults.get(groupName), 
        updates)
    }
    val removedCourses = CollectUtils.subtract(targetCourseResults.keySet, sourceCourseResults.keySet)
    for (course <- removedCourses) {
      val courseResult = targetCourseResults.get(course).asInstanceOf[CourseAuditResult]
      target.getCourseResults.remove(courseResult)
    }
    val addedCourses = CollectUtils.subtract(sourceCourseResults.keySet, targetCourseResults.keySet)
    for (course <- addedCourses) {
      val courseResult = sourceCourseResults.get(course).asInstanceOf[CourseAuditResult]
      courseResult.getGroupResult.getCourseResults.remove(courseResult)
      courseResult.setGroupResult(target)
      target.getCourseResults.add(courseResult)
    }
    val commonCourses = CollectUtils.intersection(sourceCourseResults.keySet, targetCourseResults.keySet)
    for (course <- commonCourses) {
      val targetCourseResult = targetCourseResults.get(course)
      val sourceCourseResult = sourceCourseResults.get(course)
      targetCourseResult.setPassed(sourceCourseResult.isPassed)
      targetCourseResult.setScores(sourceCourseResult.getScores)
      targetCourseResult.setCompulsory(sourceCourseResult.isCompulsory)
      targetCourseResult.setRemark(sourceCourseResult.getRemark)
    }
  }
}
