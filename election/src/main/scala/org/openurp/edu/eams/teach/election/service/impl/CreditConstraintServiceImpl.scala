package org.openurp.edu.eams.teach.election.service.impl


import java.util.Date



import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.SecurityUtils
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.teach.election.model.constraint.AbstractCreditConstraint
import org.openurp.edu.eams.teach.election.model.constraint.ConstraintLogger
import org.openurp.edu.eams.teach.election.model.constraint.StdCourseCountConstraint
import org.openurp.edu.eams.teach.election.model.constraint.StdCreditConstraint
import org.openurp.edu.eams.teach.election.model.constraint.StdTotalCreditConstraint
import org.openurp.edu.eams.teach.election.service.CreditConstraintService
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.major.helper.MajorPlanSearchHelper
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.web.helper.RestrictionHelper




class CreditConstraintServiceImpl extends BaseServiceImpl with CreditConstraintService {

  private var semesterService: SemesterService = _

  private var restrictionHelper: RestrictionHelper = _

  private var majorPlanSearchHelper: MajorPlanSearchHelper = _

  
  var coursePlanProvider: CoursePlanProvider = _

  def getCourseCountConstraint(semester: Semester, std: Student): StdCourseCountConstraint = {
    val it = entityDao.get(classOf[StdCourseCountConstraint], Array("semester", "std"), Array(semester, std))
      .iterator()
    if (it.hasNext) it.next() else null
  }

  def getTotalCreditConstraint(std: Student): StdTotalCreditConstraint = {
    val it = entityDao.get(classOf[StdTotalCreditConstraint], "std", std)
      .iterator()
    if (it.hasNext) it.next() else null
  }

  def getCreditConstraint(semester: Semester, std: Student): AbstractCreditConstraint = {
    val it = entityDao.get(classOf[StdCreditConstraint], Array("semester", "std"), Array(semester, std))
      .iterator()
    if (it.hasNext) {
      return it.next()
    }
    null
  }

  private def getCredits(term: Int, creditString: String): java.lang.Float = {
    val creditPerTerms = Strings.split(creditString, ",")
    if (null == creditPerTerms) 0f else if (creditPerTerms.length < term) 0f else java.lang.Float.parseFloat(creditPerTerms(term - 1))
  }

