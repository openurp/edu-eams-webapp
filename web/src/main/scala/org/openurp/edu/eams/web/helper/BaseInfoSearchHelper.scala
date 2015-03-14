package org.openurp.edu.eams.web.helper

import java.util.Arrays
import java.util.Collection
import java.util.List
import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.bean.transformers.PropertyTransformer
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.util.ValidEntityKeyPredicate
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.Resource
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.eams.base.Building
import org.openurp.base.Room
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Teacher

import scala.collection.JavaConversions._

class BaseInfoSearchHelper extends SearchHelper {

  def searchAdminclass(): Collection[Adminclass] = {
    entityDao.search(buildAdminclassQuery())
  }

  def searchTeacher(): Collection[Teacher] = entityDao.search(buildTeacherQuery())

  def searchClassroom(): Collection[Classroom] = entityDao.search(buildClassroomQuery())

  def buildAdminclassQuery(): OqlBuilder[Adminclass] = {
    val builder = OqlBuilder.from(classOf[Adminclass], "adminclass")
    QueryHelper.populateConditions(builder)
    val stdTypeId = Params.getLong("adminclass.stdType.id")
    val resourceName = getResourceName
    val resource = funcPermissionService.getResource(resourceName)
    if (null != resource) {
      builder.where("adminclass.stdType in (:stdTyps)", restrictionHelper.stdTypes)
      builder.where("adminclass.department in (:departments)", restrictionHelper.getDeparts)
    } else {
      if (ValidEntityKeyPredicate.Instance.apply(stdTypeId)) {
      }
    }
    val enabled = Params.getBoolean("enabled")
    if (true == enabled) {
      builder.where("adminclass.effectiveAt <= :now and (adminclass.invalidAt is null or adminclass.invalidAt >= :now)", 
        new java.util.Date())
    } else if (false == enabled) {
      builder.where("adminclass.effectiveAt > :now or adminclass.invalidAt < :now", new java.util.Date())
    }
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "adminclass.code"
      builder.orderBy(new Order("adminclass.grade", false))
      builder.orderBy(new Order("adminclass.code"))
    } else {
      builder.orderBy(orderByPras)
    }
    builder
  }

  def buildTeacherQuery(): OqlBuilder[Teacher] = {
    val builder = OqlBuilder.from(classOf[Teacher], "teacher")
    QueryHelper.populateConditions(builder)
    val resourceName = getResourceName
    val resource = funcPermissionService.getResource(resourceName)
    if (null != resource) {
    }
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "teacher.code"
    }
    builder.orderBy(orderByPras)
    builder
  }

  def buildClassroomQuery(): OqlBuilder[Classroom] = {
    val builder = OqlBuilder.from(classOf[Classroom], "classroom")
    QueryHelper.populateConditions(builder)
    val departIdSeq = Params.get("roomDepartId")
    val resourceName = getResourceName
    val resource = funcPermissionService.getResource(resourceName)
    if (Strings.isEmpty(departIdSeq) && null != resource) {
      val departs = restrictionHelper.getDeparts
      if (!departs.isEmpty) {
        builder.where("exists(from classroom.departments department where department in (:departs))", 
          departs)
      } else {
        builder.where("1=2")
      }
    } else {
      val departIds = Strings.splitToInt(departIdSeq)
      if (!org.beangle.commons.lang.Arrays.isEmpty(departIds)) {
        builder.where("exists(from classroom.departments department where department.id in (:departIds))", 
          departIds)
      } else {
        builder.where("1=2")
      }
    }
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "classroom.name"
    }
    builder.orderBy(orderByPras)
    builder
  }

  def buildBuildingQuery(): OqlBuilder[Building] = {
    val builder = OqlBuilder.from(classOf[Building], "building")
    builder.where("building.department in (:departs)", restrictionHelper.getDeparts)
    QueryHelper.populateConditions(builder)
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "building.code"
    }
    builder.orderBy(orderByPras)
    builder
  }

  def buildMajorQuery(): OqlBuilder[Major] = {
    val builder = OqlBuilder.from(classOf[Major], "major")
    QueryHelper.populateConditions(builder)
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "major.code"
    }
    builder.orderBy(orderByPras)
    builder
  }

  def buildMajorQuery(educationId: java.lang.Long): OqlBuilder[Major] = {
    val builder = OqlBuilder.from(classOf[Major], "major")
    QueryHelper.populateConditions(builder)
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "major.code"
    }
    if (null != educationId) {
      builder.where("exists (from major.educations edu where edu.id=:eduId)", educationId)
    }
    builder.orderBy(orderByPras)
    builder
  }

  def buildDirectionQuery(): OqlBuilder[Direction] = {
    val builder = OqlBuilder.from(classOf[Direction], "direction")
    QueryHelper.populateConditions(builder)
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "direction.code"
    }
    builder.orderBy(orderByPras)
    builder
  }

  def buildDirectionQuery(educationId: java.lang.Long): OqlBuilder[Direction] = {
    val builder = OqlBuilder.from(classOf[Direction], "direction")
    QueryHelper.populateConditions(builder)
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "direction.code"
    }
    if (null != educationId) {
      val hql = "exists (from direction.departs dd where dd.education.id = :educationId)"
      builder.where(hql, educationId)
    }
    builder.orderBy(orderByPras)
    builder
  }
}
