package org.openurp.edu.eams.core.web.action

import java.util.Date


import javax.persistence.EntityExistsException
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.struts2.helper.Params
import org.openurp.base.Department
import org.openurp.edu.base.Direction
import org.openurp.edu.base.DirectionJournal
import org.openurp.edu.base.Major



class DirectionAction extends DirectionSearchAction {

  def edit(): String = {
    val direction = getEntity(classOf[Direction], "direction")
    val dds = Collections.newSet[Any]
    for (dd <- direction.getDeparts) {
      dds.add(dd.getDepart)
    }
    if (getProject.id != direction.major.getProject.id || 
      !getDeparts.containsAll(dds)) {
      return forwardError("error.dataRealm.insufficient")
    }
    builderDirectionParamsForPage(direction)
    forward()
  }

  protected def builderDirectionParamsForPage(direction: Direction) {
    val query = OqlBuilder.from(classOf[Major], "major")
    query.where("major.project = :project", getProject)
    query.where("(exists(from major.journals md where md.depart in (:departments)) or size(major.journals) = 0)", 
      getDeparts)
    query.where("major.effectiveAt <= :now and (major.invalidAt is null or major.invalidAt >= :now)", 
      if (null == direction.getEffectiveAt) new java.util.Date() else direction.getEffectiveAt)
    put("departments", getCollegeOfDeparts)
    put("majors", entityDao.search(query))
    put("direction", direction)
    put("educations", getEducations)
  }

  def save(): String = {
    val directionId = getIntId("direction")
    if (entityDao.duplicate(classOf[Direction], directionId, "code", get("direction.code"))) {
      builderDirectionParamsForPage(populateEntity(classOf[Direction], "direction"))
      addError(getText("error.code.existed"))
      return "edit"
    }
    var direction: Direction = null
    val fieldParams = Params.sub("direction")
    try {
      val now = new Date()
      if (null == directionId) {
        direction = Model.newInstance(classOf[Direction])
        populate(direction, fieldParams)
        logHelper.info("Create a direction with name:" + direction.getName)
        direction.setCreatedAt(now)
        direction.setUpdatedAt(now)
      } else {
        direction = entityDao.get(classOf[Direction], directionId)
        logHelper.info("Update a direction with name:" + direction.getName)
        populate(direction, fieldParams)
        direction.setUpdatedAt(now)
      }
      if (fillDirectionDeparts(direction)) {
        entityDao.saveOrUpdate(direction)
      } else {
        return forwardError("请正确选择院系,院系栏前三项必填")
      }
    } catch {
      case e: EntityExistsException => {
        logHelper.info("Failure save or update a direction with name:" + direction.getName, e)
        return forwardError(Array("entity.direction", "error.model.existed"))
      }
      case e: Exception => {
        logHelper.info("Failure save or update a direction with name:" + direction.getName, e)
        return forwardError("error.occurred")
      }
    }
    redirect("search", "info.save.success")
  }

  private def fillDirectionDeparts(direction: Direction): Boolean = {
    val count = getInt("mdcount")
    if (count == 0) {
      return false
    }
    direction.getDeparts.clear()
    entityDao.save(direction)
    val mdCahe = Collections.newSet[Any]
    var i = 0
    while (i <= count) {
      val md = populate(classOf[DirectionJournal], "md" + i)
      if (md.education == null || md.education.id == null || 
        md.getDepart == null || 
        md.getDepart.id == null || 
        md.getEffectiveAt == null || 
        mdCahe.contains((md.education.id + "" + md.getDepart.id).hashCode)) {
        //continue
      }
      md.setDirection(direction)
      direction.getDeparts.add(md)
      mdCahe.add((md.education.id + "" + md.getDepart.id).hashCode)
      i += 1
    }
    !direction.getDeparts.isEmpty
  }

  def remove(): String = {
    entityDao.remove(entityDao.get(classOf[Direction], getIntIds("direction")))
    redirect("search", "info.action.success")
  }
}
