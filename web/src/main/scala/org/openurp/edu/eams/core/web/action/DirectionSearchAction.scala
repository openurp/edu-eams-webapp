package org.openurp.edu.eams.core.web.action



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Project
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class DirectionSearchAction extends ProjectSupportAction {

  def getEntityName(): String = classOf[Direction].getName

  def index(): String = {
    put("project", getProject)
    forward()
  }

  def search(): String = {
    put("directions", entityDao.search(buildDirectionQuery()))
    forward()
  }

  protected def buildDirectionQuery(): OqlBuilder[Direction] = {
    val query = OqlBuilder.from(classOf[Direction], "direction")
    populateConditions(query)
    val projects = getProjects
    if (CollectUtils.isNotEmpty(projects)) {
      query.where("direction.major.project in (:projects)", projects)
    }
    val departments = getDeparts
    if (CollectUtils.isNotEmpty(departments)) {
      query.where("(exists(from direction.departs dd where dd.depart in (:departs)) or size(direction.departs)=0)", 
        departments)
    }
    var orderBy = get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "direction.code"
    }
    val departId = getInt("fake.department.id")
    if (departId != null) {
      query.where("exists(from direction.departs dd where dd.depart.id=:departId)", departId)
    }
    val educationId = getInt("fake.education.id")
    if (educationId != null) {
      query.where("exists(from direction.departs dd where dd.education.id = :educationId)", educationId)
    }
    query.limit(getPageLimit)
    query.orderBy(Order.parse(orderBy))
    query
  }

  protected def getExportDatas(): Iterable[Direction] = {
    val query = buildDirectionQuery()
    query.limit(null)
    entityDao.search(query)
  }

  def info(): String = {
    val directionId = getIntId("direction")
    if (null == directionId) {
      return forwardError(Array("entity.direction", "error.model.id.needed"))
    }
    put("direction", entityDao.get(classOf[Direction], directionId))
    forward()
  }
}
