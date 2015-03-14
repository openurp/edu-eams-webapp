package org.openurp.edu.eams.teach.schedule.service.impl

import java.util.Collection
import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.base.Department
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.schedule.model.CourseTableCheck
import org.openurp.edu.eams.teach.schedule.service.CourseTableCheckService
import org.openurp.edu.eams.util.DataRealmUtils
import org.openurp.edu.eams.util.stat.StatGroup
import org.openurp.edu.eams.util.stat.StatHelper

import scala.collection.JavaConversions._

class CourseTableCheckServiceImpl extends BaseServiceImpl with CourseTableCheckService {

  def statCheckBy(semester: Semester, 
      dataRealm: DataRealm, 
      attr: String, 
      clazz: Class[_]): List[_] = {
    statCheckBy(semester, dataRealm, Array("check.std.stdType.id", "check.std.department.id"), attr, 
      clazz)
  }

  private def statCheckBy(semester: Semester, 
      dataRealm: DataRealm, 
      dataRealmAttrs: Array[String], 
      attr: String, 
      clazz: Class[_]): List[_] = {
    val entityQuery = OqlBuilder.from(classOf[CourseTableCheck], "check")
    entityQuery.select("select new  org.openurp.edu.eams.util.stat.StatItem(" + 
      attr + 
      ",count(check.isConfirm),sum(CASE WHEN check.isConfirm=true THEN 1 ELSE 0 END ),sum(CASE WHEN check.isConfirm=true THEN 0 ELSE 1 END))")
    entityQuery.where("check.semester=:semester", semester)
    DataRealmUtils.addDataRealm(entityQuery, dataRealmAttrs, dataRealm)
    entityQuery.groupBy(attr)
    val stats = entityDao.search(entityQuery)
    new StatHelper(entityDao).setStatEntities(stats, clazz)
  }

  def statCheckByDepart(semester: Semester, dataRealm: DataRealm, project: Project): List[_] = {
    val query = OqlBuilder.from(classOf[CourseTableCheck], "check")
    query.select("check.std.type.id,check.std.department.id,check.std.grade,count(check.confirm),sum(CASE WHEN check.confirm=true THEN 1 ELSE 0 END )")
    query.where("check.semester=:semester", semester)
    DataRealmUtils.addDataRealm(query, Array("check.std.type.id", "check.std.department.id"), dataRealm)
    query.groupBy("check.std.type.id").groupBy("check.std.department.id")
      .groupBy("check.std.grade")
    val datas = entityDao.search(query).asInstanceOf[List[_]]
    new StatHelper(entityDao).replaceIdWith(datas, Array(classOf[StdType], classOf[Department]))
    val statGroups = StatGroup.buildStatGroups(datas, 2)
    statGroups
  }
}
