package org.openurp.edu.eams.teach.election.web.action.courseTake

import java.util.Date



import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class CourseTakeRestoreAction extends SemesterSupportAction {

  protected override def getEntityName(): String = classOf[ElectLogger].getName

  protected def indexSetting() {
    putSemester(null)
  }

  def restore(): String = {
    val builder = OqlBuilder.from(classOf[ElectLogger], "electLogger")
    builder.where("electLogger.semester=:semester", putSemester(null))
      .where("electLogger.type=:electionType", ElectRuleType.ELECTION)
      .where("not exists(from " + classOf[CourseTake].getName + " courseTake " + 
      "where courseTake.lesson.no=electLogger.lessonNo " + 
      "and courseTake.std.code=electLogger.stdCode) ")
    val startAt = getDateTime("startAt")
    val endAt = getDateTime("endAt")
    if (null != startAt) {
      builder.where("electLogger.createdAt >=:startAt", startAt)
      put("startAt", startAt)
    }
    if (null != endAt) {
      builder.where("electLogger.createdAt <=:endAt", endAt)
        .where("not exists(from " + classOf[ElectLogger].getName + " lg " + 
        "where lg.semester=electLogger.semester " + 
        "and lg.lessonNo = electLogger.lessonNo " + 
        "and lg.stdCode = electLogger.stdCode " + 
        "and lg.createdAt>electLogger.createdAt " + 
        "and lg.createdAt <=:endAt)", endAt)
      put("endAt", endAt)
    } else {
      builder.where("not exists(from " + classOf[ElectLogger].getName + " lg " + 
        "where lg.semester=electLogger.semester " + 
        "and lg.lessonNo = electLogger.lessonNo " + 
        "and lg.stdCode = electLogger.stdCode " + 
        "and lg.createdAt>electLogger.createdAt) ")
    }
    populateConditions(builder)
    val orderby = get(Order.ORDER_STR)
    if (Strings.isNotBlank(orderby)) {
      builder.orderBy(orderby + ",electLogger.createdAt,electLogger.id")
    } else {
      builder.orderBy("electLogger.createdAt,electLogger.id")
    }
    val loggers = entityDao.search(builder)
    val stds = Collections.newMap[Any]
    val lessons = Collections.newMap[Any]
    for (electLogger <- loggers) {
      var lesson = lessons.get(electLogger.getLessonNo)
      if (null == lesson) {
        val it = entityDao.get(classOf[Lesson], "no", electLogger.getLessonNo)
          .iterator()
        if (it.hasNext) {
          lesson = it.next()
          lessons.put(electLogger.getLessonNo, lesson)
        } else {
          //continue
        }
      }
      var std = stds.get(electLogger.getStdCode)
      if (null == std) {
        val it = entityDao.get(classOf[Student], "code", electLogger.getStdCode)
          .iterator()
        if (it.hasNext) {
          std = it.next()
          stds.put(electLogger.getStdCode, std)
        } else {
          //continue
        }
      }
    }
    put("electLoggers", loggers)
    put("stds", stds)
    put("lessons", lessons)
    forward()
  }

  def doRestore(): String = {
    val ids = getLongIds("courseTake")
    var success = 0
    var failure = 0
    if (ArrayUtils.isNotEmpty(ids)) {
      val loggers = entityDao.get(classOf[ElectLogger], ids)
      val stds = Collections.newMap[Any]
      val lessons = Collections.newMap[Any]
      val updatedAt = new Date()
      for (electLogger <- loggers) {
        val courseTake = Model.newInstance(classOf[CourseTake])
        courseTake.setUpdatedAt(updatedAt)
        courseTake.setCreatedAt(updatedAt)
        courseTake.setElectionMode(electLogger.getElectionMode)
        courseTake.setCourseTakeType(electLogger.getCourseTakeType)
        var lesson = lessons.get(electLogger.getLessonNo)
        if (null == lesson) {
          lesson = entityDao.get(classOf[Lesson], "no", electLogger.getLessonNo)
            .iterator()
            .next()
          lessons.put(electLogger.getLessonNo, lesson)
        }
        courseTake.setLesson(lesson)
        var std = stds.get(electLogger.getStdCode)
        if (null == std) {
          std = entityDao.get(classOf[Student], "code", electLogger.getStdCode)
            .iterator()
            .next()
          stds.put(electLogger.getStdCode, std)
        }
        courseTake.setStd(std)
        try {
          entityDao.saveOrUpdate(courseTake)
          success += 1
        } catch {
          case e: Exception => failure += 1
        }
      }
    }
    redirect("search", "成功恢复" + success + "条记录,失败" + failure + "条记录")
  }

  protected def getQueryBuilder(): OqlBuilder[_] = {
    val builder = super.getQueryBuilder
    val startAt = getDateTime("startAt")
    val endAt = getDateTime("endAt")
    if (null != startAt) {
      builder.where("electLogger.createdAt >=:startAt", startAt)
      put("startAt", startAt)
    }
    if (null != endAt) {
      builder.where("electLogger.createdAt <=:endAt", endAt)
      put("endAt", endAt)
    }
    builder.where("electLogger.semester=:semester", putSemester(null))
    builder
  }
}
