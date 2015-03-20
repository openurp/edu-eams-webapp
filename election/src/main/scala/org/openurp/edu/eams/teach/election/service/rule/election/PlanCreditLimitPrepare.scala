package org.openurp.edu.eams.teach.election.service.rule.election




import org.beangle.commons.collection.CollectUtils
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint
import org.openurp.edu.eams.teach.election.service.context.ElectCourseGroup
import org.openurp.edu.eams.teach.election.service.context.ElectCoursePlan
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.PlanAuditResult
import org.openurp.edu.teach.planaudit.model.PlanAuditStandard
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditService
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.eams.teach.program.util.PlanUtils



class PlanCreditLimitPrepare extends ElectRulePrepare {

  protected var planAuditService: PlanAuditService = _

  protected var coursePlanPrepare: CoursePlanPrepare = _

  protected var currentTermPrepare: CurrentTermPrepare = _

  protected var entityDao: EntityDao = _

  def prepare(context: PrepareContext) {
    if (context.isPreparedData(PreparedDataName.PLAN_CREDITS_LIMIT)) return
    coursePlanPrepare.prepare(context)
    val state = context.getState
    val electCoursePlan = state.getCoursePlan
    val plan = context.getPlan
    val groupResults = CollectUtils.newHashMap()
    if (null != plan) {
      val planAuditContext = new PlanAuditContext(plan, null, null)
      planAuditContext.setStandard(new PlanAuditStandard())
      val auditResult = planAuditService.audit(context.getStudent, planAuditContext)
      for (groupResult <- auditResult.getGroupResults) {
        flatGroup(groupResult, groupResults)
      }
      val term = currentTermPrepare.getTerm(context)
      val courseGroups = getTopCourseGroups(plan)
      val courseTypeCredits = findCourseTypeCredit(state)
      for (courseGroup <- courseGroups) {
        addGroup(term, electCoursePlan, null, courseGroup, groupResults, courseTypeCredits, 0, state)
      }
      if (!context.isPreparedData(PreparedDataName.RETAKE_COURSES)) {
        state.getHisCourses.putAll(planAuditContext.getStdGrade.getCoursePassedMap)
        context.addPreparedDataName(PreparedDataName.RETAKE_COURSES)
      }
    }
    for (take <- context.getTakes) {
      val group = electCoursePlan.getOrCreateGroup(take.getLesson.getCourse, take.getLesson.getCourseType)
      if (null == group.getCourseType.getName) {
        group.getCourseType.setName(entityDao.get(classOf[CourseType], group.getCourseType.id)
          .getName)
      }
      group.addElectCourse(take.getLesson.getCourse)
    }
    context.addPreparedDataName(PreparedDataName.PLAN_CREDITS_LIMIT)
  }

  private def flatGroup(groupResult: GroupAuditResult, results: Map[Integer, GroupAuditResult]) {
    results.put(groupResult.getCourseType.id, groupResult)
    for (childGroup <- groupResult.getChildren) {
      flatGroup(childGroup, results)
    }
  }

  protected def addGroup(term: java.lang.Integer, 
      electCoursePlan: ElectCoursePlan, 
      parent: ElectCourseGroup, 
      courseGroup: CourseGroup, 
      groupResults: Map[Integer, GroupAuditResult], 
      courseTypeCredits: Map[CourseType, Float], 
      extraCredits: Float, 
      state: ElectState): ElectCourseGroup = {
    var me = electCoursePlan.groups.get(courseGroup.getCourseType.id)
    if (null == me) {
      me = new ElectCourseGroup(courseGroup.getCourseType)
      me.setParent(parent)
      if (null != parent) parent.getChildren.add(me)
      electCoursePlan.addGroup(me)
    }
    val myExtraCredits = getExtraCredits(courseGroup)
    for (childGroup <- courseGroup.getChildren) {
      addGroup(term, electCoursePlan, me, childGroup, groupResults, courseTypeCredits, myExtraCredits, 
        state)
    }
    val typeCredit = courseTypeCredits.get(courseGroup.getCourseType)
    me.setRequireCredits(courseGroup.getCredits)
    me.setLimitCredits(PlanUtils.getGroupCredits(courseGroup, term))
    if (java.lang.Float.compare(me.getLimitCredits, 0) <= 0) {
      me.setLimitCredits(courseGroup.getCredits + extraCredits)
    }
    if (null != typeCredit) {
      me.setLimitCredits(Math.min(typeCredit, me.getLimitCredits))
    }
    val gar = groupResults.get(courseGroup.getCourseType.id)
    if (null != gar) {
      me.setCompleteCredits(gar.getAuditStat.getCreditsCompleted)
    }
    me
  }

  protected def getExtraCredits(group: CourseGroup): Float = {
    var sumcredits = group.getCredits
    for (child <- group.getChildren) {
      sumcredits -= child.getCredits
    }
    if ((sumcredits > 0)) sumcredits else 0
  }

  protected def getTopCourseGroups(plan: CoursePlan): List[CourseGroup] = {
    if (plan.getGroups == null) {
      return new ArrayList[CourseGroup]()
    }
    val res = new ArrayList[CourseGroup]()
    for (group <- plan.getGroups if group != null && group.getParent == null) {
      res.add(group.asInstanceOf[CourseGroup])
    }
    res
  }

  protected def findCourseTypeCredit(state: ElectState): Map[CourseType, Float] = {
    val builder = OqlBuilder.from(classOf[CourseTypeCreditConstraint], "courseTypeCreditStat")
    builder.where("courseTypeCreditStat.semester.id = :semesterId", state.getSemesterId)
    builder.where("courseTypeCreditStat.grades like :grade", "%" + state.getStd.grade + "%")
    builder.where("courseTypeCreditStat.education.id=:education", state.getStd.educationId)
    builder.cacheable()
    val courseTypeCreditConstraints = entityDao.search(builder)
    val credits = CollectUtils.newHashMap()
    for (courseTypeCreditStat <- courseTypeCreditConstraints) {
      credits.put(courseTypeCreditStat.getCourseType, courseTypeCreditStat.getLimitCredit)
    }
    credits
  }

  def setCurrentTermPrepare(currentTermPrepare: CurrentTermPrepare) {
    this.currentTermPrepare = currentTermPrepare
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setPlanAuditService(planAuditService: PlanAuditService) {
    this.planAuditService = planAuditService
  }

  def setCoursePlanPrepare(coursePlanPrepare: CoursePlanPrepare) {
    this.coursePlanPrepare = coursePlanPrepare
  }
}
