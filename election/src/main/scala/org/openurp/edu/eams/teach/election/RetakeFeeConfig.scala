package org.openurp.edu.eams.teach.election

import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.fee.FeeConfig

import scala.collection.JavaConversions._

trait RetakeFeeConfig extends FeeConfig {

  def getSemester(): Semester

  def setSemester(semester: Semester): Unit

  def getProject(): Project

  def setProject(project: Project): Unit

  def getPricePerCredit(): Int

  def setPricePerCredit(pricePerCredit: Int): Unit
}
