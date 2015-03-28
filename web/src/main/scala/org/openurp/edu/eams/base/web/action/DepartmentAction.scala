package org.openurp.edu.eams.base.web.action




import javax.persistence.EntityExistsException
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.util.HierarchyEntityUtils
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.springframework.dao.DataIntegrityViolationException
import org.openurp.base.Department
import org.openurp.edu.eams.base.model.DepartmentBean



class DepartmentAction extends DepartmentSearchAction {

  def edit(): String = {
    builderDepartmentParamForPage(getEntity(classOf[Department], "department"))
    forward()
  }

  private def getMyFamily(depart: Department): List[Department] = {
    val departs = Collections.newBuffer[Any]
    departs.add(depart)
    findChildren(depart, departs)
    departs
  }

  private def findChildren(depart: Department, children: List[Department]) {
    if (Collections.isEmpty(depart.getChildren)) {
      return
    }
    for (one <- depart.getChildren) {
      children.add(one)
      findChildren(one, children)
    }
  }

  def save(): String = {
    val departmentId = getIntId("department")
    if (entityDao.duplicate(classOf[Department], departmentId, "code", get("department.code"))) {
      builderDepartmentParamForPage(populateEntity(classOf[Department], "department"))
      addError(getText("error.code.existed"))
      return "edit"
    }
    var department: DepartmentBean = null
    val departParams = Params.sub("department")
    try {
      if (null == departmentId) {
        department = new DepartmentBean()
        department.setSchool(getSchool)
        populate(department, departParams)
        logHelper.info("Create a depart with name:" + department.getName)
      } else {
        department = entityDao.get(classOf[DepartmentBean], departmentId)
        logHelper.info("Update a depart with name:" + department.getName)
        populate(department, departParams)
      }
      department.setSchool(getSchool)
      val errorForward = saveOrUpdate(department)
      if (null != errorForward) {
        return errorForward
      }
    } catch {
      case e: EntityExistsException => {
        logHelper.info("Failure save or update a depart with name:" + department.getName, e)
        return forwardError(Array("entity.department", "error.model.existed"))
      }
      case e: Exception => {
        logHelper.info("Failure save or update a depart with name:" + department.getName, e)
        return forwardError("error.occurred")
      }
    }
    redirect("search", "info.save.success")
  }

  protected def builderDepartmentParamForPage(department: Department) {
    if (null == department.id) {
      department.setTeaching(true)
      department.setCollege(true)
    }
    val departs = entityDao.getAll(classOf[Department])
    departs.removeAll(getMyFamily(department))
    HierarchyEntityUtils.sort(departs)
    put("parents", departs)
    put("department", department)
  }

  def remove(): String = {
    try {
      entityDao.remove(entityDao.get(classOf[Department], getIntIds("department")))
    } catch {
      case e: DataIntegrityViolationException => {
        logger.error(e.getMessage)
        addError(getText("error.remove.beenUsed"))
        put("departments", entityDao.search(buildDepartmentQuery()))
        return "search"
      }
    }
    redirect("search", "info.action.success")
  }

  protected override def getExportDatas(): Iterable[Department] = {
    val departmentIds = Strings.splitToInt(get("departmentIds"))
    if (departmentIds.length > 0) {
      val builder = OqlBuilder.from(classOf[Department], "department")
      builder.where("department.id in (:departmentIds)", departmentIds)
      entityDao.search(builder)
    } else {
      entityDao.search(buildDepartmentQuery().limit(null))
    }
  }
}
