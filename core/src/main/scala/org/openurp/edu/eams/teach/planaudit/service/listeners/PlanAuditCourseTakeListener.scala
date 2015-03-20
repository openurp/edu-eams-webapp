package org.openurp.edu.eams.teach.planaudit.service.listeners


import java.util.Date


import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.CourseType
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
import org.openurp.edu.eams.teach.program.GroupRelation
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.model.ExpressionGroupRelation
import PlanAuditCourseTakeListener._




object PlanAuditCourseTakeListener {

  private val TakeCourse2Types = "takeCourse2Types"

  private val Group2CoursesKey = "group2CoursesKey"
}

class PlanAuditCourseTakeListener extends PlanAuditListener {

  private var entityDao: EntityDao = _

  
  var defaultPassed: Boolean = true

  def startPlanAudit(context: PlanAuditContext): Boolean = {
    val builder = OqlBuilder.from(classOf[CourseTake], "ct").where("ct.std=:std", context.std)
    builder.where("not exists(from " + classOf[CourseGrade].name + 
      " cg where cg.semester=ct.lesson.semester and cg.course=ct.lesson.course " + 
      "and cg.std=ct.std and cg.status=:status)", Grade.Status.PUBLISHED)
    builder.where("ct.lesson.semester.endOn >= :now", new Date())
    builder.select("ct.lesson.course,ct.lesson.courseType")
    val course2Types = CollectUtils.newHashMap()
    for (c <- entityDao.search(builder)) {
      course2Types.put(c.asInstanceOf[Array[Any]](0).asInstanceOf[Course], c.asInstanceOf[Array[Any]](1).asInstanceOf[CourseType])
    }
    context.params.put(TakeCourse2Types, course2Types)
    context.params.put(Group2CoursesKey, new ArrayList[Pair[GroupAuditResult, Course]]())
    true
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    true
  }

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    if (context.params.get(TakeCourse2Types).asInstanceOf[Map[Course, CourseType]]
      .containsKey(planCourse.course)) {
      (context.params.get(Group2CoursesKey)).asInstanceOf[ArrayList[Pair[GroupAuditResult, Course]]]
        .add(new Pair(groupResult, planCourse.course))
    }
    true
  }

  def endPlanAudit(context: PlanAuditContext) {
    val course2Types = context.params.remove(TakeCourse2Types).asInstanceOf[Map[Course, CourseType]]
    val results = context.params.remove(Group2CoursesKey).asInstanceOf[ArrayList[Pair[GroupAuditResult, Course]]]
    val used = CollectUtils.newHashSet()
    for (tuple <- results) {
      add2Group(tuple.right, tuple.left)
      course2Types.remove(tuple.right)
      used.add(tuple.left)
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
    groupResult.planResult.partial=true
    if (existedResult == null) {
      existedResult = new CourseAuditResultBean()
      existedResult.course=course
      existedResult.passed=defaultPassed
      groupResult.addCourseResult(existedResult)
    } else {
      if (defaultPassed) existedResult.passed=defaultPassed
      existedResult.groupResult.updateCourseResult(existedResult)
    }
    if (Strings.isEmpty(existedResult.remark)) {
      existedResult.remark="在读"
    } else {
      existedResult.remark=(existedResult.remark + "/在读")
    }
  }

  private def getTargetGroupResult(context: PlanAuditContext): GroupAuditResult = {
    val electiveType = context.standard.convertTargetCourseType
    if (null == electiveType) return null
    val result = context.result
    var groupResult = result.groupResult(electiveType)
    if (null == groupResult) {
      val groupRs = new GroupAuditResultBean()
      groupRs.courseType=electiveType
      groupRs.name=electiveType.name
      val groupRelation = new ExpressionGroupRelation()
      groupRelation.relation=ExpressionGroupRelation.AND
      groupRs.relation=groupRelation
      groupResult = groupRs
      result.addGroupResult(groupResult)
    }
    groupResult
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
