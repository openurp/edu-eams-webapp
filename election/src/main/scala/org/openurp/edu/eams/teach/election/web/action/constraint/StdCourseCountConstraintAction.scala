package org.openurp.edu.eams.teach.election.web.action.constraint

import java.util.Date



import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.ListUtils
import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.StudentJournal
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.election.model.Enum.ConstraintType
import org.openurp.edu.eams.teach.election.model.constraint.ConstraintLogger
import org.openurp.edu.eams.teach.election.model.constraint.StdCourseCountConstraint
import org.openurp.edu.eams.teach.election.service.CourseTakeService
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import com.opensymphony.xwork2.ActionContext



class StdCourseCountConstraintAction extends SemesterSupportAction {

  private var coursePlanProvider: CoursePlanProvider = _

  private var courseTakeService: CourseTakeService = _

  protected override def getEntityName(): String = {
    classOf[StdCourseCountConstraint].getName
  }

  protected def indexSetting() {
    putSemester(null)
    put("projects", getProjects)
    put("constraintTypes", ConstraintType.values)
  }

  protected def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(getEntityName, getShortName)
    populateConditions(builder)
    builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    builder.where("stdCourseCountConstraint.semester=:semester", putSemester(null))
    builder
  }

  def save(): String = {
    val stdCourseCountConstraint = populateEntity().asInstanceOf[StdCourseCountConstraint]
    var stds = CollectUtils.newArrayList()
    val persistedConstraintStds = CollectUtils.newHashSet()
    var stdIds: Array[Long] = null
    val stdCourseCountConstraints = CollectUtils.newArrayList()
    val semester = stdCourseCountConstraint.getSemester
    if (true == getBoolean("allNoCredit")) {
      stds = entityDao.search(getStdBuilder(semester, null, true))
    } else {
      stdIds = Strings.splitToLong(get("stdIds"))
      if (ArrayUtils.isNotEmpty(stdIds)) {
        stds = entityDao.get(classOf[Student], stdIds)
        val builder = OqlBuilder.from(classOf[StdCourseCountConstraint], "stdCourseCountConstraint")
        builder.where("stdCourseCountConstraint.std in (:stds)")
          .where("stdCourseCountConstraint.semester=:semester")
        val stdArray = stds.toArray(Array())
        var i = 0
        while (i < stdArray.length) {
          var end = i + 500
          if (end > stdArray.length) {
            end = stdArray.length
          }
          val parameterMap = CollectUtils.newHashMap()
          parameterMap.put("stds", ArrayUtils.subarray(stdArray, i, end))
          parameterMap.put("semester", semester)
          stdCourseCountConstraints.addAll(entityDao.search(builder.params(parameterMap).build()))
          i += 500
        }
        for (constraint <- stdCourseCountConstraints) {
          persistedConstraintStds.add(constraint.getStd)
        }
      }
    }
    for (std <- stds if !persistedConstraintStds.contains(std)) {
      val constraint = Model.newInstance(classOf[StdCourseCountConstraint])
      constraint.setSemester(semester)
      constraint.setStd(std)
      stdCourseCountConstraints.add(constraint)
    }
    val createdAt = new Date()
    val loggers = CollectUtils.newArrayList()
    val maxCourseCount = getCourseTypeMaxCourseCount
    for (constraint <- stdCourseCountConstraints) {
      val logger = ConstraintLogger.genLogger(constraint, if (constraint.isPersisted) "UPDATE" else "CREATE")
      logger.setCreatedAt(createdAt)
      logger.setOperator(getUsername)
      loggers.add(logger)
      val removedCourseTypeLimits = ListUtils.removeAll(constraint.getCourseTypeMaxCourseCount.keySet, 
        maxCourseCount.keySet)
      for (courseType <- removedCourseTypeLimits) {
        val courseTypeLogger = ConstraintLogger.genLogger(constraint, courseType, "DELETE")
        courseTypeLogger.setCreatedAt(createdAt)
        courseTypeLogger.setOperator(getUsername)
        loggers.add(logger)
      }
      for (courseType <- constraint.getCourseTypeMaxCourseCount.keySet if !removedCourseTypeLimits.contains(courseType)) {
        val `type` = if (null == 
          constraint.getCourseTypeMaxCourseCount.get(courseType)) "CREATE" else "UPDATE"
        val courseTypeLogger = ConstraintLogger.genLogger(constraint, courseType, `type`)
        courseTypeLogger.setCreatedAt(createdAt)
        courseTypeLogger.setOperator(getUsername)
        loggers.add(logger)
      }
      constraint.setCourseTypeMaxCourseCount(maxCourseCount)
      constraint.setMaxCourseCount(stdCourseCountConstraint.getMaxCourseCount)
    }
    try {
      entityDao.execute(Operation.saveOrUpdate(stdCourseCountConstraints).saveOrUpdate(loggers))
      return redirect("search", "info.save.success")
    } catch {
      case e: Exception => 
    }
    redirect("search", "info.save.failure")
  }

  private def calStdsElectedCourseTypeCourseCount(stdCourseTakes: Map[Student, List[CourseTake]], stdPlans: Map[Student, CoursePlan]): Map[Student, Map[CourseType, Integer]] = {
    val stds = stdPlans.keySet
    val result = CollectUtils.newHashMap()
    for (student <- stds) {
      val plan = stdPlans.get(student)
      if (null != plan) {
        result.put(student, calStdElectedCourseTypeCourseCount(student, plan, stdCourseTakes.get(student)))
      } else {
        result.put(student, new HashMap[CourseType, Integer]())
      }
    }
    result
  }

  private def calStdElectedCourseTypeCourseCount(student: Student, plan: CoursePlan, courseTakes: List[CourseTake]): Map[CourseType, Integer] = {
    val groups = plan.getGroups
    val result = CollectUtils.newHashMap()
    for (courseGroup <- groups) {
      result.put(courseGroup.getCourseType, 0)
    }
    for (courseTake <- courseTakes if courseTake.getCourseTakeType.id != CourseTakeType.RESTUDY) {
      var `type` = courseTake.getLesson.getCourse.getCourseType
      var count = result.get(`type`)
      count = if (count == null) 0 else count
      if (null == result.get(`type`)) {
        `type` = courseTake.getLesson.getCourseType
      }
      result.put(`type`, count)
    }
    result
  }

  private def getCourseTypeMaxCourseCount(): Map[CourseType, Integer] = {
    val result = CollectUtils.newHashMap()
    var i = -1
    val params = ActionContext.getContext.getParameters
    for (key <- params.keySet if key.startsWith("courseTypeId")) {
      i += 1
    }
    while (i >= 0) {
      val courseTypeId = getInt("courseTypeId" + i)
      val limitCount = getInt("courseTypeLimitCount" + i)
      if (null != courseTypeId && null != limitCount) {
        result.put(Model.newInstance(classOf[CourseType], courseTypeId), limitCount)
      }
      i -= 1
    }
    result
  }

  protected override def editSetting(entity: Entity[_]) {
    val allNoCredit = getBoolean("allNoCredit")
    val entityIds = getLongIds(getShortName)
    val semester = putSemester(null)
    val idsLength = if (ArrayUtils.isEmpty(entityIds)) 0 else entityIds.length
    var stds = CollectUtils.newArrayList()
    if (null == allNoCredit) {
      if (idsLength == 1) {
        val stdCourseCountConstraint = entity.asInstanceOf[StdCourseCountConstraint]
        put("stdCourseCountConstraint", stdCourseCountConstraint)
        if (stdCourseCountConstraint.isPersisted) {
          stds.add(stdCourseCountConstraint.getStd)
        }
      } else {
        val project = getProject
        val stdTypes = getStdTypes
        val educations = getEducations
        val departments = getDeparts
        if (null == project || stdTypes.isEmpty || educations.isEmpty || 
          departments.isEmpty) {
          put("stds", stds)
        }
        val stdCourseCountConstraints = CollectUtils.newArrayList()
        var i = 0
        while (i < entityIds.length) {
          var end = i + 500
          if (end > entityIds.length) {
            end = entityIds.length
          }
          val stdCourseCountConstraintBuilder = OqlBuilder.from(classOf[StdCourseCountConstraint], "stdCourseCountConstraint")
          stdCourseCountConstraintBuilder.where("stdCourseCountConstraint.std.project =:project", project)
          stdCourseCountConstraintBuilder.where("stdCourseCountConstraint.std.department in(:departments)", 
            departments)
          stdCourseCountConstraintBuilder.where("stdCourseCountConstraint.std.type in(:types)", stdTypes)
          stdCourseCountConstraintBuilder.where("stdCourseCountConstraint.std.education in(:educations)", 
            educations)
          stdCourseCountConstraintBuilder.where("stdCourseCountConstraint.id in(:ids)", ArrayUtils.subarray(entityIds, 
            i, end))
          stdCourseCountConstraintBuilder.where("stdCourseCountConstraint.semester=:semester", semester)
          populateConditions(stdCourseCountConstraintBuilder)
          stdCourseCountConstraints.addAll(entityDao.search(stdCourseCountConstraintBuilder))
          i += 500
        }
        for (stdCourseCountConstraint <- stdCourseCountConstraints) {
          stds.add(stdCourseCountConstraint.getStd)
        }
        put("allNoCredit", false)
      }
    } else {
      val stdIds = getLongIds("student")
      put("allNoCredit", allNoCredit)
      val stdSet = CollectUtils.newHashSet()
      if (ArrayUtils.isNotEmpty(stdIds)) {
        var i = 0
        while (i < stdIds.length) {
          var end = i + 500
          if (end > stdIds.length) {
            end = stdIds.length
          }
          stdSet.addAll(entityDao.search(getStdBuilder(semester, ArrayUtils.subarray(stdIds, i, end), 
            true)))
          i += 500
        }
      } else {
        stdSet.addAll(entityDao.search(getStdBuilder(semester, null, true)))
      }
      stds = CollectUtils.newArrayList(stdSet)
    }
    val stdCourseTakes = courseTakeService.getCourseTakes(stds, semester)
    put("electedCount", getStdsElectedCourseCount(stdCourseTakes))
    put("electedCourseTypeCount", calStdsElectedCourseTypeCourseCount(stdCourseTakes, coursePlanProvider.getCoursePlans(stds)))
    put("courseTypes", getCourseTypes(stds))
    put("stds", stds)
  }

  private def getCourseTypes(stds: Iterable[Student]): List[CourseType] = {
    if (CollectUtils.isEmpty(stds)) {
      return Collections.emptyList()
    }
    val plans = CollectUtils.newHashSet(coursePlanProvider.getCoursePlans(stds).values)
    val typePlanMap = CollectUtils.newHashMap()
    for (coursePlan <- plans) {
      val groups = coursePlan.getGroups
      for (courseGroup <- groups) {
        var typePlans = typePlanMap.get(courseGroup.getCourseType)
        if (null == typePlans) {
          typePlans = CollectUtils.newHashSet()
          typePlanMap.put(courseGroup.getCourseType, typePlans)
        }
        typePlans.add(coursePlan)
      }
    }
    val result = CollectUtils.newArrayList()
    for ((key, value) <- typePlanMap if null != value && CollectionUtils.isEqualCollection(plans, value)) {
      result.add(key)
    }
    result
  }

  def ajaxQueryStds(): String = {
    val codes = Strings.split(get("stdCodes"), ",")
    val project = getProject
    val stdTypes = getStdTypes
    val educations = getEducations
    val departments = getDeparts
    if (null == project || stdTypes.isEmpty || educations.isEmpty || 
      departments.isEmpty) {
      put("message", "权限不足")
      return forward()
    } else if (ArrayUtils.isNotEmpty(codes)) {
      val stds = entityDao.get(classOf[Student], Array("code", "project", "department", "type", "education"), 
        Array(codes, project, departments, stdTypes, educations))
      val stdMap = CollectUtils.newHashMap()
      for (student <- stds) {
        stdMap.put(student.getCode, student)
      }
      for (code <- codes if !stdMap.containsKey(code)) {
        stdMap.put(code.toString, null)
      }
      val semester = putSemester(null)
      val stdCourseTakes = courseTakeService.getCourseTakes(stds, semester)
      put("electedCount", getStdsElectedCourseCount(stdCourseTakes))
      put("courseTypes", getCourseTypes(stds))
      put("stdMap", stdMap)
    } else {
      put("message", "没有学号")
    }
    forward()
  }

  private def getStdsElectedCourseCount(stdCourseTakes: Map[Student, List[CourseTake]]): Map[Student, Integer] = {
    val result = CollectUtils.newHashMap()
    if (null == stdCourseTakes || stdCourseTakes.isEmpty) {
      return result
    }
    for (student <- stdCourseTakes.keySet) {
      val courseTakes = stdCourseTakes.get(student)
      for (courseTake <- courseTakes if courseTake.getCourseTakeType.id != CourseTakeType.RESTUDY) {
        val count = result.get(courseTake.getStd)
        result.put(courseTake.getStd, if (null == count) 1 else (count + 1))
      }
    }
    result
  }

  def noStdCourseCountConstraintStdList(): String = {
    val semester = putSemester(null)
    if (null == semester) {
      forwardError("error.parameters.illegal")
    } else {
      val builder = getStdBuilder(semester, null, true)
      val conditions = QueryHelper.extractConditions(classOf[StdCourseCountConstraint], "stdCourseCountConstraint", 
        "stdCourseCountConstraint.id,stdCourseCountConstraint.semester.id")
      for (condition <- conditions) {
        builder.where(condition.getContent.replaceAll("stdCourseCountConstraint.std", "student"), condition.getParams)
      }
      put("stds", entityDao.search(builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)))
      forward()
    }
  }

  private def getStdBuilder(semester: Semester, stdIds: Array[Any], noCreditsConstraint: Boolean): OqlBuilder[Student] = {
    val departs = getDeparts
    val stdTypes = getStdTypes
    val educations = getEducations
    val builder = OqlBuilder.from(classOf[Student], "student")
    if (CollectUtils.isEmpty(departs) || CollectUtils.isEmpty(stdTypes) || 
      CollectUtils.isEmpty(educations)) {
      builder.where("1=2")
    } else {
      builder.where((if (noCreditsConstraint) "not " else "") + "exists (from " + 
        classOf[StdCourseCountConstraint].getName + 
        " stdCourseCountConstraint " + 
        "where stdCourseCountConstraint.std=student " + 
        "and stdCourseCountConstraint.semester=:semester)", semester)
      populateConditions(builder)
      builder.where("student.project=:project", getProject)
      builder.where("student.department in (:departs) and student.type in(:stdTypes) and student.education in(:educations)", 
        departs, stdTypes, educations)
      builder.where("exists (from " + classOf[StudentJournal].getName + 
        " journal where journal.std=student and journal.beginOn <= :semEndOn and journal.endOn >= :semBeginOn and journal.endOn is not null)", 
        semester.getEndOn, semester.beginOn)
      if (ArrayUtils.isNotEmpty(stdIds)) {
        builder.where("student.id in (:stdIds)", stdIds)
      }
    }
    builder
  }

  def setMaxCourseCount(): String = {
    val ids = getLongIds(getShortName)
    val maxCourseCount = getInt("maxCourseCount")
    if (ArrayUtils.isNotEmpty(ids) && null != maxCourseCount && 
      maxCourseCount > -1) {
      val constraints = entityDao.get(classOf[StdCourseCountConstraint], ids)
      val loggers = CollectUtils.newArrayList()
      val createdAt = new Date()
      for (stdCourseCountConstraint <- constraints) {
        val logger = ConstraintLogger.genLogger(stdCourseCountConstraint, if (stdCourseCountConstraint.isPersisted) "UPDATE" else "CREATE")
        logger.setCreatedAt(createdAt)
        logger.setOperator(getUsername)
        loggers.add(logger)
        stdCourseCountConstraint.setMaxCourseCount(maxCourseCount)
      }
      try {
        entityDao.execute(Operation.saveOrUpdate(constraints).saveOrUpdate(loggers))
        return redirect("search", "info.save.success")
      } catch {
        case e: Exception => 
      }
    }
    redirect("search", "info.save.failure")
  }

  def initCourseCountConstraint(): String = {
    val semester = putSemester(null)
    val project = getProject
    val departs = getDeparts
    val stdTypes = getStdTypes
    val educations = getEducations
    val constraintBuilder = OqlBuilder.from(classOf[StdCourseCountConstraint], "stdCourseCountConstraint")
    val conditions = QueryHelper.extractConditions(constraintBuilder.getEntityClass, constraintBuilder.getAlias, 
      "stdCourseCountConstraint.id")
    constraintBuilder.where("stdCourseCountConstraint.std.project = :project", project)
      .where("stdCourseCountConstraint.std.type in (:stdTypes)", stdTypes)
      .where("stdCourseCountConstraint.std.department in (:departs)", departs)
      .where("stdCourseCountConstraint.std.education in  (:educations)", educations)
      .where("stdCourseCountConstraint.semester=:semester", semester)
      .where("exists (from " + classOf[StudentJournal].getName + 
      " journal where journal.std=stdCourseCountConstraint.std and journal.beginOn <= :semEndOn and journal.endOn >= :semBeginOn and journal.endOn is not null)", 
      semester.getEndOn, semester.beginOn)
      .where(conditions)
    val constraints = entityDao.search(constraintBuilder)
    val stdBuilder = OqlBuilder.from(classOf[Student].getName, "student")
    stdBuilder.where("exists (from " + classOf[StudentJournal].getName + 
      " journal where journal.std=student and journal.beginOn <= :semEndOn and journal.endOn >= :semBeginOn and journal.endOn is not null)", 
      semester.getEndOn, semester.beginOn)
    for (condition <- conditions if condition.getContent.startsWith("stdCourseCountConstraint.std")) {
      val majorPlanCondition = new Condition(condition.getContent.replace("stdCourseCountConstraint.std", 
        "student"))
      majorPlanCondition.params(condition.getParams)
      stdBuilder.where(majorPlanCondition)
    }
    val students = CollectUtils.newHashSet(entityDao.search(stdBuilder))
    for (constraint <- constraints) {
      students.remove(constraint.getStd)
    }
    for (std <- students) {
      val constraint = Model.newInstance(classOf[StdCourseCountConstraint])
      constraint.setSemester(semester)
      constraint.setStd(std)
      constraints.add(constraint)
    }
    try {
      val createdAt = new Date()
      val loggers = CollectUtils.newArrayList()
      for (constraint <- constraints) {
        val logger = ConstraintLogger.genLogger(constraint, "INIT")
        logger.setCreatedAt(createdAt)
        logger.setOperator(getUsername)
        logger.setValue(String.valueOf(constraint.getMaxCourseCount))
        loggers.add(logger)
        for (courseType <- constraint.getCourseTypeMaxCourseCount.keySet) {
          val `type` = "INIT"
          val courseTypeLogger = ConstraintLogger.genLogger(constraint, courseType, `type`)
          courseTypeLogger.setCreatedAt(createdAt)
          courseTypeLogger.setOperator(getUsername)
          courseTypeLogger.setValue(String.valueOf(constraint.getCourseTypeMaxCourseCount.get(courseType)))
          loggers.add(logger)
        }
      }
      entityDao.execute(Operation.saveOrUpdate(constraints).saveOrUpdate(loggers))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        return redirect("search", "info.save.failure")
      }
    }
    redirect("search", "info.save.success")
  }

  protected def removeAndForward(entities: Iterable[_]): String = {
    try {
      val loggers = CollectUtils.newArrayList()
      val createdAt = new Date()
      for (`object` <- entities) {
        val constraint = `object`.asInstanceOf[StdCourseCountConstraint]
        constraint.getCourseTypeMaxCourseCount.clear()
        for (courseType <- constraint.getCourseTypeMaxCourseCount.keySet) {
          val logger = ConstraintLogger.genLogger(constraint, courseType, "DELETE")
          logger.setOperator(getUsername)
          logger.setCreatedAt(createdAt)
          loggers.add(logger)
        }
        val logger = ConstraintLogger.genLogger(constraint, "DELETE")
        logger.setOperator(getUsername)
        logger.setCreatedAt(createdAt)
        loggers.add(logger)
      }
      entityDao.execute(Operation.saveOrUpdate(entities).remove(entities).saveOrUpdate(loggers))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        logger.info("removeAndForwad failure", e)
        return redirect("search", "info.delete.failure")
      }
    }
    redirect("search", "info.remove.success")
  }

  def setCoursePlanProvider(coursePlanProvider: CoursePlanProvider) {
    this.coursePlanProvider = coursePlanProvider
  }

  def setCourseTakeService(courseTakeService: CourseTakeService) {
    this.courseTakeService = courseTakeService
  }
}
