package org.openurp.edu.eams.teach.election.web.action.constraint

import java.util.Collection
import java.util.Collections
import java.util.Date
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint
import org.openurp.edu.eams.teach.election.model.Enum.ConstraintType
import org.openurp.edu.eams.teach.election.model.constraint.ConstraintLogger
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class CourseTypeCreditConstraintAction extends SemesterSupportAction {

  protected override def getEntityName(): String = {
    classOf[CourseTypeCreditConstraint].getName
  }

  override def indexSetting() {
    putSemester(null)
    put("constraintTypes", ConstraintType.values)
    put("educations", getEducations)
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = super.getQueryBuilder.asInstanceOf[OqlBuilder[_]]
    builder.where(getShortName + ".semester=:semester", putSemester(null))
    builder
  }

  protected override def editSetting(entity: Entity[_]) {
    var semester: Semester = null
    semester = if (entity.isPersisted) entity.asInstanceOf[CourseTypeCreditConstraint].getSemester else putSemester(null)
    put("semester", semester)
    put("educations", getEducations)
    put("types", baseCodeService.getCodes(classOf[CourseType]))
  }

  def save(): String = {
    val courseTypeCreditConstraint = populateEntity().asInstanceOf[CourseTypeCreditConstraint]
    val courseTypeCreditConstraints = CollectUtils.newArrayList()
    courseTypeCreditConstraints.addAll(entityDao.get(classOf[CourseTypeCreditConstraint], Array("semester", "education", "courseType", "grades"), 
      Array(putSemester(null), courseTypeCreditConstraint.education, courseTypeCreditConstraint.getCourseType, courseTypeCreditConstraint.grades)))
    if (courseTypeCreditConstraints.isEmpty) {
      courseTypeCreditConstraints.add(courseTypeCreditConstraint)
    }
    val loggers = CollectUtils.newArrayList()
    val createdAt = new Date()
    for (courseTypeCreditConstraint2 <- courseTypeCreditConstraints) {
      courseTypeCreditConstraint2.setLimitCredit(courseTypeCreditConstraint.getLimitCredit)
      val logger = ConstraintLogger.genLogger(courseTypeCreditConstraint2, if (courseTypeCreditConstraint2.isPersisted) "UPDATE" else "CREATE")
      logger.setOperator(getUsername)
      logger.setCreatedAt(createdAt)
      loggers.add(logger)
    }
    try {
      entityDao.execute(Operation.saveOrUpdate(courseTypeCreditConstraints)
        .saveOrUpdate(loggers))
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
      }
    }
  }

  def getCourseTypes(): String = {
    val educationId = getLong("educationId")
    val builder = OqlBuilder.from(classOf[CourseTypeCreditConstraint].getName + " courseTypeCreditConstraint")
    builder.where("courseTypeCreditConstraint.semester=:semester", putSemester(null))
    if (null != educationId) {
      builder.where("courseTypeCreditConstraint.education.id=:educationId", educationId)
    }
    builder.groupBy("courseTypeCreditConstraint.courseType.id")
    builder.select("courseTypeCreditConstraint.courseType.id")
    val courseTypeIds = entityDao.search(builder)
    if (courseTypeIds.isEmpty) {
      put("types", Collections.emptyList())
    } else {
      val courseTypes = entityDao.get(classOf[CourseType], courseTypeIds)
      put("types", courseTypes)
    }
    forward()
  }

  protected def removeAndForward(entities: Collection[_]): String = {
    try {
      val loggers = CollectUtils.newArrayList()
      val createdAt = new Date()
      for (`object` <- entities) {
        val constraint = `object`.asInstanceOf[CourseTypeCreditConstraint]
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
