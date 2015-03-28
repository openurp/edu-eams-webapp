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
import org.beangle.commons.lang.Arrays
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
import org.openurp.edu.eams.teach.election.model.constraint.StdCreditConstraint
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.service.GpaStatService
import org.openurp.edu.eams.teach.grade.service.impl.MultiStdGpa
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class StdCreditConstraintAction extends SemesterSupportAction {

  var gpaStatService: GpaStatService = _

  protected override def getEntityName(): String = classOf[StdCreditConstraint].getName

  override def indexSetting() {
    putSemester(null)
    put("projects", getProjects)
    put("constraintTypes", ConstraintType.values)
  }

  override def search(): String = {
    val semester = putSemester(null)
    val builder = OqlBuilder.from(classOf[StdCreditConstraint], "stdCreditConstraint")
    val project = getProject
    val stdTypes = getStdTypes
    val educations = getEducations
    val departments = getDeparts
    if (null == project || stdTypes.isEmpty || educations.isEmpty || 
      departments.isEmpty) {
      builder.where("1=2")
    } else {
      builder.where("stdCreditConstraint.std.project=:project ", project)
        .where("stdCreditConstraint.std.type in(:stdTypes) " + 
        "and stdCreditConstraint.std.education in(:educations) " + 
        "and stdCreditConstraint.std.department in(:departs)", stdTypes, educations, departments)
      populateConditions(builder)
      val maxFrom = getFloat("maxFrom")
      if (null != maxFrom) {
        builder.where("stdCreditConstraint.maxCredit >=:maxFrom", maxFrom)
      }
      val maxTo = getFloat("maxTo")
      if (null != maxTo) {
        builder.where("stdCreditConstraint.maxCredit <=:maxTo", maxTo)
      }
      val electedFrom = getFloat("electedFrom")
      if (null != electedFrom) {
        builder.where("COALESCE((select sum(courseTake.lesson.course.credit) " + 
          "from " + 
          classOf[CourseTake].getName + 
          " courseTake " + 
          "where courseTake.std=stdCreditConstraint.std),0) >=:electedFrom " + 
          "and courseTake.lesson.semester=:semester", electedFrom, semester)
      }
      val electedTo = getFloat("electedTo")
      if (null != electedTo) {
        builder.where("COALESCE((select sum(courseTake.lesson.course.credit) " + 
          "from " + 
          classOf[CourseTake].getName + 
          " courseTake " + 
          "where courseTake.std=stdCreditConstraint.std),0) <=:electedTotalCreditTo" + 
          "and courseTake.lesson.semester=:semester", electedFrom, semester)
      }
      val gpaFrom = getFloat("gpaFrom")
      if (null != gpaFrom) {
        builder.where("stdCreditConstraint.GPA >=:gpaFrom", gpaFrom)
      }
      val gpaTo = getFloat("gpaTo")
      if (null != gpaTo) {
        builder.where("stdCreditConstraint.GPA <=:gpaTo", gpaTo)
      }
      builder.where("stdCreditConstraint.semester=:semester", semester)
    }
    builder.limit(getPageLimit)
    builder.orderBy(Order.parse(get(Order.ORDER_STR)))
    put("creditConstrains", entityDao.search(builder))
    forward()
  }

  protected override def editSetting(entity: Entity[_]) {
    val semester = putSemester(null)
    val allNoCredit = getBoolean("allNoCredit")
    val entityIds = getLongIds(getShortName)
    val idsLength = if (ArrayUtils.isEmpty(entityIds)) 0 else entityIds.length
    var stds = Collections.newBuffer[Any]
    if (null == allNoCredit) {
      if (idsLength == 1) {
        val stdCreditConstraint = entity.asInstanceOf[StdCreditConstraint]
        if (stdCreditConstraint.isPersisted) {
          stds.add(stdCreditConstraint.getStd)
        }
        stdCreditConstraint.setSemester(semester)
        put("stdCreditConstraint", stdCreditConstraint)
      } else {
        val project = getProject
        val stdTypes = getStdTypes
        val educations = getEducations
        val departments = getDeparts
        if (null == project || stdTypes.isEmpty || educations.isEmpty || 
          departments.isEmpty) {
          put("stds", Collections.emptyList())
          put("stdGpaMap", Collections.emptyList())
          put("electedCredits", Collections.emptyMap())
        }
        val stdCreditConstraints = Collections.newBuffer[Any]
        var i = 0
        while (i < entityIds.length) {
          var end = i + 500
          if (end > entityIds.length) {
            end = entityIds.length
          }
          val stdCreditConstraintBuilder = OqlBuilder.from(classOf[StdCreditConstraint], "stdCreditConstraint")
          stdCreditConstraintBuilder.where("stdCreditConstraint.semester=:semester", semester)
          stdCreditConstraintBuilder.where("stdCreditConstraint.std.project =:project", project)
          stdCreditConstraintBuilder.where("stdCreditConstraint.std.department in(:departments)", departments)
          stdCreditConstraintBuilder.where("stdCreditConstraint.std.type in(:types)", stdTypes)
          stdCreditConstraintBuilder.where("stdCreditConstraint.std.education in(:educations)", educations)
          stdCreditConstraintBuilder.where("stdCreditConstraint.id in(:ids)", ArrayUtils.subarray(entityIds, 
            i, end))
          populateConditions(stdCreditConstraintBuilder)
          stdCreditConstraints.addAll(entityDao.search(stdCreditConstraintBuilder))
          i += 500
        }
        for (stdCreditConstraint <- stdCreditConstraints) {
          stds.add(stdCreditConstraint.getStd)
        }
        put("allNoCredit", false)
        put("stds", stds)
      }
    } else {
      put("allNoCredit", allNoCredit)
      stds = entityDao.search(getStdBuilder(semester, getLongIds("student"), true))
      put("stds", stds)
    }
    val stdGpaMap = Collections.newMap[Any]
    if (!stds.isEmpty) {
      val multiStdGpa = gpaStatService.statGpas(stds, semester)
      for (stdGpa <- multiStdGpa.getStdGpas) {
        stdGpaMap.put(stdGpa.getStd, stdGpa.getGpa)
      }
    }
    put("stdGpaMap", stdGpaMap)
    put("electedCredits", getStdsElectedCredits(stds))
  }

  private def getStdsElectedCredits(stds: Iterable[Student]): Map[Student, Float] = {
    val electedCredits = Collections.newMap[Any]
    if (!stds.isEmpty) {
      val semester = putSemester(null)
      val values = stds.toArray(Array.ofDim[Student](stds.size))
      var courseTakes = Collections.newBuffer[Any]
      if (values.length < 500) {
        val query = OqlBuilder.from(classOf[CourseTake], "courseTake")
        query.where("courseTake.lesson.semester=:semester", semester)
        query.where("courseTake.std in(:stds)", values)
        courseTakes = entityDao.search(query)
      } else {
        var i = 0
        while (i < values.length) {
          var end = i + 500
          if (end > values.length) end = values.length
          val query = OqlBuilder.from(classOf[CourseTake], "courseTake")
          query.where("courseTake.lesson.semester=:semester", semester)
          query.where("courseTake.std in(:stds)", Arrays.subarray(values, i, end))
          courseTakes.addAll(entityDao.search(query))
          i += 500
        }
      }
      for (courseTake <- courseTakes) {
        val credits = courseTake.getLesson.getCourse.getCredits
        val electedCredit = electedCredits.get(courseTake.getStd)
        electedCredits.put(courseTake.getStd, if (null == electedCredit) credits else electedCredit + credits)
      }
    }
    electedCredits
  }

  def noStdCreditConstraintStdList(): String = {
    val semester = putSemester(null)
    if (null == semester) {
      forwardError("error.parameters.illegal")
    } else {
      val builder = getStdBuilder(semester, null, true)
      val conditions = QueryHelper.extractConditions(classOf[StdCreditConstraint], "stdCreditConstraint", 
        "stdCreditConstraint.id,stdCreditConstraint.semester.id")
      for (condition <- conditions) {
        builder.where(condition.getContent.replaceAll("stdCreditConstraint.std", "student"), condition.getParams)
      }
      put("stds", entityDao.search(builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)))
      forward()
    }
  }

  private def getStdBuilder(semester: Semester, stdIds: Array[Long], noCreditsConstraint: Boolean): OqlBuilder[Student] = {
    val departs = getDeparts
    val stdTypes = getStdTypes
    val educations = getEducations
    val builder = OqlBuilder.from(classOf[Student], "student")
    if (Collections.isEmpty(departs) || Collections.isEmpty(stdTypes) || 
      Collections.isEmpty(educations)) {
      builder.where("1=2")
    } else {
      builder.where((if (noCreditsConstraint) "not " else "") + "exists (from " + 
        classOf[StdCreditConstraint].getName + 
        " stdCreditConstraint " + 
        "where stdCreditConstraint.std=student " + 
        "and stdCreditConstraint.semester=:semester)", semester)
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
      val multiStdGpa = gpaStatService.statGpas(stds, putSemester(null))
      val stdGpaMap = Collections.newMap[Any]
      for (stdGpa <- multiStdGpa.getStdGpas) {
        stdGpaMap.put(stdGpa.getStd, stdGpa.getGpa)
      }
      put("stdGpaMap", stdGpaMap)
      put("electedCredits", getStdsElectedCredits(stds))
      put("stdMap", stdMap)
    } else {
      put("message", "没有学号")
    }
    forward()
  }

  def save(): String = {
    val stdCreditConstraint = populateEntity().asInstanceOf[StdCreditConstraint]
    var stds = Collections.newBuffer[Any]

    val persistedConstraintStds = Collections.newSet[Any]
    var stdIds: Array[Long] = null
    val stdCreditConstraints = Collections.newBuffer[Any]
    val semester = putSemester(null)
    if (true == getBoolean("allNoCredit")) {
      stds = entityDao.search(getStdBuilder(stdCreditConstraint.getSemester, null, true))
    } else {
      stdIds = Strings.splitToLong(get("stdIds"))
      if (ArrayUtils.isNotEmpty(stdIds)) {
        stds = entityDao.get(classOf[Student], stdIds)
        val builder = OqlBuilder.from(classOf[StdCreditConstraint], "stdCreditConstraint")
        builder.where("stdCreditConstraint.std in (:stds)")
          .where("stdCreditConstraint.semester=:semester")
        val stdArray = stds.toArray(Array())
        var i = 0
        while (i < stdArray.length) {
          var end = i + 500
          if (end > stdArray.length) {
            end = stdArray.length
          }
          val parameterMap = Collections.newMap[Any]
          parameterMap.put("stds", ArrayUtils.subarray(stdArray, i, end))
          parameterMap.put("semester", semester)
          stdCreditConstraints.addAll(entityDao.search(builder.params(parameterMap).build()))
          i += 500
        }
        for (constraint <- stdCreditConstraints) {
          persistedConstraintStds.add(constraint.getStd)
        }
      }
    }
    val multiStdGpa = gpaStatService.statGpas(stds, semester)
    val stdGpaMap = Collections.newMap[Any]
    for (stdGpa <- multiStdGpa.getStdGpas) {
      stdGpaMap.put(stdGpa.getStd, stdGpa)
    }
    for (std <- stds if !persistedConstraintStds.contains(std)) {
      val constraint = Model.newInstance(classOf[StdCreditConstraint])
      constraint.setSemester(semester)
      constraint.setStd(std)
      stdCreditConstraints.add(constraint)
    }
    val createdAt = new Date()
    val loggers = Collections.newBuffer[Any]
    for (constraint <- stdCreditConstraints) {
      constraint.setGPA(stdGpaMap.get(constraint.getStd).getGpa(semester))
      constraint.setMaxCredit(stdCreditConstraint.getMaxCredit)
      val logger = ConstraintLogger.genLogger(constraint, if (constraint.isPersisted) "UPDATE" else "CREATE")
      logger.setCreatedAt(createdAt)
      logger.setOperator(getUsername)
      loggers.add(logger)
    }
    try {
      entityDao.execute(Operation.saveOrUpdate(stdCreditConstraints).saveOrUpdate(loggers))
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
      val constraints = entityDao.get(classOf[StdCreditConstraint], ids)
      val createdAt = new Date()
      val loggers = Collections.newBuffer[Any]
      for (stdCreditConstraint <- constraints) {
        stdCreditConstraint.setMaxCredit(maxCredit)
        val logger = ConstraintLogger.genLogger(stdCreditConstraint, if (stdCreditConstraint.isPersisted) "UPDATE" else "CREATE")
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

  def statStdCreditsConstraint(): String = {
    val semester = putSemester(null)
    val entityIds = getLongIds(getShortName)
    var stdCreditConstraints: List[StdCreditConstraint] = null
    if (ArrayUtils.isEmpty(entityIds)) {
      stdCreditConstraints = entityDao.get(classOf[StdCreditConstraint], "semester", semester)
    } else {
      stdCreditConstraints = Collections.newBuffer[Any]
      val builder = OqlBuilder.from(classOf[StdCreditConstraint], "stdCreditConstraint")
      builder.where("stdCreditConstraint.id in (:ids)").where("stdCreditConstraint.semester=:semester")
      var i = 0
      while (i < entityIds.length) {
        var end = i + 500
        if (end > entityIds.length) {
          end = entityIds.length
        }
        val parameterMap = Collections.newMap[Any]
        parameterMap.put("ids", ArrayUtils.subarray(entityIds, i, end))
        parameterMap.put("semester", semester)
        stdCreditConstraints.addAll(entityDao.search(builder.params(parameterMap).build()))
        i += 500
      }
    }
    val stds = Collections.newSet[Any]
    val constraintMap = Collections.newMap[Any]
    for (stdCreditConstraint <- stdCreditConstraints) {
      stds.add(stdCreditConstraint.getStd)
      constraintMap.put(stdCreditConstraint.getStd, stdCreditConstraint)
    }
    if (!stds.isEmpty) {
      val multiStdGpa = gpaStatService.statGpas(stds, putSemester(null))
      val stdGpaMap = Collections.newMap[Any]
      for (stdGpa <- multiStdGpa.getStdGpas) {
        stdGpaMap.put(stdGpa.getStd, stdGpa.getGpa)
      }
      val createdAt = new Date()
      val loggers = Collections.newBuffer[Any]
      for (stdCreditConstraint <- stdCreditConstraints) {
        val std = stdCreditConstraint.getStd
        stdCreditConstraint.setGPA(stdGpaMap.get(std))
        val logger = ConstraintLogger.genLogger(stdCreditConstraint, if (stdCreditConstraint.isPersisted) "UPDATE" else "CREATE")
        logger.setCreatedAt(createdAt)
        logger.setOperator(getUsername)
        loggers.add(logger)
      }
      try {
        entityDao.execute(Operation.saveOrUpdate(stdCreditConstraints).saveOrUpdate(loggers))
        return redirect("search", "info.save.success")
      } catch {
        case e: Exception => 
      }
    }
    redirect("search", "info.save.failure")
  }

  protected def removeAndForward(entities: Iterable[_]): String = {
    try {
      val loggers = Collections.newBuffer[Any]
      val createdAt = new Date()
      for (`object` <- entities) {
        val constraint = `object`.asInstanceOf[StdCreditConstraint]
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
