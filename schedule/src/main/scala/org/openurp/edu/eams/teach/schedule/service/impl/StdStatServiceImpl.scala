package org.openurp.edu.eams.teach.schedule.service.impl

import java.util.Date
import java.util.HashMap
import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.base.Department
import org.openurp.edu.base.Student
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.schedule.service.StdStatService
import org.openurp.edu.eams.util.DataRealmUtils
import org.openurp.edu.eams.util.stat.StatGroup
import org.openurp.edu.eams.util.stat.StatHelper

import scala.collection.JavaConversions._

class StdStatServiceImpl extends BaseServiceImpl with StdStatService {

  def statOnCampusByStdType(dataRealm: DataRealm): List[_] = {
    statOnCampusByStdTypeAndDepart(dataRealm, Array(classOf[StdType]))
  }

  def statOnCampusByDepart(dataRealm: DataRealm): List[_] = {
    statOnCampusByStdTypeAndDepart(dataRealm, Array(classOf[Department]))
  }

  def statOnCampusByStdTypeDepart(dataRealm: DataRealm): List[_] = {
    statOnCampusByStdTypeAndDepart(dataRealm, Array(classOf[StdType], classOf[Department]))
  }

  def statOnCampusByDepartStdType(dataRealm: DataRealm): List[_] = {
    statOnCampusByStdTypeAndDepart(dataRealm, Array(classOf[Department], classOf[StdType]))
  }

  def statOnCampusByStdTypeAndDepart(dataRealm: DataRealm, groupClasses: Array[Class[_]]): List[_] = {
    val entityQuery = OqlBuilder.from(classOf[Student], "std")
    if (null != dataRealm) {
      DataRealmUtils.addDataRealm(entityQuery, Array("std.type.id", "std.department.id"), dataRealm)
    }
    val selectClause = new StringBuffer("")
    val classAttrMap = new HashMap()
    classAttrMap.put(classOf[StdType], "std.type.id")
    classAttrMap.put(classOf[Department], "std.department.id")
    for (i <- 0 until groupClasses.length) {
      selectClause.append(classAttrMap.get(groupClasses(i)))
        .append(",")
      entityQuery.groupBy(classAttrMap.get(groupClasses(i)).asInstanceOf[String])
    }
    entityQuery.select(selectClause.toString + "std.grade,count(*)")
    entityQuery.join("std.journals", "stdJournal")
    val date = new java.util.Date()
    entityQuery.where("stdJournal.beginOn<=:now and stdJournal.endOn>=:now", date)
    entityQuery.where("std.registOn<=:now and std.graduateOn>=:now and std.registed is true", date)
    entityQuery.groupBy("std.grade")
    val datas = entityDao.search(entityQuery)
    new StatHelper(entityDao).replaceIdWith(datas, groupClasses)
    val statGroups = StatGroup.buildStatGroups(datas)
    statGroups
  }
}
