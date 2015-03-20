package org.openurp.edu.eams.web.helper

import java.util.Date


import javax.servlet.http.HttpSession
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Predicate
import org.apache.struts2.ServletActionContext
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.ems.web.helper.SecurityHelper
import org.beangle.security.blueprint.Field
import org.beangle.security.blueprint.Profile
import org.beangle.security.blueprint.SecurityUtils
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.data.service.DataPermissionService
import org.beangle.security.blueprint.function.FuncResource
import org.beangle.security.blueprint.function.service.FuncPermissionService
import org.beangle.security.blueprint.model.UserProfileBean
import org.beangle.security.blueprint.service.ProfileService
import org.beangle.security.core.AuthenticationException
import org.beangle.struts2.helper.ContextHelper
import org.openurp.base.Department
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.core.service.DepartmentService
import org.openurp.edu.eams.exception.EamsException
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.system.security.DepartAuthorityException
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.system.security.StdTypeAuthorityException
import com.opensymphony.xwork2.ActionContext
import RestrictionHelperImpl._



object RestrictionHelperImpl {

  private def ids(clazz: Class[_], entityDao: EntityDao): String = {
    val date = new java.util.Date()
    val query = OqlBuilder.from(clazz, "entity")
    query.where("entity.effectiveAt <= :now", date)
    query.where("(entity.invalidAt is null or entity.invalidAt >= :now)", date)
    query.select("id")
    val departIds = entityDao.search(query)
    val sb = new StringBuffer()
    var iter = departIds.iterator()
    while (iter.hasNext) {
      val id = iter.next().asInstanceOf[java.lang.Integer]
      sb.append(id).append(",")
    }
    if (sb.length != 0) {
      sb.deleteCharAt(sb.length - 1)
    }
    sb.toString
  }

  protected def getUserId(): java.lang.Long = {
    val userId = SecurityUtils.getUserId
    if (null == userId) throw new AuthenticationException() else userId
  }

  protected def getResourceName(): String = SecurityUtils.getResource
}

class RestrictionHelperImpl extends RestrictionHelper {

  protected var departmentService: DepartmentService = _

  protected var entityDao: EntityDao = _

  protected var funcPermissionService: FuncPermissionService = _

  protected var dataPermissionService: DataPermissionService = _

  protected var profileService: ProfileService = _

  protected var securityHelper: SecurityHelper = _

  def getUser(session: HttpSession): User = {
    entityDao.get(classOf[User], SecurityUtils.getUserId)
  }

  def setDataRealm(realmScope: Int) {
    var stdTypeList: List[_] = null
    if (realmScope % 2 == 1) {
      stdTypeList = getStdTypes
      ContextHelper.put(STDTYPES_KEY, stdTypeList)
      realmScope -= 1
    }
    if (realmScope == hasTeachDepart) {
      ContextHelper.put(DEPARTS_KEY, getTeachDeparts)
    }
    if (realmScope == hasCollege) {
      ContextHelper.put(DEPARTS_KEY, getColleges)
    }
    if (realmScope == hasDepart) {
      ContextHelper.put(DEPARTS_KEY, getDeparts)
    }
  }

  def getProjects(): List[_] = getProperties("projects")

  def getEducations(): List[_] = {
    val educations = getProperties("educations")
    Collections.sort(educations, new PropertyComparator("code"))
    CollectionUtils.filter(educations, new Predicate() {

      def evaluate(`object`: AnyRef): Boolean = {
        if (`object`.asInstanceOf[Education].getInvalidAt == null) {
          return true
        }
        return `object`.asInstanceOf[Education].getInvalidAt.after(new Date())
      }
    })
    educations
  }

  def getStdTypes(): List[_] = getProperties("stdTypes")

  def getDeparts(): List[_] = getProperties("departs")

