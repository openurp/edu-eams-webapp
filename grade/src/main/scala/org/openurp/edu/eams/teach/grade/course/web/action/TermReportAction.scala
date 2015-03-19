package org.openurp.edu.eams.teach.grade.course.web.action

import java.util.Date


import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.model.NotPassCreditStats
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.eams.teach.grade.service.GpaService
import org.openurp.edu.eams.teach.grade.service.impl.GradeFilter
import org.openurp.edu.eams.teach.grade.service.stat.GradeReportSetting
import org.openurp.edu.eams.teach.grade.service.stat.MultiStdGrade
import org.openurp.edu.eams.teach.grade.service.stat.StdGpaHelper
import org.openurp.edu.eams.teach.grade.service.stat.StdTermCredit
import org.openurp.edu.eams.teach.grade.service.stat.StdTermGrade
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.helper.StdSearchHelper



class TermReportAction extends SemesterSupportAction {

  private var gpaService: GpaService = _

  private var courseGradeProvider: CourseGradeProvider = _

  private var stdSearchHelper: StdSearchHelper = _

  def stdList(): String = {
    put("students", entityDao.search(stdSearchHelper.buildStdQuery()))
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType]))
    put("printAt", new Date())
    put("GA", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.GA_ID))
    put("semesterId", get("semester.id"))
    forward()
  }

  def report(): String = {
    val setting = new GradeReportSetting()
    populate(setting, "reportSetting")
    setting.setFontSize(13)
    val stdList = entityDao.get(classOf[Student], Strings.splitToLong(get("std.ids")))
    val semesterId = getInt("semester.id")
    val semester = semesterService.getSemester(semesterId)
    val stdGradeReports = CollectUtils.newArrayList()
    for (std <- stdList) {
      val grades = courseGradeProvider.getPublished(std, semester)
      val stdTermGrade = new StdTermGrade(std, grades, null, new ArrayList[GradeFilter]())
      val stdGpa = new StdGpa(std)
      stdGpa.setGpa(gpaService.getGpa(std, grades))
      stdTermGrade.setStdGpa(stdGpa)
      if (true == setting.getPrintAwardCredit) {
      }
      stdGradeReports.add(stdTermGrade)
    }
    put("setting", setting)
    val orders = Order.parse(get("orderBy"))
    if (CollectUtils.isNotEmpty(orders)) {
      val order = orders.get(0)
      if (Strings.isNotBlank(order.getProperty) && order.getProperty != "null") {
        val orderCmp = new PropertyComparator(order.getProperty, order.isAscending)
        Collections.sort(stdGradeReports, orderCmp)
      }
    }
    put("pcf", getConfig.get("stdGrade.Explain"))
    put("school", getProject.getSchool)
    put("stdGradeReports", stdGradeReports)
    put("semester", semester)
    forward()
  }

  def multiStdReport(): String = {
    val semesterId = getInt("semester.id")
    val semester = semesterService.getSemester(semesterId)
    val stds = entityDao.get(classOf[Student], Strings.splitToLong(get("std.ids")))
    val setting = new GradeReportSetting()
    populate(setting, "reportSetting")
    setting.setGradeType(entityDao.get(classOf[GradeType], setting.gradeType.id))
    if (Strings.isEmpty(setting.getOrder.getProperty)) {
      setting.setOrder(Order.desc("semester.pga"))
    }
    if (setting.getPageSize.intValue() < 0) {
      setting.setPageSize(new java.lang.Integer(20))
    }
    var ratio = getFloat("ratio")
    if (null == ratio || ratio.floatValue() < 0 || ratio.floatValue() >= 1) {
      ratio = new java.lang.Float(0.15)
    }
    val grades = courseGradeProvider.getPublished(stds, semester)
    val multiStdGrade = new MultiStdGrade(semester, grades, ratio)
    StdGpaHelper.statGpa(multiStdGrade, gpaService)
    put("school", getProject.getSchool)
    put("setting", setting)
    put("multiStdGrades", Collections.singletonList(multiStdGrade))
    put("FINAL_ID", GradeTypeConstants.FINAL_ID)
    put("semesters", CollectUtils.newArrayList(semester))
    forward()
  }

  def lessHalfStat(): String = {
    val query = OqlBuilder.from(classOf[StdTermCredit], "stdTC")
    query.where("2 * stdTC.credits < stdTC.totalCredits")
    query.where("stdTC.semester.id = :semesterId", getInt("semester.id"))
    query.limit(getPageLimit)
    query.orderBy(Order.parse(get("orderBy")))
    put("stdTCs", entityDao.search(query))
    forward()
  }

  def setGpaService(gpaService: GpaService) {
    this.gpaService = gpaService
  }

  def setStdSearchHelper(stdSearchHelper: StdSearchHelper) {
    this.stdSearchHelper = stdSearchHelper
  }

  def setCourseGradeProvider(courseGradeProvider: CourseGradeProvider) {
    this.courseGradeProvider = courseGradeProvider
  }
}
