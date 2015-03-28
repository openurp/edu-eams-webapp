package org.openurp.edu.eams.base.web.action


import java.util.Date
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.springframework.dao.DataIntegrityViolationException
import org.openurp.base.Campus
import org.openurp.edu.eams.base.model.CampusBean



class CampusAction extends BaseInfoAction {

  def index(): String = forward()

  def search(): String = {
    val query = buildOqlBuilder()
    query.limit(getPageLimit)
    put("campuses", entityDao.search(query))
    forward()
  }

  protected def buildOqlBuilder(): OqlBuilder[Campus] = {
    val query = OqlBuilder.from(classOf[Campus], "campus")
    populateConditions(query)
    var orderBy = Params.get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "campus.name"
    }
    query.orderBy(Order.parse(orderBy))
    query
  }

  def info(): String = {
    put("campus", entityDao.get(classOf[Campus], getIntId("campus")))
    forward()
  }

  def edit(): String = {
    put("campus", getEntity(classOf[Campus], "campus"))
    forward()
  }

  def save(): String = {
    val campus = populateEntity(classOf[CampusBean], "campus")
    val query = OqlBuilder.from(classOf[Campus], "campus")
    query.where("campus.code = :code", campus.getCode)
    if (null != campus.id) {
      query.where("campus != :campus", campus)
    }
    if (Collections.isNotEmpty(entityDao.search(query))) {
      put("campus", campus)
      addError(getText("error.code.existed"))
      return "edit"
    }
    if (null == campus.id) {
      campus.setCreatedAt(new Date())
      campus.setUpdatedAt(campus.getCreatedAt)
    } else {
      campus.setUpdatedAt(new Date())
    }
    if (null == campus.getSchool) campus.setSchool(getSchool)
    entityDao.saveOrUpdate(campus)
    logHelper.info((if (null == campus.id) "Create" else "Update") + " a campus with name: " + 
      campus.getName)
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    try {
      entityDao.remove(entityDao.get(classOf[Campus], getIntIds("campus")))
    } catch {
      case e: DataIntegrityViolationException => {
        logger.error(e.getMessage)
        addError(getText("error.remove.beenUsed"))
        search()
        return "search"
      }
    }
    redirect("search", "info.action.success")
  }

  def checkDuplicated(): String = {
    put("duplicated", Collections.isNotEmpty(entityDao.get(classOf[Campus], "code", get("code"))))
    forward()
  }

  protected def getExportDatas(): Iterable[Campus] = entityDao.search(buildOqlBuilder())

  def getEntityName(): String = classOf[Campus].getName
}
