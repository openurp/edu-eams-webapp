package org.openurp.edu.eams.teach.planaudit.service.listeners

import java.util.Date
import org.beangle.commons.collection.Collections
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.planaudit.CourseAuditResult
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.PlanAuditResult
import org.openurp.edu.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.teach.planaudit.model.GroupAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import PlanAuditCourseTakeListener._
import org.openurp.edu.teach.grade.Grade
import scala.collection.mutable.Buffer

object PlanAuditCourseTakeListener {

  private val TakeCourse2Types = "takeCourse2Types"

  private val Group2CoursesKey = "group2CoursesKey"
}

class PlanAuditCourseTakeListener extends PlanAuditListener {

  private var entityDao: EntityDao = _

  var defaultPassed: Boolean = true

  def startPlanAudit(context: PlanAuditContext): Boolean = {
    val builder = OqlBuilder.from(classOf[CourseTake], "ct").where("ct.std=:std", context.std)
    builder.where("not exists(from " + classOf[CourseGrade].getName +
      " cg where cg.semester=ct.lesson.semester and cg.course=ct.lesson.course " +
      "and cg.std=ct.std and cg.status=:status)", Grade.Status.Published)
    builder.where("ct.lesson.semester.endOn >= :now", new Date())
    builder.select("ct.lesson.course,ct.lesson.courseType")
    val course2Types = Collections.newMap[Course, CourseType]
    for (c <- entityDao.search(builder)) {
      course2Types.put(c.asInstanceOf[Array[Any]](0).asInstanceOf[Course], c.asInstanceOf[Array[Any]](1).asInstanceOf[CourseType])
    }
    context.params.put(TakeCourse2Types, course2Types)
    context.params.put(Group2CoursesKey, Collections.newBuffer[Pair[GroupAuditResult, Course]])
    true
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    true
  }

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    if (context.params.get(TakeCourse2Types).asInstanceOf[collection.mutable.Map[Course, CourseType]]
      .contains(planCourse.course)) {
      context.params.get(Group2CoursesKey).asInstanceOf[Buffer[Pair[GroupAuditResult, Course]]] += new Pair(groupResult, planCourse.course)
    }
    true
  }

  def endPlanAudit(context: PlanAuditContext) {
    val course2Types = context.params.remove(TakeCourse2Types).asInstanceOf[collection.mutable.Map[Course, CourseType]]
    val results = context.params.remove(Group2CoursesKey).asInstanceOf[Buffer[Pair[GroupAuditResult, Course]]]
    val used = Collections.newSet[GroupAuditResult]
    for (tuple <- results) {
      add2Group(tuple._2, tuple._1)
      course2Types.remove(tuple._2)
      used.add(tuple._1)
    }
    val lastTarget = getTargetGroupResult(context)
    for ((key, value) <- course2Types) {
      val g = context.coursePlan.group(value)
      var gr: GroupAuditResult = null
      if (null == g || g.planCourses.isEmpty) {
        gr = context.result.groupResult(value)
      }
      if (null == gr) gr = lastTarget
      if (null != gr) {
        add2Group(key, gr)
        used.add(gr)
      }
    }
    for (aur <- used) aur.checkPassed(true)
  }

  private def add2Group(course: Course, groupResult: GroupAuditResult) {
    var existedResult: CourseAuditResult = null
    for (cr <- groupResult.courseResults if cr.course == course) {
      existedResult = cr
      //break
    }
    groupResult.planResult.partial = true
    if (existedResult == null) {
      existedResult = new CourseAuditResultBean()
      existedResult.course = course
      existedResult.passed = defaultPassed
      groupResult.addCourseResult(existedResult)
    } else {
      if (defaultPassed) existedResult.passed = defaultPassed
      existedResult.groupResult.updateCourseResult(existedResult)
    }
    if (Strings.isEmpty(existedResult.remark)) {
      existedResult.remark = "在读"
    } else {
      existedResult.remark = (existedResult.remark + "/在读")
    }
  }

  private def getTargetGroupResult(context: PlanAuditContext): GroupAuditResult = {
    val electiveType = context.standard.convertTarCourseType
    if (null == electiveType) return null
    val result = context.result
    var groupResult = result.groupResult(electiveType)
    if (null == groupResult) {
      val groupRs = new GroupAuditResultBean()
      groupRs.courseType = electiveType
      groupRs.name = electiveType.name
      groupRs.groupNum = -1
      groupResult = groupRs
      result.addGroupResult(groupResult)
    }
    groupResult
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
