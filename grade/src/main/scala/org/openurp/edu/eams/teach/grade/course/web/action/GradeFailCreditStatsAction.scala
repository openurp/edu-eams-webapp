package org.openurp.edu.eams.teach.grade.course.web.action

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.eams.core.service.StudentService
import org.openurp.edu.eams.teach.grade.course.model.NotPassCreditStats
import org.openurp.edu.eams.teach.grade.course.model.TotalCreditStats
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class GradeFailCreditStatsAction extends SemesterSupportAction {

  protected var studentService: StudentService = _

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    forward()
  }

  def initData(): String = forward("list")

  def search(): String = {
    val lowerCredit = getFloat("lowerCredit")
    val upperCredit = getFloat("upperCredit")
    val query = OqlBuilder.from(classOf[TotalCreditStats], "gradeInfo")
    populateConditions(query)
    if (null != lowerCredit) query.where("gradeInfo.tolCredit>=:lowerCredit", lowerCredit)
    if (null != upperCredit) query.where("gradeInfo.tolCredit<=:upperCredit", upperCredit)
    query.limit(getPageLimit).orderBy(Order.parse(get("orderBy")))
    put("gradeInfos", entityDao.search(query))
    forward()
  }

  def info(): String = {
    val stdId = getLong("stdId")
    val query = OqlBuilder.from(classOf[NotPassCreditStats], "notPassCreditStats")
    query.where("notPassCreditStats.std.id=:stdId", stdId)
    put("grades", entityDao.search(query))
    put("std", studentService.getStudent(stdId))
    forward()
  }

  def setStudentService(studentService: StudentService) {
    this.studentService = studentService
  }
}
