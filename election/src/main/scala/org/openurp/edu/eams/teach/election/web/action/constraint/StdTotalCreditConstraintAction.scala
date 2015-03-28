package org.openurp.edu.eams.teach.election.web.action.constraint

import java.util.Date



import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.Collections
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
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.teach.election.model.Enum.ConstraintType
import org.openurp.edu.eams.teach.election.model.constraint.ConstraintLogger
import org.openurp.edu.eams.teach.election.model.constraint.StdTotalCreditConstraint
import org.openurp.edu.eams.teach.election.service.CreditConstraintService
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class StdTotalCreditConstraintAction extends SemesterSupportAction {

  var creditConstraintService: CreditConstraintService = _

  protected override def getEntityName(): String = {
    classOf[StdTotalCreditConstraint].getName
  }

  protected def indexSetting() {
    putSemester(null)
    put("projects", getProjects)
    put("constraintTypes", ConstraintType.values)
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = super.getQueryBuilder
    val stdTypes = getStdTypes
    val departs = getDeparts
    val educations = getEducations
    val project = getProject
    if (Collections.isEmpty(stdTypes) || Collections.isEmpty(departs) || 
      Collections.isEmpty(educations) || 
      null == project) {
      builder.where("1=2")
    } else {
      val maxFrom = getFloat("maxFrom")
      if (null != maxFrom) {
        builder.where("stdTotalCreditConstraint.maxTotalCredit >=:maxFrom", maxFrom)
      }
      val maxTo = getFloat("maxTo")
      if (null != maxTo) {
        builder.where("stdTotalCreditConstraint.maxTotalCredit <=:maxTo", maxTo)
      }
      val electedTotalCreditFrom = getFloat("electedTotalCreditFrom")
      if (null != electedTotalCreditFrom) {
        builder.where("COALESCE((select sum(courseTake.lesson.course.credit) " + 
          "from " + 
          classOf[CourseTake].getName + 
          " courseTake " + 
          "where courseTake.std=stdTotalCreditConstraint.std),0) >=:electedTotalCreditFrom", electedTotalCreditFrom)
      }
      val electedTotalCreditTo = getFloat("electedTotalCreditTo")
      if (null != electedTotalCreditTo) {
        builder.where("COALESCE((select sum(courseTake.lesson.course.credit) " + 
          "from " + 
          classOf[CourseTake].getName + 
          " courseTake " + 
          "where courseTake.std=stdTotalCreditConstraint.std),0) <=:electedTotalCreditTo", electedTotalCreditTo)
      }
      builder.where("stdTotalCreditConstraint.std.project=:project", project)
      builder.where("stdTotalCreditConstraint.std.type in(:stdTypes) " + 
        "and stdTotalCreditConstraint.std.department in(:departs) " + 
        "and stdTotalCreditConstraint.std.education in(:educations)", stdTypes, departs, educations)
    }
    builder
  }

  protected override def editSetting(entity: Entity[_]) {
    val allNoCredit = getBoolean("allNoCredit")
    val entityIds = getLongIds(getShortName)
    val idsLength = if (ArrayUtils.isEmpty(entityIds)) 0 else entityIds.length
    if (null == allNoCredit) {
      if (idsLength == 1) {
        val stdTotalCreditConstraint = entity.asInstanceOf[StdTotalCreditConstraint]
        put("stdTotalCreditConstraint", stdTotalCreditConstraint)
        if (stdTotalCreditConstraint.isPersisted) {
          put("electedCredits", getStdsElectedCredits(Collections.newBuffer[Any](stdTotalCreditConstraint.getStd)))
        }
      } else {
        val project = getProject
        val stdTypes = getStdTypes
        val educations = getEducations
        val departments = getDeparts
        if (null == project || stdTypes.isEmpty || educations.isEmpty || 
          departments.isEmpty) {
          put("stds", Collections.emptyList())
          put("electedCredits", Collections.emptyMap())
        }
        val stdTotalCreditConstraints = Collections.newBuffer[Any]
        var i = 0
        while (i < entityIds.length) {
          var end = i + 500
          if (end > entityIds.length) {
            end = entityIds.length
          }
          val stdTotalCreditConstraintBuilder = OqlBuilder.from(classOf[StdTotalCreditConstraint], "stdTotalCreditConstraint")
          stdTotalCreditConstraintBuilder.where("stdTotalCreditConstraint.std.project =:project", project)
          stdTotalCreditConstraintBuilder.where("stdTotalCreditConstraint.std.department in(:departments)", 
            departments)
          stdTotalCreditConstraintBuilder.where("stdTotalCreditConstraint.std.type in(:types)", stdTypes)
          stdTotalCreditConstraintBuilder.where("stdTotalCreditConstraint.std.education in(:educations)", 
            educations)
          stdTotalCreditConstraintBuilder.where("stdTotalCreditConstraint.id in(:ids)", ArrayUtils.subarray(entityIds, 
            i, end))
          populateConditions(stdTotalCreditConstraintBuilder)
          stdTotalCreditConstraints.addAll(entityDao.search(stdTotalCreditConstraintBuilder))
          i += 500
        }
        val stds = Collections.newBuffer[Any]
        for (stdTotalCreditConstraint <- stdTotalCreditConstraints) {
          stds.add(stdTotalCreditConstraint.getStd)
        }
        put("allNoCredit", false)
        put("stds", stds)
        put("electedCredits", getStdsElectedCredits(stds))
      }
    } else {
      put("allNoCredit", allNoCredit)
      val stds = entityDao.search(getStdBuilder(getLongIds("student"), true))
      put("stds", stds)
      put("electedCredits", getStdsElectedCredits(stds))
    }
  }

  private def getStdsElectedCredits(stds: List[Student]): Map[Student, Float] = {
    val electedCredits = Collections.newMap[Any]
    if (!stds.isEmpty) {
      val courseTakes = entityDao.get(classOf[CourseTake], "std", stds)
      for (courseTake <- courseTakes) {
        val credits = courseTake.getLesson.getCourse.getCredits
        val electedCredit = electedCredits.get(courseTake.getStd)
        electedCredits.put(courseTake.getStd, if (null == electedCredit) credits else electedCredit + credits)
      }
    }
    electedCredits
  }

  private def getStdBuilder(stdIds: Array[Long], noCreditsConstraint: Boolean): OqlBuilder[Student] = {
    val departs = getDeparts
    val stdTypes = getStdTypes
    val educations = getEducations
    val builder = OqlBuilder.from(classOf[Student], "student")
    if (Collections.isEmpty(departs) || Collections.isEmpty(stdTypes) || 
      Collections.isEmpty(educations)) {
      builder.where("1=2")
    } else {
      builder.where((if (noCreditsConstraint) "not " else "") + "exists (from " + 
        classOf[StdTotalCreditConstraint].getName + 
        " stdTotalCreditConstraint " + 
        "where stdTotalCreditConstraint.std=student)")
      populateConditions(builder)
      builder.where("student.project=:project", getProject)
      builder.where("student.department in (:departs) and student.type in(:stdTypes) and student.education in(:educations)", 
        departs, stdTypes, educations)
      if (ArrayUtils.isNotEmpty(stdIds)) {
        builder.where("student.id in (:stdIds)", stdIds)
      }
    }
    builder
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
      val stdMap = Collections.newMap[Any]
      for (student <- stds) {
        stdMap.put(student.getCode, student)
      }
      for (code <- codes if !stdMap.containsKey(code)) {
        stdMap.put(code.toString, null)
      }
      put("electedCredits", getStdsElectedCredits(stds))
      put("stdMap", stdMap)
    } else {
      put("message", "没有学号")
    }
    forward()
  }

  def noStdTotalCreditConstraintStdList(): String = {
    val semester = putSemester(null)
    if (null == semester) {
      forwardError("error.parameters.illegal")
    } else {
      val builder = getStdBuilder(null, true)
      val conditions = QueryHelper.extractConditions(classOf[StdTotalCreditConstraint], "stdTotalCreditConstraint", 
        null)
      for (condition <- conditions) {
        builder.where(condition.getContent.replaceAll("stdTotalCreditConstraint.std", "student"), condition.getParams)
      }
      put("stds", entityDao.search(builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)))
      forward()
    }
  }

  def save(): String = {
    val stdTotalCreditConstraint = populateEntity().asInstanceOf[StdTotalCreditConstraint]
    var stds = Collections.newBuffer[Any]

    val persistedConstraintStds = Collections.newSet[Any]
    var stdIds: Array[Long] = null
    var stdTotalCreditConstraints = Collections.newBuffer[Any]
    if (true == getBoolean("allNoCredit")) {
      stds = entityDao.search(getStdBuilder(null, true))
    } else {
      stdIds = Strings.splitToLong(get("stdIds"))
      if (ArrayUtils.isNotEmpty(stdIds)) {
        stds = entityDao.get(classOf[Student], stdIds)
        stdTotalCreditConstraints = entityDao.get(classOf[StdTotalCreditConstraint], "std", stds)
        for (constraint <- stdTotalCreditConstraints) {
          persistedConstraintStds.add(constraint.getStd)
        }
      }
    }
    for (std <- stds if !persistedConstraintStds.contains(std)) {
      val constraint = Model.newInstance(classOf[StdTotalCreditConstraint])
      constraint.setStd(std)
      stdTotalCreditConstraints.add(constraint)
    }
    val createdAt = new Date()
    val loggers = Collections.newBuffer[Any]
    for (constraint <- stdTotalCreditConstraints) {
      constraint.setMaxCredit(stdTotalCreditConstraint.getMaxCredit)
      val logger = ConstraintLogger.genLogger(constraint, if (constraint.isPersisted) "UPDATE" else "CREATE")
      logger.setCreatedAt(createdAt)
      logger.setOperator(getUsername)
      loggers.add(logger)
    }
    try {
      entityDao.execute(Operation.saveOrUpdate(stdTotalCreditConstraints).saveOrUpdate(loggers))
      return redirect("search", "info.save.success")
    } catch {
      case e: Exception => e.printStackTrace()
    }
    redirect("search", "info.save.failure")
  }

  def setMaxCredit(): String = {
    val maxCredit = getFloat("maxCredit")
    val ids = getLongIds(getShortName)
    if (null == maxCredit) {
      return redirect("search", "学分上限不能为空")
    }
    if (ArrayUtils.isNotEmpty(ids)) {
      val createdAt = new Date()
      val loggers = Collections.newBuffer[Any]
      val constraints = entityDao.get(classOf[StdTotalCreditConstraint], ids)
      for (stdTotalCreditConstraint <- constraints) {
        stdTotalCreditConstraint.setMaxCredit(maxCredit)
        val logger = ConstraintLogger.genLogger(stdTotalCreditConstraint, if (stdTotalCreditConstraint.isPersisted) "UPDATE" else "CREATE")
        logger.setCreatedAt(createdAt)
        logger.setOperator(getUsername)
        loggers.add(logger)
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

  def statTotalCredit(): String = {
    val entityIds = getLongIds(getShortName)
    var stdTotalCreditConstraints: List[StdTotalCreditConstraint] = null
    stdTotalCreditConstraints = if (ArrayUtils.isEmpty(entityIds)) entityDao.getAll(classOf[StdTotalCreditConstraint]) else entityDao.get(classOf[StdTotalCreditConstraint], 
      entityIds)
    val stds = Collections.newSet[Any]
    val constraintMap = Collections.newMap[Any]
    val createdAt = new Date()
    val loggers = Collections.newBuffer[Any]
    for (stdTotalCreditConstraint <- stdTotalCreditConstraints) {
      stds.add(stdTotalCreditConstraint.getStd)
      constraintMap.put(stdTotalCreditConstraint.getStd, stdTotalCreditConstraint)
      val logger = ConstraintLogger.genLogger(stdTotalCreditConstraint, if (stdTotalCreditConstraint.isPersisted) "UPDATE" else "CREATE")
      logger.setCreatedAt(createdAt)
      logger.setOperator(getUsername)
      loggers.add(logger)
    }
    if (!stds.isEmpty) {
      try {
        entityDao.execute(Operation.saveOrUpdate(stdTotalCreditConstraints).saveOrUpdate(loggers))
      } catch {
        case e: Exception => return redirect("search", "info.save.failure")
      }
    }
    redirect("search", "info.save.success")
  }

  def initConstraint(): String = {
    redirect("search", creditConstraintService.initStdTotalCreditConstraint(getProject))
  }

  protected def removeAndForward(entities: Iterable[_]): String = {
    try {
      val loggers = Collections.newBuffer[Any]
      val createdAt = new Date()
      for (`object` <- entities) {
        val constraint = `object`.asInstanceOf[StdTotalCreditConstraint]
        val logger = ConstraintLogger.genLogger(constraint, "DELETE")
        logger.setOperator(getUsername)
        logger.setCreatedAt(createdAt)
        loggers.add(logger)
      }
      entityDao.execute(Operation.remove(entities).saveOrUpdate(loggers))
    } catch {
      case e: Exception => {
        logger.info("removeAndForwad failure", e)
        return redirect("search", "info.delete.failure")
      }
    }
    redirect("search", "info.remove.success")
  }
}
