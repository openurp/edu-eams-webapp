package org.openurp.edu.eams.system.report

import org.openurp.edu.base.Project
import org.beangle.data.model.LongIdEntity


trait ReportTemplate extends LongIdEntity{

  def getProject(): Project

  def setProject(project: Project): Unit

  def getCategory(): String

  def setCategory(category: String): Unit

  def getCode(): String

  def setCode(code: String): Unit

  def getName(): String

  def setName(name: String): Unit

  def getTemplate(): String

  def setTemplate(template: String): Unit

  def getRemark(): String

  def setRemark(remark: String): Unit

  def getOptions(): String

  def setOptions(options: String): Unit

  def getPageSize(): String

  def getOrientation(): String
}
