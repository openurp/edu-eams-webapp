package org.openurp.edu.eams.teach.grade.service.impl

import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.openurp.base.Semester
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.model.StdSemesterGpa

import scala.collection.JavaConversions._

class MultiStdGpa {

  var unit: AnyRef = _

  var semesters: List[Semester] = _

  var stdGpas: List[StdGpa] = CollectUtils.newArrayList()

  def this(unit: AnyRef) {
    super()
    this.unit = unit
  }

  def getSemesters(): List[Semester] = semesters

  def statSemestersFromStdGpa() {
    val semesterFromStdGpa = CollectUtils.newHashSet()
    for (stdGp <- stdGpas; stdSemesterGpa <- stdGp.getSemesterGpas) {
      semesterFromStdGpa.add(stdSemesterGpa.getSemester)
    }
    semesters = CollectUtils.newArrayList(semesterFromStdGpa)
  }

  def setSemesters(semesters: List[Semester]) {
    this.semesters = semesters
  }

  def getStdGpas(): List[StdGpa] = stdGpas

  def setStdGpas(stdGpas: List[StdGpa]) {
    this.stdGpas = stdGpas
  }
}