  @Deprecated
  def getProject(): Project = {
    val projectId = ServletActionContext.getRequest.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]
    if (null == projectId) {
      throw new RuntimeException("project not selected")
    }
    entityDao.get(classOf[Project], projectId)
  }

  def getProjects(resourceName: String): List[_] = {
    val resource = funcPermissionService.getResource(resourceName)
    getProperties(getUser, securityHelper.getProfiles, "projects", resource)
  }

  def getProjects(resource: FuncResource): List[_] = {
    getProperties(getUser, securityHelper.getProfiles, "projects", resource)
  }

  def getColleges(): List[_] = {
    val departs = getProperties("departs")
    CollectionUtils.filter(departs, new Predicate() {

      def evaluate(obj: AnyRef): Boolean = {
        val depart = obj.asInstanceOf[Department]
        return depart.isCollege
      }
    })
    Collections.sort(departs, new PropertyComparator("code"))
    departs
  }

  def getTeachDeparts(): List[_] = {
    val departs = getProperties("departs")
    CollectionUtils.filter(departs, new Predicate() {

      def evaluate(obj: AnyRef): Boolean = {
        val depart = obj.asInstanceOf[Department]
        return depart.isTeaching
      }
    })
    Collections.sort(departs, new PropertyComparator("code"))
    departs
  }

  def getDataRealms(): List[_] = getDataRealms(funcPermissionService)

  def getDataRealmsWith(stdTypeId: java.lang.Long): List[DataRealm] = {
    val dataRealms = getDataRealms.asInstanceOf[List[DataRealm]]
    if (null == stdTypeId) {
      dataRealms
    } else {
      val stdTypeIds = String.valueOf(stdTypeId)
      val newRealms = CollectUtils.newArrayList()
      var iterator = dataRealms.iterator()
      while (iterator.hasNext) {
        val newRealm = (iterator.next().clone()).asInstanceOf[DataRealm]
        newRealm.setStudentTypeIdSeq(Strings.intersectSeq(stdTypeIds, newRealm.getStudentTypeIdSeq))
        if (Strings.isNotEmpty(newRealm.getStudentTypeIdSeq)) {
          newRealms.add(newRealm)
        }
      }
      if (newRealms.isEmpty) {
        throw new StdTypeAuthorityException(getUser, getResourceName)
      }
      newRealms
    }
  }

  def getDataRealms(funcPermissionService: FuncPermissionService): List[_] = {
    val profiles = securityHelper.getProfiles
    if (profiles.isEmpty) {
      if (SecurityUtils.getUserId != 1l) {
        throw new DepartAuthorityException(getUser, getResourceName)
      }
    }
    val realms = CollectUtils.newArrayList()
    val realm = new DataRealm()
    val profile = profiles.get(0)
    if (SecurityUtils.getUserId == 1l) {
      realm.setStudentTypeIdSeq("*")
      realm.setDepartmentIdSeq("*")
    } else {
      if (null != profile.getProperty("stdTypes")) {
        realm.setStudentTypeIdSeq(profile.getProperty("stdTypes").getValue)
      }
      if (null != profile.getProperty("departs")) {
        realm.setDepartmentIdSeq(profile.getProperty("departs").getValue)
      }
    }
    realms.add(realm)
    replaceStar(realms)
    realms
  }

  private def replaceStar(realms: List[DataRealm]) {
    var starOccured = false
    var iter = realms.iterator()
    while (iter.hasNext) {
      val realm = iter.next()
      if (Objects.==(realm.departmentIdSeq, "*")) {
        val departIdsTemp = new StringBuffer()
        starOccured = true
        val query = OqlBuilder.from(classOf[Department], "depart")
        val col = this.entityDao.search(query)
        departIdsTemp.append(',')
        var temp: Department = null
        var iter2 = col.iterator()
        while (iter2.hasNext) {
          temp = iter2.next()
          departIdsTemp.append(temp.id.longValue()).append(",")
        }
        realm.setDepartmentIdSeq(departIdsTemp.toString)
      }
      if (Objects.==(realm.getStudentTypeIdSeq, "*")) {
        starOccured = true
        val stdTypeIdsTemp = new StringBuffer()
        val query = OqlBuilder.from(classOf[StdType], "stdType")
        val col = this.entityDao.search(query)
        stdTypeIdsTemp.append(',')
        var temp: StdType = null
        var iter2 = col.iterator()
        while (iter2.hasNext) {
          temp = iter2.next()
          stdTypeIdsTemp.append(temp.id.longValue()).append(",")
        }
        realm.setStudentTypeIdSeq(stdTypeIdsTemp.toString)
      }
    }
  }

  def getDepartmentIdSeq(): String = {
    if (null == SecurityUtils.getUserId || SecurityUtils.getUserId == 1l) {
      return ids(classOf[Department], entityDao)
    }
    val profiles = securityHelper.getProfiles
    if (profiles.isEmpty) {
      throw new DepartAuthorityException(getUser, getResourceName)
    }
    profiles.get(0).getProperty("departs").getValue
  }

  def getStdTypeIdSeq(): String = {
    if (null == SecurityUtils.getUserId || SecurityUtils.getUserId == 1l) {
      return ids(classOf[StdType], entityDao)
    }
    val profiles = securityHelper.getProfiles
    if (profiles.isEmpty) {
      throw new DepartAuthorityException(getUser, getResourceName)
    }
    profiles.get(0).getProperty("stdTypes").getValue
  }

  def getEducationIdSeq(): String = {
    if (null == SecurityUtils.getUserId || SecurityUtils.getUserId == 1l) {
      return ids(classOf[Education], entityDao)
    }
    val profiles = securityHelper.getProfiles
    if (profiles.isEmpty) {
      throw new DepartAuthorityException(getUser, getResourceName)
    }
    profiles.get(0).getProperty("educations").getValue
  }

  def getResource(): FuncResource = {
    funcPermissionService.getResource(getResourceName)
  }

  def applyRestriction(builder: OqlBuilder[_]) {
    securityHelper.applyPermission(builder)
  }

  def getProperties(user: User, 
      profiles: List[Profile], 
      name: String, 
      resource: FuncResource): List[_] = {
    if (name == "projects") {
      profiles = user.getProfiles
    }
    val field = profileService.getField(name)
    if (null == field) {
      throw new RuntimeException("cannot find " + name + " in profile fields!")
    }
    if (CollectUtils.isEmpty(profiles)) {
      val currentRoleId = ActionContext.getContext.getSession.get("security.userCategoryId").asInstanceOf[java.lang.Integer]
      if (user.id == 1L) {
        if ("directions" == name || "majors" == name) {
          CollectUtils.newArrayList()
        } else {
          profileService.getFieldValues(field)
        }
      } else {
        CollectUtils.newArrayList()
      }
    } else {
      val res = CollectUtils.newArrayList()
      for (profile <- profiles) {
        if (null != profile.getProperty(name)) {
          val value = profile.getProperty(name).getValue
          if (value == "*") {
            if ("directions" == name || "majors" == name) {
              return CollectUtils.newArrayList()
            } else {
              return profileService.getFieldValues(field)
            }
          } else {
            res.addAll(profileService.getFieldValues(field, Strings.split(value)))
          }
        } else {
          return profileService.getFieldValues(field)
        }
      }
      res
    }
  }

  def getProperties(name: String): List[_] = {
    getProperties(getUser, securityHelper.getProfiles, name, null)
  }

  protected def getUser(): User = {
    entityDao.get(classOf[User], getUserId).asInstanceOf[User]
  }

  def setDepartmentService(departmentService: DepartmentService) {
    this.departmentService = departmentService
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setFuncPermissionService(funcPermissionService: FuncPermissionService) {
    this.funcPermissionService = funcPermissionService
  }

  def setDataPermissionService(dataPermissionService: DataPermissionService) {
    this.dataPermissionService = dataPermissionService
  }

  def getFuncPermissionService(): FuncPermissionService = funcPermissionService

  def getDataPermissionService(): DataPermissionService = dataPermissionService

  def getProfileService(): ProfileService = profileService

  def setProfileService(profileService: ProfileService) {
    this.profileService = profileService
  }

  def setSecurityHelper(securityHelper: SecurityHelper) {
    this.securityHelper = securityHelper
  }
}
