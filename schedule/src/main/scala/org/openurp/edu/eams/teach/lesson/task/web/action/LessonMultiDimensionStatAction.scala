package org.openurp.edu.eams.teach.lesson.task.web.action

import java.util.Collections
import java.util.List
import org.beangle.commons.collection.Order
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.lesson.task.service.LessonStatService
import org.openurp.edu.eams.util.stat.StatGroup
import org.openurp.edu.eams.util.stat.StatItemComparator
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class LessonMultiDimensionStatAction extends SemesterSupportAction {

  var lessonStatService: LessonStatService = _

  def index(): String = {
    setSemesterDataRealm(hasStdTypeDepart)
    put("kind", get("kind"))
    forward()
  }

  def statLessonCount(): String = {
    val semester = entityDao.get(classOf[Semester], getInt("lesson.semester.id"))
    val project = entityDao.get(classOf[Project], getInt("lesson.project.id"))
    var stats: List[_] = null
    val kind = get("kind")
    if ("class" == kind) {
      stats = lessonStatService.countByAdminclass(project, semester, getDataRealm)
    } else if ("teacher" == kind) {
      stats = lessonStatService.countByTeacher(project, semester, getDataRealm)
    } else if ("courseType" == kind) {
      stats = lessonStatService.countByCourseType(project, semester, getDataRealm)
    } else if ("teachDepart" == kind) {
      stats = lessonStatService.countByTeachDepart(project, semester, getDataRealm)
    } else if ("studentType" == kind) {
      stats = lessonStatService.countByStdType(project, semester, getDataRealm)
    } else {
      return forwardError("不支持的统计类型")
    }
    var orderBy = get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "what.name"
    }
    val orders = Order.parse(orderBy)
    if (!orders.isEmpty) {
      val order = orders.get(0).asInstanceOf[Order]
      if ("null" != order.getProperty) {
        val comparator = new StatItemComparator(order)
        Collections.sort(stats, comparator)
      }
    }
    put("stats", stats)
    forward()
  }

  def statLessonConfirm(): String = {
    val semester = entityDao.get(classOf[Semester], getInt("lesson.semester.id"))
    val project = entityDao.get(classOf[Project], getInt("lesson.project.id"))
    var stats: List[_] = null
    val kind = get("kind")
    if ("teachDepart" == kind) {
      stats = lessonStatService.statTeachDepartConfirm(project, semester, getDataRealmLimit.getDataRealm)
    } else if ("courseType" == kind) {
      stats = lessonStatService.statCourseTypeConfirm(project, semester, getDataRealmLimit.getDataRealm)
    } else {
      throw new RuntimeException("unsuported confirm stat kind [" + kind + "]!")
    }
    var orderBy = get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "what.name"
    }
    val orders = Order.parse(orderBy)
    if (!orders.isEmpty) {
      val order = orders.get(0).asInstanceOf[Order]
      if ("null" != order.getProperty) {
        val comparator = new StatItemComparator(order)
        Collections.sort(stats, comparator)
      }
    }
    put("stats", stats)
    forward()
  }

  def setLessonStatService(lessonStatService: LessonStatService) {
    this.lessonStatService = lessonStatService
  }
}