  def initStdTotalCreditConstraint(project: Project): String = {
    val stdTypes = restrictionHelper.stdTypes
    val departs = restrictionHelper.getDeparts
    val educations = restrictionHelper.educations
    if (CollectUtils.isEmpty(stdTypes) || CollectUtils.isEmpty(departs) || 
      CollectUtils.isEmpty(educations) || 
      null == project) {
      return "初始化失败,权限不足"
    }
    val constraintBuilder = OqlBuilder.from(classOf[StdTotalCreditConstraint], "stdTotalCreditConstraint")
    val conditions = QueryHelper.extractConditions(constraintBuilder.getEntityClass, constraintBuilder.getAlias, 
      "stdTotalCreditConstraint.id")
    constraintBuilder.where(conditions)
    val maxFrom = Params.getFloat("maxFrom")
    if (null != maxFrom) {
      constraintBuilder.where("stdTotalCreditConstraint.maxCredit >=:maxFrom", maxFrom)
    }
    val maxTo = Params.getFloat("maxTo")
    if (null != maxTo) {
      constraintBuilder.where("stdTotalCreditConstraint.maxCredit <=:maxTo", maxTo)
    }
    constraintBuilder.where("stdTotalCreditConstraint.std.project=:project", project)
    constraintBuilder.where("stdTotalCreditConstraint.std.type in(:stdTypes) " + 
      "and stdTotalCreditConstraint.std.department in(:departs) " + 
      "and stdTotalCreditConstraint.std.education in(:educations)", stdTypes, departs, educations)
    val electedTotalCreditFrom = Params.getFloat("electedTotalCreditFrom")
    if (null != electedTotalCreditFrom) {
      constraintBuilder.where("COALESCE((select sum(courseTake.lesson.course.credit) " + 
        "from " + 
        classOf[CourseTake].getName + 
        " courseTake " + 
        "where courseTake.std=stdTotalCreditConstraint.std),0) >=:electedTotalCreditFrom", electedTotalCreditFrom)
    }
    val electedTotalCreditTo = Params.getFloat("electedTotalCreditTo")
    if (null != electedTotalCreditTo) {
      constraintBuilder.where("COALESCE((select sum(courseTake.lesson.course.credit) " + 
        "from " + 
        classOf[CourseTake].getName + 
        " courseTake " + 
        "where courseTake.std=stdTotalCreditConstraint.std),0) <=:electedTotalCreditTo", electedTotalCreditTo)
    }
    val constraints = entityDao.search(constraintBuilder)
    val stdBuilder = OqlBuilder.from(classOf[Student].getName + " student")
    for (consCondition <- conditions if consCondition.getContent.startsWith("stdTotalCreditConstraint.std")) {
      val condition = new Condition(consCondition.getContent.replace("stdTotalCreditConstraint.std", 
        "student"))
      condition.params(consCondition.getParams)
      stdBuilder.where(condition)
    }
    if (null != electedTotalCreditFrom) {
      stdBuilder.where("COALESCE((select sum(courseTake.lesson.course.credit) " + 
        "from " + 
        classOf[CourseTake].getName + 
        " courseTake " + 
        "where courseTake.std=student),0) >=:electedTotalCreditFrom", electedTotalCreditFrom)
    }
    if (null != electedTotalCreditTo) {
      stdBuilder.where("COALESCE((select sum(courseTake.lesson.course.credit) " + 
        "from " + 
        classOf[CourseTake].getName + 
        " courseTake " + 
        "where courseTake.std=student),0) <=:electedTotalCreditTo", electedTotalCreditTo)
    }
    val students = CollectUtils.newHashSet(entityDao.search(stdBuilder))
    val plans = getCoursePlans(students)
    for (stdTotalCreditConstraint <- constraints) {
      val std = stdTotalCreditConstraint.getStd
      students.remove(std)
    }
    for (std <- students) {
      val plan = plans.get(std.id)
      val stdTotalCreditConstraint = Model.newInstance(classOf[StdTotalCreditConstraint])
      stdTotalCreditConstraint.setStd(std)
      stdTotalCreditConstraint.setMaxCredit(if (null == plan) 0 else plan.getCredits)
      constraints.add(stdTotalCreditConstraint)
    }
    try {
      val createdAt = new Date()
      val loggers = CollectUtils.newArrayList()
      for (stdTotalCreditConstraint <- constraints) {
        val logger = ConstraintLogger.genLogger(stdTotalCreditConstraint, "INIT")
        logger.setCreatedAt(createdAt)
        logger.setOperator(SecurityUtils.getUsername)
        loggers.add(logger)
      }
      entityDao.execute(Operation.saveOrUpdate(constraints).saveOrUpdate(loggers))
      "info.save.success"
    } catch {
      case e: Exception => {
        e.printStackTrace()
        "info.save.failure"
      }
    }
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setRestrictionHelper(restrictionHelper: RestrictionHelper) {
    this.restrictionHelper = restrictionHelper
  }

  def getCoursePlans(students: Iterable[Student]): Map[Long, CoursePlan] = {
    val plans = CollectUtils.newHashMap()
    val stdPlans = coursePlanProvider.getCoursePlans(students)
    for ((key, value) <- stdPlans) {
      plans.put(key.id, value)
    }
    plans
  }

  private def get[T](builder: OqlBuilder[T], multipleParamName: String, multipleParamValue: AnyRef*): List[T] = {
    val rs = CollectUtils.newArrayList()
    var i = 0
    while (i < multipleParamValue.length) {
      var end = i + 500
      if (end > multipleParamValue.length) {
        end = multipleParamValue.length
      }
      val parameterMap = CollectUtils.newHashMap()
      parameterMap.put(multipleParamName, ArrayUtils.subarray(multipleParamValue, i, end))
      rs.addAll(entityDao.search(builder.params(parameterMap).build()))
      i += 500
    }
    rs
  }

  def setMajorPlanSearchHelper(majorPlanSearchHelper: MajorPlanSearchHelper) {
    this.majorPlanSearchHelper = majorPlanSearchHelper
  }
}
