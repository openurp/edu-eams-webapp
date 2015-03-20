package org.openurp.edu.eams.teach.planaudit.service.internal

import java.util.Date


import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.security.blueprint.SecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.openurp.edu.base.Student
import org.openurp.edu.teach.grade.domain.CourseGradeProvider
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.planaudit.AuditStat
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.PlanAuditResult
import org.openurp.edu.teach.planaudit.adapters.CourseGroupAdapter
import org.openurp.edu.teach.planaudit.adapters.GroupResultAdapter
import org.openurp.edu.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.teach.planaudit.model.PlanAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditService
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.teach.program.util.PlanUtils



class PlanAuditServiceImpl extends BaseServiceImpl with PlanAuditService {

  private var logger: Logger = LoggerFactory.getLogger(classOf[PlanAuditServiceImpl])

  protected var coursePlanProvider: CoursePlanProvider = _

  protected var courseGradeProvider: CourseGradeProvider = _

  protected var groupResultBuilder: GroupResultBuilder = new DefaultGroupResultBuilder()

  protected var listeners: List[PlanAuditListener] = new ArrayList[PlanAuditListener]()

  def audit(student: Student, context: PlanAuditContext): PlanAuditResult = {
    logger.debug("start audit {}", student.code)
    val planAuditResult = new PlanAuditResultBean(student)
    planAuditResult.createdAt=new Date()
    planAuditResult.passed=false
    planAuditResult.remark=null
    planAuditResult.updatedAt=new Date()
    planAuditResult.auditor=(SecurityUtils.username + SecurityUtils.fullname)
    planAuditResult.auditStat=new AuditStat()
    planAuditResult.partial=context.isPartial
    context.result=planAuditResult
    val plan = context.coursePlan
    if (null == plan) {
      return context.result
    }
    context.stdGrade=new StdGradeImpl(courseGradeProvider.published(student))
    for (listener <- listeners if !listener.startPlanAudit(context)) {
      return planAuditResult
    }
    val courseGroupAdapter = new CourseGroupAdapter(context.coursePlan)
    val groupResultAdapter = new GroupResultAdapter(courseGroupAdapter, planAuditResult)
    var creditsRequired = context.coursePlan.credits
    if (context.auditTerms != null && context.auditTerms.length != 0) {
      creditsRequired = 0
      for (i <- 0 until context.auditTerms.length; group <- context.coursePlan.groups if group.parent == null) {
        creditsRequired += PlanUtils.groupCredits(group, java.lang.Integer.valueOf(context.auditTerms()(i)))
      }
    }
    planAuditResult.auditStat.creditsRequired=creditsRequired
    var numRequired = 0
    if (!context.isPartial) {
      for (group <- context.coursePlan.groups if group.parent == null) {
        numRequired += group.courseNum
      }
    }
    planAuditResult.auditStat.numRequired=numRequired
    auditGroup(context, courseGroupAdapter, groupResultAdapter)
    for (listener <- listeners) listener.endPlanAudit(context)
    planAuditResult
  }

  private def auditGroup(context: PlanAuditContext, courseGroup: CourseGroup, groupAuditResult: GroupAuditResult) {
    val courseGroups = courseGroup.children
    val planAuditResult = context.result
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
          planAuditResult.auditStat.reduceRequired(childResult.auditStat.creditsRequired, childResult.auditStat.numRequired)
          groupAuditResult.removeChild(childResult)
          planAuditResult.removeGroupResult(childResult)
          //continue
        }
      }
      auditGroup(context, children, childResult)
    }
    var myPlanCourses = courseGroup.planCourses
    if (courseGroup.isInstanceOf[ReferedCourseGroup]) {
      val refereGroup = courseGroup.asInstanceOf[ReferedCourseGroup].referenceGroup
      if (null != refereGroup && null != refereGroup.shareCourseGroup) {
        myPlanCourses = refereGroup.planCourses
      }
    }
    courseAudit: var iter = myPlanCourses.iterator()
    while (iter.hasNext) {
      val planCourse = iter.next()
      for (listener <- listeners if !listener.startCourseAudit(context, groupAuditResult, planCourse)) {
        //continue
      }
      val planCourseAuditResult = new CourseAuditResultBean(planCourse)
      var courseGrades = context.stdGrade.useGrades(planCourse.course)
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
