package org.openurp.edu.eams.core.web.action

import java.sql.BatchUpdateException


ExistsException
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.openurp.edu.eams.base.Calendar
import org.openurp.base.Campus
import org.openurp.base.Department
import org.openurp.edu.eams.base.School
import org.openurp.base.TimeSetting
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.web.action.BaseAction



class ProjectAction extends BaseAction {

  def index(): String = {
    put("departments", entityDao.getAll(classOf[Department]))
    put("educations", baseCodeService.getCodes(classOf[Education]))
    put("studentLabels", baseCodeService.getCodes(classOf[StdLabel]))
    put("studentTypes", baseCodeService.getCodes(classOf[StdType]))
    put("campuses", baseInfoService.getBaseInfos(classOf[Campus]))
    forward()
  }

  def search(): String = {
    val query = buildOqlBuilder()
    query.limit(getPageLimit)
    put("projects", entityDao.search(query))
    forward()
  }

  protected def buildOqlBuilder(): OqlBuilder[Project] = {
    val query = OqlBuilder.from(classOf[Project], "project")
    populateConditions(query)
    if (Strings.isNotBlank(get("project.campuses.id"))) {
      query.join("left", "project.campuses", "campus")
      query.where("campus.id =:campusId", getInt("project.campuses.id"))
    }
    if (Strings.isNotBlank(get("project.departments.id"))) {
      query.join("left", "project.departments", "depart")
      query.where("depart.id =:departId", getInt("project.departments.id"))
    }
    if (Strings.isNotBlank(get("project.educations.id"))) {
      query.join("left", "project.educations", "education")
      query.where("education.id =:educationId", getInt("project.educations.id"))
    }
    if (Strings.isNotBlank(get("project.types.id"))) {
      query.join("left", "project.types", "type")
      query.where("type.id =:typeId", getInt("project.types.id"))
    }
    var orderBy = Params.get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "project.name"
    }
    query.orderBy(Order.parse(orderBy))
    query
  }

  def edit(): String = {
    val project = getEntity(classOf[Project], "project")
    put("project", project)
    val builderDepart = OqlBuilder.from(classOf[Department], "department")
    if (project.departments.size > 0) {
      builderDepart.where("department not in (:projDepts)", project.departments)
    }
    val builderEduc = OqlBuilder.from(classOf[Education], "education")
    if (project.educations.size > 0) {
      builderEduc.where("education not in (:projEducs)", project.educations)
    }
    val builderLabel = OqlBuilder.from(classOf[StdLabel], "label")
    if (project.getLabels.size > 0) {
      builderLabel.where("label not in (:projLabels)", project.getLabels)
    }
    val builderStdType = OqlBuilder.from(classOf[StdType], "stdType")
    if (project.getTypes.size > 0) {
      builderStdType.where("stdType not in (:projStdTypes)", project.getTypes)
    }
    val builderCampuse = OqlBuilder.from(classOf[Campus], "campuse")
    if (project.getCampuses.size > 0) {
      builderCampuse.where("campuse not in (:projCampuse)", project.getCampuses)
    }
    put("departments", entityDao.search(builderDepart))
    put("educations", entityDao.search(builderEduc))
    put("studentLabels", entityDao.search(builderLabel))
    put("studentTypes", entityDao.search(builderStdType))
    put("campuses", entityDao.search(builderCampuse))
    put("schools", entityDao.getAll(classOf[School]))
    put("calendars", entityDao.getAll(classOf[Calendar]))
    put("timeSettings", entityDao.getAll(classOf[TimeSetting]))
    if (!project.getTimeSettings.isEmpty) {
      put("timeSetting", project.getTimeSettings.get(0))
    }
    forward()
  }

  def save(): String = {
    val projectId = getIntId("project")
    if (entityDao.duplicate(classOf[Project], projectId, "name", get("project.name"))) {
      addError(getText("error.code.existed"))
      return "edit"
    }
    var project: Project = null
    val fieldParams = Params.sub("project", "project.timeSetting.id")
    try {
      var log: String = null
      if (null == projectId) {
        project = Model.newInstance(classOf[Project])
        log = "Create"
      } else {
        project = entityDao.get(classOf[Project], projectId)
        log = "Update"
      }
      populate(project, fieldParams)
      val educations = entityDao.get(classOf[Education], Strings.splitToInt(get("project.educations.id")))
      val departments = entityDao.get(classOf[Department], Strings.splitToInt(get("project.departments.id")))
      val campuses = entityDao.get(classOf[Campus], Strings.splitToInt(get("project.campuses.id")))
      val stdLabels = entityDao.get(classOf[StdLabel], Strings.splitToInt(get("project.labels.id")))
      val stdTypes = entityDao.get(classOf[StdType], Strings.splitToInt(get("project.types.id")))
      project.getTimeSettings.clear()
      val timeSettingId = getInt("project.timeSetting.id")
      if (null != timeSettingId) {
        val timeSetting = entityDao.get(classOf[TimeSetting], timeSettingId)
        project.getTimeSettings.add(timeSetting)
      }
      project.getCampuses.clear()
      project.getCampuses.addAll(campuses)
      project.departments.clear()
      project.departments.addAll(departments)
      project.educations.clear()
      project.educations.addAll(educations)
      project.getLabels.clear()
      project.getLabels.addAll(stdLabels)
      project.getTypes.clear()
      project.getTypes.addAll(stdTypes)
      project.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()))
      if (!project.isPersisted) {
        project.setCreatedAt(new java.sql.Date(System.currentTimeMillis()))
      }
      log += " a building with name:" + project.getName
      logHelper.info(log)
      saveOrUpdate(project)
    } catch {
      case e: EntityExistsException => {
        logHelper.info("Failure save or update a building with name " + project.getName, e)
        return forwardError(Array("entity.building", "error.model.existed"))
      }
      case e: Exception => {
        logHelper.info("Failure save or update a building with name " + project.getName, e)
        return forwardError("error.occurred")
      }
    }
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    try {
      entityDao.remove(entityDao.get(classOf[Project], getIntIds("project")))
    } catch {
      case e: DataIntegrityViolationException => if (e.getCause.isInstanceOf[ConstraintViolationException]) {
        if (e.getCause.getCause.isInstanceOf[BatchUpdateException]) {
          val e1 = e.getCause.getCause.asInstanceOf[BatchUpdateException]
          if (Objects.==(e1.getSQLState, "23000") && e1.getMessage.indexOf("ORA-02292") >= 0) {
            return redirect("search", "error.remove.beenUsed")
          }
        }
      }
    }
    redirect("search", "info.action.success")
  }

  def info(): String = {
    val projectId = getIntId("project")
    if (projectId == null || projectId == 0) {
      return forwardError(Array("entity.project", "error.model.id.needed"))
    }
    val project = entityDao.get(classOf[Project], projectId)
    put("project", project)
    forward()
  }
}
