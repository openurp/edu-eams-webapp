package org.openurp.edu.eams.teach.grade.lesson.web.action

import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class InputSwitchAction extends SemesterSupportAction {

  protected override def getEntityName(): String = classOf[GradeInputSwitch].getName

  protected override def editSetting(entity: Entity[_]) {
    val is = entity.asInstanceOf[GradeInputSwitch]
    val semester = putSemester(null)
    if (null == is.getSemester) is.setSemester(semester)
    val types = baseCodeService.getCodes(classOf[GradeType])
    types.remove(entityDao.get(classOf[GradeType], GradeTypeConstants.FINAL_ID))
    put("gradeTypes", types)
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(getEntityName, getShortName)
    populateConditions(builder)
    builder.where(getShortName + ".project = :project", getProject)
    builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    builder
  }

  def save(): String = {
    val gradeInputSwitch = populateEntity(classOf[GradeInputSwitch], "gradeInputSwitch")
    gradeInputSwitch.setProject(getProject)
    val query = OqlBuilder.from(classOf[GradeInputSwitch], "gradeInputSwitch")
    query.where("gradeInputSwitch.project.id = :projectId", gradeInputSwitch.getProject.getId)
    query.where("gradeInputSwitch.semester.id = :semesterId", gradeInputSwitch.getSemester.getId)
    if (gradeInputSwitch.isPersisted) {
      query.where("gradeInputSwitch.id != :switchId", gradeInputSwitch.getId)
    }
    if (CollectUtils.isNotEmpty(entityDao.search(query))) {
      redirect("search", "info.save.failure.overlapAcross")
    } else {
      gradeInputSwitch.getTypes.clear()
      gradeInputSwitch.getTypes.addAll(entityDao.get(classOf[GradeType], Strings.splitToInt(get("gradeTypeIds"))))
      try {
        entityDao.saveOrUpdate(gradeInputSwitch)
        redirect("search", "info.save.success")
      } catch {
        case e: Exception => redirect("search", "info.save.failure")
      }
    }
  }
}
