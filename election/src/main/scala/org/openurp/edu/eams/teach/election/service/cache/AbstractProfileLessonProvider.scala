package org.openurp.edu.eams.teach.election.service.cache

import org.beangle.commons.dao.EntityDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory



abstract class AbstractProfileLessonProvider {

  protected var entityDao: EntityDao = _

  protected val logger = LoggerFactory.getLogger(this.getClass)

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
