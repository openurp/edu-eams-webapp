package org.openurp.edu.eams.teach.schedule.web.action

import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.schedule.model.CourseTableCheck
import org.openurp.edu.eams.teach.schedule.service.CourseTableCheckService
import org.openurp.edu.eams.teach.schedule.service.StdStatService
import org.openurp.edu.eams.util.DataRealmUtils
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class CourseTableCheckAction extends SemesterSupportAction {

  protected var courseTableCheckService: CourseTableCheckService = _

  protected var stdStatService: StdStatService = _

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    forward()
  }

  def search(): String = {
    val query = OqlBuilder.from(classOf[CourseTableCheck], "check")
      .where("check.std.department in (:departments)", getDeparts)
      .where("check.std.type in (:types)", getStdTypes)
      .limit(getPageLimit)
      .orderBy(get(Order.ORDER_STR))
    populateConditions(query)
    put("courseTableChecks", entityDao.search(query))
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(classOf[CourseTableCheck], "check")
    populateConditions(builder)
    val depart = "check.std.department.id"
    builder.where(new Condition("check.semester.id=(:semesterId)", getInt("semester.id")))
    DataRealmUtils.addDataRealms(builder, Array("check.std.stdType.id", depart), getDataRealmsWith(getLong("check.std.stdType.id")))
    builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    builder
  }

  def stat(): String = {
    val semester = semesterService.getSemester(getInt("semester.id"))
    put("stats", courseTableCheckService.statCheckByDepart(semester, getDataRealm, populateEntity(classOf[Project], 
      "project")))
    val realm = getDataRealm
    realm.setStudentTypeIdSeq(getStdTypeIdSeq)
    put("onCampusStats", stdStatService.statOnCampusByStdTypeDepart(realm))
    forward()
  }

  def setCourseTableCheckService(courseTableCheckService: CourseTableCheckService) {
    this.courseTableCheckService = courseTableCheckService
  }

  def setStdStatService(stdStatService: StdStatService) {
    this.stdStatService = stdStatService
  }
}
