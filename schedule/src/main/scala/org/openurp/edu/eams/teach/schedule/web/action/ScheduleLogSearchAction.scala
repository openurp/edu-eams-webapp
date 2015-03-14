package org.openurp.edu.eams.teach.schedule.web.action

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Collection
import java.util.Date
import java.util.List
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.ems.log.BusinessLog
import org.openurp.edu.eams.teach.schedule.log.ScheduleLogBuilder
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction

import scala.collection.JavaConversions._

class ScheduleLogSearchAction extends RestrictionSupportAction {

  def index(): String = {
    put("project", getProject)
    forward()
  }

  def search(): String = {
    put("logs", entityDao.search(buildOql()))
    forward()
  }

  def info(): String = {
    val logId = getLong("log.id")
    if (logId == null) {
      return forwardError("error.model.id.needed")
    }
    val log = entityDao.get(classOf[BusinessLog], logId)
    val pattern = Pattern.compile("LESSON\\.ID=(\\d+)\\s.*")
    val matcher = pattern.matcher(log.getDetail.getContent)
    if (matcher.find()) {
      val lessonId = matcher.group(1)
      val prevLogs = entityDao.search(OqlBuilder.from(classOf[BusinessLog], "log").where("log.detail.content like :pattern", 
        ScheduleLogBuilder.LESSON_ID + "=" + lessonId + "\n%")
        .where("log.id < :meId", log.getId)
        .where("log.resource = '排课日志'")
        .orderBy("log.operateAt desc"))
      if (CollectUtils.isNotEmpty(prevLogs)) {
        put("log_prev", prevLogs.get(0))
      }
    }
    put("log", log)
    forward()
  }

  protected def getExportDatas(): Collection[_] = {
    val logIds = getLongIds("log")
    if (logIds != null && logIds.length != 0) {
      return entityDao.search(buildOql().where("log.id in (:ids)", logIds).limit(null))
    }
    entityDao.search(buildOql().limit(null))
  }

  private def buildOql(): OqlBuilder[BusinessLog] = {
    val query = OqlBuilder.from(classOf[BusinessLog], "log")
    populateConditions(query)
    query.where("log.resource = '排课日志'")
    for (i <- 0 until ScheduleLogBuilder.LOG_FIELDS.length) {
      val key = ScheduleLogBuilder.LOG_FIELDS(i)
      val value = get(key)
      if (Strings.isNotEmpty(value)) {
        val keyBefore = if (i == 0) "" else "%" + ScheduleLogBuilder.LOG_FIELDS(i - 1) + "%"
        val keyAfter = if (i == ScheduleLogBuilder.LOG_FIELDS.length - 1) "" else ScheduleLogBuilder.LOG_FIELDS(i + 1) + "%"
        query.where("log.detail.content like :patterna" + i, keyBefore + key + "=" + "%" + value + "%" + keyAfter)
      }
    }
    val userName = get("log.user.name")
    if (Strings.isNotEmpty(userName)) {
      query.where("log.user.name like :username", "%" + userName + "%")
    }
    val logBegDate = get("logBegDate")
    val logEndDate = get("logEndDate")
    if (Strings.isNotEmpty(logBegDate)) {
      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val begDate = format.parse(logBegDate + " 00:00:00")
      query.where("log.operateAt >= :begDate", begDate)
    }
    if (Strings.isNotEmpty(logEndDate)) {
      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val endDate = format.parse(logEndDate + " 23:59:59")
      query.where("log.operateAt <= :endDate", endDate)
    }
    query.limit(getPageLimit)
    if (Strings.isEmpty(get("orderBy"))) {
      query.orderBy("log.operateAt desc")
    } else {
      query.orderBy(get("orderBy"))
    }
    query
  }
}
