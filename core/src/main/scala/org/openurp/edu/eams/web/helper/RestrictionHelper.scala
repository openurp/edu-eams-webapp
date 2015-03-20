package org.openurp.edu.eams.web.helper


import javax.servlet.http.HttpSession
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.security.blueprint.Profile
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.data.service.DataPermissionService
import org.beangle.security.blueprint.function.FuncResource
import org.beangle.security.blueprint.function.service.FuncPermissionService
import org.openurp.edu.base.Project
import org.openurp.edu.eams.core.service.DepartmentService
import org.openurp.edu.eams.system.security.DataRealm
import RestrictionHelper._



object RestrictionHelper {

  var hasStdType: Int = 1

  var hasDepart: Int = 2

  var hasCollege: Int = 4

  var hasTeachDepart: Int = 10

  var hasStdTypeDepart: Int = 3

  var hasStdTypeCollege: Int = 5

  var hasStdTypeAdminDepart: Int = 9

  var hasStdTypeTeachDepart: Int = 11

  val STDTYPES_KEY = "stdTypeList"

  val DEPARTS_KEY = "departmentList"
}

trait RestrictionHelper {

  def getUser(session: HttpSession): User

  def setDataRealm(realmScope: Int): Unit

  def getProjects(): List[_]

  def getEducations(): List[_]

  def getStdTypes(): List[_]

  def getDeparts(): List[_]

  def getProject(): Project

  def getProjects(resourceName: String): List[_]

  def getProjects(resource: FuncResource): List[_]

  def getColleges(): List[_]

  def getTeachDeparts(): List[_]

  def getDataRealms(): List[_]

  def getDataRealmsWith(stdTypeId: java.lang.Long): List[DataRealm]

  def getDataRealms(funcPermissionService: FuncPermissionService): List[_]

  def getDepartmentIdSeq(): String

  def getStdTypeIdSeq(): String

  def getEducationIdSeq(): String

  def getResource(): FuncResource

  def applyRestriction(builder: OqlBuilder[_]): Unit

  def getProperties(user: User, 
      profiles: List[Profile], 
      name: String, 
      resource: FuncResource): List[_]

  def getProperties(name: String): List[_]

  def setDepartmentService(departmentService: DepartmentService): Unit

  def setEntityDao(entityDao: EntityDao): Unit

  def setFuncPermissionService(funcPermissionService: FuncPermissionService): Unit

  def setDataPermissionService(dataPermissionService: DataPermissionService): Unit

  def getFuncPermissionService(): FuncPermissionService

  def getDataPermissionService(): DataPermissionService
}
