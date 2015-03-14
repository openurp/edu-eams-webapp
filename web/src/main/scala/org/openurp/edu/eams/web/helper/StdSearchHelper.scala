package org.openurp.edu.eams.web.helper

import java.util.Date
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.base.Student
import StdSearchHelper._

import scala.collection.JavaConversions._

object StdSearchHelper {

  def addMajorConditon(entityQuery: OqlBuilder, stdAttr: String) {
    val educationId = Params.getInt("std.education.id")
    val departId = Params.getInt("department.id")
    val specialityId = Params.getInt("major.id")
    val aspectId = Params.getInt("direction.id")
    if ((new java.lang.Long(4)) != educationId) {
      entityQuery.join("left", stdAttr + ".major", "major")
      entityQuery.join("left", stdAttr + ".direction", "direction")
      if (null != aspectId) {
        entityQuery.where("direction.id=" + aspectId)
      }
      if (null != specialityId) {
        entityQuery.where("major.id=" + specialityId)
      } else {
        if (null != departId) entityQuery.where(stdAttr + ".department.id=" + departId)
      }
    } else {
      entityQuery.join("left", stdAttr + ".secondAspect", "secondAspect")
      entityQuery.join("left", stdAttr + ".secondMajor", "secondMajor")
      entityQuery.where("secondMajor is not null")
      if (null != aspectId) {
        entityQuery.where("secondAspect.id=" + aspectId)
      } else {
        if (null != specialityId) {
          entityQuery.where("secondMajor.id=" + specialityId)
        } else {
          if (null != departId) entityQuery.where(stdAttr + ".secondMajor.department.id=" + departId)
        }
      }
    }
  }
}

class StdSearchHelper extends SearchHelper {

  def buildStdQuery(): OqlBuilder[_ <: Entity[Long]] = buildStdQuery(null)

  def buildStdQuery(extraStdTypeAttr: String): OqlBuilder[_ <: Entity[Long]] = {
    var query: OqlBuilder[_ <: Entity[Long]] = null
    query = OqlBuilder.from(classOf[Student], "std")
    QueryHelper.populateConditions(query)
    val stdActive = Params.getBoolean("stdActive")
    if (null != stdActive) {
      if (true == stdActive) {
        query.where("std.registOn <= :now and std.graduateOn >= :now and std.registed = true", new Date())
      } else {
        query.where("std.registOn > :now or std.graduateOn < :now or std.registed=false", new Date())
      }
    }
    restrictionHelper.applyRestriction(query)
    query.limit(QueryHelper.getPageLimit)
    query.orderBy(Order.parse(Params.get("orderBy")))
    val adminclassName = Params.get("adminclassName")
    if (Strings.isNotEmpty(Strings.trim(adminclassName))) {
      query.where(Condition.like("std.adminclass.name", adminclassName))
    }
    val stdIds = Params.get("stdIds")
    if (Strings.isNotEmpty(stdIds)) {
      val ids = Strings.splitToLong(stdIds)
      query.where(new Condition("std.id in (:stdIds)", ids))
    }
    query
  }
}
