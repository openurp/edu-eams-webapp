package org.openurp.edu.eams.web.action.selector

import org.openurp.edu.eams.web.action.common.RestrictionSupportAction

import scala.collection.JavaConversions._

class DirectionSelector extends RestrictionSupportAction {

  def withMajor(): String = "success"

  def allSecondDirections(): String = {
    val departmentIds = getDepartmentIdSeq
    "secondSuccess"
  }
}
