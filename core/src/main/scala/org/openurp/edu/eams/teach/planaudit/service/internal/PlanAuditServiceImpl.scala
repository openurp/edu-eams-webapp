package org.openurp.edu.eams.teach.planaudit.service.internal

import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.Iterator
import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.security.blueprint.SecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.planaudit.AuditStat
import org.openurp.edu.eams.teach.planaudit.GroupAuditResult
import org.openurp.edu.eams.teach.planaudit.PlanAuditResult
import org.openurp.edu.eams.teach.planaudit.adapters.CourseGroupAdapter
import org.openurp.edu.eams.teach.planaudit.adapters.GroupResultAdapter
import org.openurp.edu.eams.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.eams.teach.planaudit.model.PlanAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditService
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.ReferedCourseGroup
import org.openurp.edu.eams.teach.program.major.model.ReferenceGroupBean
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.teach.program.util.PlanUtils

import scala.collection.JavaConversions._

class PlanAuditServiceImpl extends BaseServiceImpl with PlanAuditService {

  private var logger: Logger = LoggerFactory.getLogger(classOf[PlanAuditServiceImpl])

  protected var coursePlanProvider: CoursePlanProvider = _

  protected var courseGradeProvider: CourseGradeProvider = _

  protected var groupResultBuilder: GroupResultBuilder = new DefaultGroupResultBuilder()

  protected var listeners: List[PlanAuditListener] = new ArrayList[PlanAuditListener]()

  def audit(student: Student, context: PlanAuditContext): PlanAuditResult = {
    logger.debug("start audit {}", student.getCode)
    val planAuditResult = new PlanAuditResultBean(student)
    planAuditResult.setCreatedAt(new Date())
    planAuditResult.setPassed(false)
    planAuditResult.setRemark(null)
    planAuditResult.setUpdatedAt(new Date())
    planAuditResult.setAuditor(SecurityUtils.getUsername + SecurityUtils.getFullname)
    planAuditResult.setAuditStat(new AuditStat())
    planAuditResult.setPartial(context.isPartial)
    context.setResult(planAuditResult)
    val plan = context.getCoursePlan
    if (null == plan) {
      return context.getResult
    }
    context.setStdGrade(new StdGradeImpl(courseGradeProvider.getPublished(student)))
    for (listener <- listeners if !listener.startPlanAudit(context)) {
      return planAuditResult
    }
    val courseGroupAdapter = new CourseGroupAdapter(context.getCoursePlan)
    val groupResultAdapter = new GroupResultAdapter(courseGroupAdapter, planAuditResult)
    var creditsRequired = context.getCoursePlan.getCredits
    if (context.getAuditTerms != null && context.getAuditTerms.length != 0) {
      creditsRequired = 0
      for (i <- 0 until context.getAuditTerms.length; group <- context.getCoursePlan.getGroups if group.getParent == null) {
        creditsRequired += PlanUtils.getGroupCredits(group, java.lang.Integer.valueOf(context.getAuditTerms()(i)))
      }
    }
    planAuditResult.getAuditStat.setCreditsRequired(creditsRequired)
    var numRequired = 0
    if (!context.isPartial) {
      for (group <- context.getCoursePlan.getGroups if group.getParent == null) {
        numRequired += group.getCourseNum
      }
    }
    planAuditResult.getAuditStat.setNumRequired(numRequired)
    auditGroup(context, courseGroupAdapter, groupResultAdapter)
    for (listener <- listeners) listener.endPlanAudit(context)
    planAuditResult
  }

  private def auditGroup(context: PlanAuditContext, courseGroup: CourseGroup, groupAuditResult: GroupAuditResult) {
    val courseGroups = courseGroup.getChildren
    val planAuditResult = context.getResult
    groupAudit: var it = courseGroups.iterator()
    while (it.hasNext) {
      val children = it.next()
      val childResult = groupResultBuilder.buildResult(context, children)
      groupAuditResult.addChild(childResult)
      planAuditResult.addGroupResult(childResult)
      var it1 = listeners.iterator()
      while (it1.hasNext) {
        val listener = it1.next()
        if (!listener.startGroupAudit(context, children, childResult)) {
          planAuditResult.getAuditStat.reduceRequired(childResult.getAuditStat.getCreditsRequired, childResult.getAuditStat.getNumRequired)
          groupAuditResult.removeChild(childResult)
          planAuditResult.removeGroupResult(childResult)
          //continue
        }
      }
      auditGroup(context, children, childResult)
    }
    var myPlanCourses = courseGroup.getPlanCourses
    if (courseGroup.isInstanceOf[ReferedCourseGroup]) {
      val refereGroup = courseGroup.asInstanceOf[ReferedCourseGroup].getReferenceGroup
      if (null != refereGroup && null != refereGroup.getShareCourseGroup) {
        myPlanCourses = refereGroup.getPlanCourses
      }
    }
    courseAudit: var iter = myPlanCourses.iterator()
    while (iter.hasNext) {
      val planCourse = iter.next()
      for (listener <- listeners if !listener.startCourseAudit(context, groupAuditResult, planCourse)) {
        //continue
      }
      val planCourseAuditResult = new CourseAuditResultBean(planCourse)
      var courseGrades = context.getStdGrade.useGrades(planCourse.getCourse)
      if (courseGrades.isEmpty) {
        if (!planCourse.isCompulsory) //continue
        courseGrades = Collections.emptyList()
      }
      planCourseAuditResult.checkPassed(courseGrades)
      groupAuditResult.addCourseResult(planCourseAuditResult)
    }
    groupAuditResult.checkPassed(false)
  }

  def getResult(std: Student): PlanAuditResult = {
    val query = OqlBuilder.from(classOf[PlanAuditResult], "planResult")
    query.where("planResult.std = :std", std)
    val results = entityDao.search(query)
    if (results.size > 0) {
      return results.get(0)
    }
    null
  }

  def getSeriousResult(std: Student): PlanAuditResult = {
    val res = getResult(std)
    if (res != null && !res.isPartial) {
      return res
    }
    null
  }

  def setCoursePlanProvider(coursePlanProvider: CoursePlanProvider) {
    this.coursePlanProvider = coursePlanProvider
  }

  def setCourseGradeProvider(courseGradeProvider: CourseGradeProvider) {
    this.courseGradeProvider = courseGradeProvider
  }

  def getListeners(): List[PlanAuditListener] = listeners

  def setListeners(listeners: List[PlanAuditListener]) {
    this.listeners = listeners
  }

  def setGroupResultBuilder(groupResultBuilder: GroupResultBuilder) {
    this.groupResultBuilder = groupResultBuilder
  }
}
