package org.openurp.edu.eams.teach.service.impl

import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Map
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.dao.query.QueryBuilder
import org.beangle.commons.lang.BitStrings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.openurp.base.CourseUnit
import org.openurp.base.Semester
import org.openurp.edu.eams.base.util.WeekDay
import org.openurp.edu.eams.teach.service.OccupyProcessor
import org.openurp.edu.eams.teach.service.StdOccupyProvider
import org.openurp.edu.eams.teach.service.wrapper.TimeZone
import scala.collection.JavaConversions._
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.model.dao.QueryBuilder

abstract class AbstractStdOccupyProvider extends StdOccupyProvider {

  protected var logger: Logger = LoggerFactory.getLogger(getClass)

  protected var entityDao: EntityDao = _

  protected var semester: Semester = _

  def setEntityService(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setSemester(semester: Semester) {
    this.semester = semester
  }

  protected def executeOccupyQuery(query: QueryBuilder, zone: TimeZone, processor: OccupyProcessor): Map[_,_] = {
    val st = System.currentTimeMillis()
    var params = query.getParams
    if (null == params) {
      params = new HashMap()
    }
    val occupis = new HashMap()
    var iter = zone.getWeeks.iterator()
    while (iter.hasNext) {
      val week = iter.next().asInstanceOf[WeekDay]
      val weekOccupy = new HashMap()
      var iter2 = zone.getUnits.iterator()
      while (iter2.hasNext) {
        val unit = iter2.next().asInstanceOf[CourseUnit]
        for (weekState <- zone.getWeekStates) {
          params.put("weekId", new java.lang.Integer(week.getId.intValue()))
          params.put("startTime", unit.getStartTime)
          params.put("endTime", unit.getEndTime)
          params.put("weekState", BitStrings.binValueOf(weekState))
          query.params(params)
          val datas = entityDao.search(query)
          if (!datas.isEmpty) {
            processor.process(weekOccupy, unit, datas)
          }
        }
      }
      if (!weekOccupy.isEmpty) {
        occupis.put(week, weekOccupy)
      }
    }
    logger.info("occupy query consume time {} millis", System.currentTimeMillis() - st)
    occupis
  }
}
