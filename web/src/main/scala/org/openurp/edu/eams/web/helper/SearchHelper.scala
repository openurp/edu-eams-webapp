package org.openurp.edu.eams.web.helper

import javax.servlet.http.HttpServletRequest
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.entity.metadata.Model
import org.beangle.security.blueprint.SecurityUtils
import org.beangle.security.blueprint.function.service.FuncPermissionService
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.eams.core.service.DepartmentService
import org.openurp.edu.eams.core.service.SemesterService
import SearchHelper._

import scala.collection.JavaConversions._

object SearchHelper {

  def getResourceName(): String = SecurityUtils.getResource
}

abstract class SearchHelper {

  protected var departmentService: DepartmentService = _

  protected var funcPermissionService: FuncPermissionService = _

  protected var entityDao: EntityDao = _

  protected var semesterService: SemesterService = _

  protected var restrictionHelper: RestrictionHelper = _

  def getPageLimit(request: HttpServletRequest): PageLimit = {
    val limit = new PageLimit()
    limit.setPageNo(QueryHelper.getPageNo)
    limit.setPageSize(QueryHelper.getPageSize)
    limit
  }

  def populate[T](request: HttpServletRequest, clazz: Class[T], name: String): T = {
    try {
      val t = clazz.newInstance()
      Model.populate(t, Params.sub(name))
      t
    } catch {
      case e: Exception => null
    }
  }

  def getFuncPermissionService(): FuncPermissionService = funcPermissionService

  def setFuncPermissionService(funcPermissionService: FuncPermissionService) {
    this.funcPermissionService = funcPermissionService
  }

  def getDepartmentService(): DepartmentService = departmentService

  def setDepartmentService(departmentService: DepartmentService) {
    this.departmentService = departmentService
  }

  def getEntityDao(): EntityDao = entityDao

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setRestrictionHelper(restrictionHelper: RestrictionHelper) {
    this.restrictionHelper = restrictionHelper
  }
}
