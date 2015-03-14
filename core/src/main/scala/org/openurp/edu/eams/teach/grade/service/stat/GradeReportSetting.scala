package org.openurp.edu.eams.teach.grade.service.stat

import org.openurp.edu.eams.teach.lesson.GradeTypeConstants.FINAL_ID
import java.util.Date
import org.beangle.commons.collection.Order
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.code.industry.GradeType

import scala.collection.JavaConversions._

class GradeReportSetting {

  var printGpa: Boolean = true

  var printTermGpa: Boolean = false

  var gradeFilters: String = _

  var pageSize: java.lang.Integer = new java.lang.Integer(80)

  var fontSize: java.lang.Integer = new java.lang.Integer(10)

  var project: Project = _

  var printAwardCredit: java.lang.Boolean = true

  var printOtherGrade: java.lang.Boolean = true

  var order: Order = new Order()

  var gradeType: GradeType = new GradeType(FINAL_ID)

  var printBy: String = _

  var template: String = _

  var printAt: Date = new Date()

  def getPrintOtherGrade(): java.lang.Boolean = printOtherGrade

  def setPrintOtherGrade(printOtherGrade: java.lang.Boolean) {
    this.printOtherGrade = printOtherGrade
  }

  def getFontSize(): java.lang.Integer = fontSize

  def setFontSize(fontSize: java.lang.Integer) {
    this.fontSize = fontSize
  }

  def getPageSize(): java.lang.Integer = pageSize

  def setPageSize(pageSize: java.lang.Integer) {
    this.pageSize = pageSize
  }

  def getPrintAwardCredit(): java.lang.Boolean = printAwardCredit

  def setPrintAwardCredit(printAwardCredit: java.lang.Boolean) {
    this.printAwardCredit = printAwardCredit
  }

  def getOrder(): Order = order

  def setOrder(order: Order) {
    this.order = order
  }

  def getGradeType(): GradeType = gradeType

  def setGradeType(gradeType: GradeType) {
    this.gradeType = gradeType
  }

  def getPrintBy(): String = printBy

  def setPrintBy(printBy: String) {
    this.printBy = printBy
  }

  def getTemplate(): String = template

  def setTemplate(template: String) {
    this.template = template
  }

  def getProject(): Project = project

  def setProject(project: Project) {
    this.project = project
  }

  def getPrintAt(): Date = printAt

  def setPrintAt(printAt: Date) {
    this.printAt = printAt
  }

  def isPrintGpa(): Boolean = printGpa

  def setPrintGpa(printGpa: Boolean) {
    this.printGpa = printGpa
  }

  def isPrintTermGpa(): Boolean = printTermGpa

  def setPrintTermGpa(printTermGpa: Boolean) {
    this.printTermGpa = printTermGpa
  }

  def getGradeFilters(): String = gradeFilters

  def setGradeFilters(gradeFilters: String) {
    this.gradeFilters = gradeFilters
  }
}
