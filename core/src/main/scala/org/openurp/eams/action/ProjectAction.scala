package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.{ Calendar, Campus, Department, School, TimeSetting }
import org.openurp.base.code.Education
import org.openurp.edu.base.code.{ StdLabel, StdType }
import org.openurp.edu.base.Project
import org.openurp.edu.base.model.ProjectBean

class ProjectAction extends RestfulAction[Project] {
  override def editSetting(entity: Project) = {
    val schools = findItems(classOf[School])
    put("schools", schools)

    val calendars = findItems(classOf[Calendar])
    put("calendars", calendars)

    val campuses = findItems(classOf[Campus])
    put("campuses", campuses)

    val departments = findItems(classOf[Department])
    put("departments", departments)

    val educations = findItems(classOf[Education])
    put("educations", educations)

    val labels = findItems(classOf[StdLabel])
    put("labels", labels)

    val types = findItems(classOf[StdType])
    put("types", types)

    val timeSettings = findItems(classOf[TimeSetting])
    put("timeSettings", timeSettings)

 
    
    super.editSetting(entity)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("name")
    val items = entityDao.search(query)
    items
  }
  protected override def saveAndRedirect(entity: Project): View = {
    val project = entity.asInstanceOf[ProjectBean]

    project.campuses.clear()
    val campusIds = getAll("campusesId2nd", classOf[Integer])
    project.campuses ++= entityDao.find(classOf[Campus], campusIds)

    project.departments.clear()
    val departmentIds = getAll("departmentsId2nd", classOf[Integer])
    project.departments ++= entityDao.find(classOf[Department], departmentIds)

    project.educations.clear()
    val educationIds = getAll("educationsId2nd", classOf[Integer])
    project.educations ++= entityDao.find(classOf[Education], educationIds)

    project.labels.clear()
    val labelsIds = getAll("labelsId2nd", classOf[Integer])
    project.labels ++= entityDao.find(classOf[StdLabel], labelsIds)

    project.types.clear()
    val typesIds = getAll("typesId2nd", classOf[Integer])
    project.types ++= entityDao.find(classOf[StdType], typesIds)

    project.timeSettings.clear()
    val timeSettingsIds = getAll("timeSettingsId2nd", classOf[Integer])
    project.timeSettings ++= entityDao.find(classOf[TimeSetting], timeSettingsIds)

    super.saveAndRedirect(entity)
  }

}

