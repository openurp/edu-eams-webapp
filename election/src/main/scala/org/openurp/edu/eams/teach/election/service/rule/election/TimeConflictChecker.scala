package org.openurp.edu.eams.teach.election.service.rule.election

import java.util.Collection
import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.beangle.ems.rule.Context
import org.beangle.ems.rule.model.RuleConfig
import org.beangle.ems.rule.model.RuleConfigParam
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext.Params
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import TimeConflictChecker._

import scala.collection.JavaConversions._

object TimeConflictChecker {

  val CHECK_CONFLICT = true

  val CHECK_UN_CONFLICT = false
}

class TimeConflictChecker extends AbstractElectRuleExecutor with ElectRulePrepare {

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    val electLesson = electContext.getLesson
    var unitCount = electContext.getState.getParams.get("MAX_TIME_CONFLICT_COUNT").asInstanceOf[java.lang.Integer]
      .intValue()
    if (unitCount < 0) unitCount = 0
    val checkType = electContext.getState.getParams.get("TIME_CONFLICT_CHECK_TYPE").asInstanceOf[java.lang.Boolean]
      .booleanValue()
    val isContinuous = electContext.getState.getParams.get("TIME_CONFLICT_IS_CONTINUOUS").asInstanceOf[java.lang.Boolean]
      .booleanValue()
    val electedLessons = electContext.getParams.get(Params.CONFLICT_COURSE_TAKES.toString).asInstanceOf[Collection[Lesson]]
    val electingLessons = electContext.getParams.get(Params.CONFLICT_LESSONS.toString).asInstanceOf[Collection[Lesson]]
    val conflictCourseTake = getConflictLessons(electLesson, electedLessons, unitCount, checkType, isContinuous)
    val conflictLessons = getConflictLessons(electLesson, electingLessons, unitCount, checkType, isContinuous)
    if (!conflictLessons.isEmpty || !conflictCourseTake.isEmpty) {
      val builder = new StringBuilder("与以下课程冲突 :")
      for (lesson <- conflictLessons) {
        builder.append("<dd>").append(lesson.getCourse.getName)
          .append("[")
          .append(lesson.getNo)
          .append("]")
          .append("</dd>")
      }
      for (lesson <- conflictCourseTake) {
        builder.append("<dd>").append(lesson.getCourse.getName)
          .append("[")
          .append(lesson.getNo)
          .append("]")
          .append("</dd>")
      }
      electContext.addMessage(new ElectMessage(builder.toString, ElectRuleType.ELECTION, false, electContext.getLesson))
      return false
    }
    true
  }

  def isConflict(lesson: Lesson, 
      lesson2: Lesson, 
      unitCount: Int, 
      checkType: Boolean, 
      isContinuous: Boolean): Boolean = {
    if (null == lesson || null == lesson2 || lesson == lesson2) return false
    val activities = lesson.getCourseSchedule.getActivities
    val activities2 = lesson2.getCourseSchedule.getActivities
    if (activities.isEmpty || activities2.isEmpty) {
      return false
    }
    var allUnitCount = 0
    var conflictCount = 0
    var maxConflict = 0
    var maxUnConflict = 0
    for (courseActivity <- activities) {
      val time = courseActivity.getTime
      val timeOneDayUnit = time.getEndUnit - time.getStartUnit + 1
      var oneDayUnConfictCount = timeOneDayUnit
      allUnitCount += timeOneDayUnit
      for (courseActivity2 <- activities2) {
        val time2 = courseActivity2.getTime
        if ((time.getWeekStateNum & time2.getWeekStateNum) > 0 && time.getWeekday == time2.getWeekday) {
          if (time.getStartUnit <= time2.getEndUnit && time.getEndUnit >= time2.getStartUnit) {
            oneDayUnConfictCount = 0
            if (unitCount < 2 && checkType) {
              return true
            } else {
              val minStart = Math.min(time.getStartUnit, time2.getStartUnit)
              val maxStart = Math.max(time.getStartUnit, time2.getStartUnit)
              val minEnd = Math.min(time.getEndUnit, time2.getEndUnit)
              val maxEnd = Math.max(time.getEndUnit, time2.getEndUnit)
              val oneDayConflictCount = (minEnd - maxStart + 1)
              conflictCount += oneDayConflictCount
              if (oneDayConflictCount > maxConflict) {
                maxConflict = oneDayConflictCount
              }
              if (minStart == time.getStartUnit) {
                val leftOneDayUnConflictCount = (maxStart - minStart)
                if (!isContinuous || leftOneDayUnConflictCount > 1) {
                  oneDayUnConfictCount += leftOneDayUnConflictCount
                }
              }
              if (maxEnd == time.getEndUnit) {
                val rightOneDayUnConflictCount = (maxEnd - minEnd)
                if (!isContinuous || rightOneDayUnConflictCount > 1) {
                  oneDayUnConfictCount += rightOneDayUnConflictCount
                }
              }
              if (oneDayUnConfictCount > maxUnConflict) {
                maxUnConflict = oneDayUnConfictCount
              }
            }
          }
        }
      }
      if (oneDayUnConfictCount > maxUnConflict) {
        maxUnConflict = oneDayUnConfictCount
      }
    }
    if (checkType) {
      if (conflictCount >= unitCount && conflictCount > 0) {
        return true
      }
    } else {
      if (maxUnConflict >= unitCount && maxUnConflict > 0) {
        return !(unitCount > 0)
      } else {
        return true
      }
    }
    false
  }

  def getConflictLessons(lesson: Lesson, 
      lessons: Collection[Lesson], 
      conflictTimeCount: Int, 
      checkType: Boolean, 
      isContinuous: Boolean): List[Lesson] = {
    val temp = CollectUtils.newHashSet()
    val result = CollectUtils.newArrayList()
    if (null != lessons) {
      for (lesson2 <- lessons) {
        if (temp.contains(lesson2)) {
          //continue
        }
        if (isConflict(lesson, lesson2, conflictTimeCount, checkType, isContinuous)) {
          result.add(lesson2)
        }
        temp.add(lesson2)
      }
    }
    result
  }

  def getConflictLessonsWithCourseTakes(lesson: Lesson, 
      courseTakes: Collection[CourseTake], 
      timeConflictCount: Int, 
      checkType: Boolean, 
      isContinuous: Boolean): List[Lesson] = {
    val temp = CollectUtils.newHashSet()
    val result = CollectUtils.newArrayList()
    if (null != courseTakes) {
      for (courseTake <- courseTakes) {
        val lesson2 = courseTake.getLesson
        if (temp.contains(lesson2)) {
          //continue
        }
        if (isConflict(lesson, lesson2, timeConflictCount, checkType, isContinuous)) {
          result.add(lesson2)
        }
        temp.add(lesson2)
      }
    }
    result
  }

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.CHECK_TIME_CONFLICT)) {
      context.getState.setCheckTimeConflict(true)
      var conflictTimeCount = 0
      var checkType = CHECK_CONFLICT
      var isContinuous = true
      for (config <- context.getState.getProfile(entityDao).getElectConfigs if config.getRule.getServiceName.toUpperCase() == this.getClass.getSimpleName.toUpperCase()) {
        val s = CollectUtils.newHashSet("真", "是", "YES", "Y", "TRUE", "T")
        for (param <- config.getParams) {
          if (param.getParam.getName.trim() == "unitCount") {
            conflictTimeCount = java.lang.Integer.parseInt(Strings.trim(param.getValue))
          } else if (param.getParam.getName.trim() == "checkType") {
            val valStr = Strings.trim(param.getValue)
            checkType = null != valStr && s.contains(valStr.toUpperCase())
          } else if (param.getParam.getName.trim() == "isContinuous") {
            val valStr = Strings.trim(param.getValue)
            isContinuous = null != valStr && s.contains(valStr.toUpperCase())
          }
        }
      }
      context.getState.getParams.put("MAX_TIME_CONFLICT_COUNT", conflictTimeCount)
      context.getState.getParams.put("TIME_CONFLICT_CHECK_TYPE", checkType)
      context.getState.getParams.put("TIME_CONFLICT_IS_CONTINUOUS", isContinuous)
      context.addPreparedDataName(PreparedDataName.CHECK_TIME_CONFLICT)
    }
  }
}
