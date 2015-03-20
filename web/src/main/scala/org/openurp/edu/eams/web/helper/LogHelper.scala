package org.openurp.edu.eams.web.helper

import org.beangle.data.model.dao.EntityDao
import org.beangle.commons.lang.Strings
import org.slf4j.Logger
import org.slf4j.LoggerFactory



class LogHelper {

  private var logger: Logger = LoggerFactory.getLogger(classOf[LogHelper])

  private var entityDao: EntityDao = _

  private def checkParams(content: String) {
    if (Strings.isEmpty(content)) {
      throw new RuntimeException("==> the params of [" + this.getClass.getName + ".info] of method is exception.")
    }
  }

  def info(content: String) {
  }

  def info(content: String, e: Exception) {
    info(content)
    logger.info(content, e)
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
