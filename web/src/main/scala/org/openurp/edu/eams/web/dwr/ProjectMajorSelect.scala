package org.openurp.edu.eams.web.dwr

import java.util.Collections
import java.util.Date
import java.util.List
import javax.servlet.http.HttpServletRequest
import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.Profile
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.function.FuncResource
import org.beangle.security.blueprint.model.UserProfileBean
import org.beangle.security.blueprint.service.UserToken
import org.beangle.security.core.context.SecurityContext
import org.beangle.security.web.context.HttpSessionContextFilter
import org.openurp.base.Department
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.web.helper.RestrictionHelper

import scala.collection.JavaConversions._

class ProjectMajorSelect {

  var entityDao: EntityDao = _

  var restrictionHelper: RestrictionHelper = _

  def projects(): String = null

  def educationAndDeparts(request: HttpServletRequest, projectId: java.lang.Integer, resourceName: String): Array[List[_]] = {
    val project = entityDao.get(classOf[Project], projectId)
    val user = getUser(request)
    var resource: FuncResource = null
    val profiles = entityDao.get(classOf[UserProfileBean], "id", request.getSession.getAttribute("security.profileId").asInstanceOf[java.lang.Long]).asInstanceOf[List[_]]
    var fineResourceName = resourceName
    if (Strings.isNotEmpty(resourceName)) {
      if (fineResourceName.contains(".")) fineResourceName = Strings.substringBeforeLast(resourceName, 
        ".")
      if (fineResourceName.contains("!")) fineResourceName = Strings.substringBeforeLast(resourceName, 
        "!")
    }
    if (Strings.isNotEmpty(fineResourceName)) {
      resource = restrictionHelper.getFuncPermissionService.getResource(fineResourceName)
    }
    var departs = CollectUtils.newArrayList()
    if (null != resource) {
      val departs2 = restrictionHelper.getProperties(user, profiles, "departs", resource).asInstanceOf[List[Department]]
      for (d <- project.departments if departs2.contains(d)) departs.add(d)
    } else {
      departs = project.departments
    }
    val departInfos = CollectUtils.newArrayList()
    for (depart <- departs) {
      departInfos.add(Array(depart.getId, depart.getName))
    }
    val educations1 = project.educations
    var educations2: List[Education] = null
    educations2 = if (null != resource) restrictionHelper.getProperties(user, profiles, "educations", 
      resource).asInstanceOf[List[Education]] else educations1
    val educations = CollectionUtils.intersection(educations1, educations2).asInstanceOf[List[Education]]
    Collections.sort(educations, new PropertyComparator("code"))
    val educationInfos = CollectUtils.newArrayList()
    for (education <- educations) {
      educationInfos.add(Array(education.getId, education.getName))
    }
    val stdTypes1 = project.getTypes
    var stdTypes2: List[StdType] = null
    stdTypes2 = if (null != resource) restrictionHelper.getProperties(user, profiles, "stdTypes", resource).asInstanceOf[List[StdType]] else stdTypes1
    val stdTypes = CollectionUtils.intersection(stdTypes1, stdTypes2).asInstanceOf[List[StdType]]
    Collections.sort(stdTypes, new PropertyComparator("code"))
    val stdTypeInfos = CollectUtils.newArrayList()
    for (stdType <- stdTypes) {
      stdTypeInfos.add(Array(stdType.getId, stdType.getName))
    }
    Array(educationInfos, departInfos, stdTypeInfos)
  }

  private def getUser(request: HttpServletRequest): User = {
    val scontext = request.getSession.getAttribute(HttpSessionContextFilter.SECURITY_CONTEXT_KEY).asInstanceOf[SecurityContext]
    if (scontext == null) {
    }
    val userToken = scontext.getAuthentication.getPrincipal.asInstanceOf[UserToken]
    val user = entityDao.get(classOf[User], userToken.getId)
    user
  }

  def majors(projectId: java.lang.Integer, educationId: java.lang.Integer, departId: java.lang.Integer): List[_] = {
    if (null == departId || projectId == null) {
      return Collections.emptyList()
    }
    val now = new Date()
    val query = OqlBuilder.from(classOf[Major], "s")
    query.select("s.id, s.name, s.engName").where("s.effectiveAt<=:now", now)
      .where("(s.invalidAt is null or s.invalidAt >= :now)", now)
      .where("exists(from s.journals md where md.major=s and md.depart.id = :departId)", departId)
      .where("s.project.id = :projectId", projectId)
    if (null != educationId) {
      query.where("exists(from s.educations edu where edu.id = :educationId)", educationId)
    }
    query.orderBy("s.name")
    entityDao.search(query)
  }

  def directions(majorId: java.lang.Integer): List[Array[Any]] = {
    if (null == majorId) {
      return Collections.emptyList()
    }
    val now = new Date()
    val query = OqlBuilder.from(classOf[Direction], "s")
    query.select("s.id, s.name, s.engName").where("s.effectiveAt<=:now", now)
      .where("(s.invalidAt is null or s.invalidAt >= :now)", now)
      .where("s.major.id = :majorId", majorId)
    query.orderBy("s.name")
    entityDao.search(query)
  }

  def adminClasses(grade: String, directionId: java.lang.Integer, majorId: java.lang.Integer): List[Array[Any]] = {
    if (null == majorId) {
      return Collections.emptyList()
    }
    var hql = "select s.id, s.name " + "from org.openurp.edu.base.Adminclass as s" + 
      " where s.effectiveAt<=:now and (s.invalidAt is null or s.invalidAt>=:now) and s.major.id=:majorId"
    if (null != directionId) {
      hql += " and s.direction.id =:directionId"
    }
    if (null != grade) {
      hql += " and s.grade = :grade"
    }
    hql += " order by s.name"
    val builder = OqlBuilder.from(hql)
    builder.param("majorId", majorId)
    builder.param("now", new Date())
    if (null != directionId) {
      builder.param("directionId", directionId)
    }
    if (null != grade) {
      builder.param("grade", grade)
    }
    entityDao.search(builder)
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setRestrictionHelper(restrictionHelper: RestrictionHelper) {
    this.restrictionHelper = restrictionHelper
  }
}
