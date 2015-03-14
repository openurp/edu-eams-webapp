package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Collection
import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeSwitch
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class CourseArrangeSwitchAction extends SemesterSupportAction {

  def index(): String = {
    val semesterId = getInt("semester.id")
    if (null != semesterId) {
      put("semester", entityDao.get(classOf[Semester], semesterId))
    }
    initNoSwitchSemesters()
    put("actionName", get("actionName"))
    forward()
  }

  def initNoSwitchSemesters() {
    val query = OqlBuilder.from(classOf[Semester], "semester")
    query.where("exists(from " + classOf[Project].getName + 
      " p where p.calendar=semester.calendar and p=:project)")
    val hql = "not exists (from org.openurp.edu.eams.teach.schedule.model.CourseArrangeSwitch switch where switch.semester = semester and switch.project = :project)"
    query.where(hql)
    query.param("project", getProject)
    val semesters = entityDao.search(query)
    if (CollectUtils.isNotEmpty(semesters)) {
      val semesterProjectIds = CollectUtils.newHashSet()
      val switches = CollectUtils.newArrayList()
      val project = getProject
      for (semester <- semesters) {
        val semesterProjectId = "semesterId:" + semester.getId.toString + ";projectId:" + 
          project.getId.toString
        if (!semesterProjectIds.contains(semesterProjectId)) {
          semesterProjectIds.add(semesterProjectId)
          switches.add(new CourseArrangeSwitch(semester, project))
        }
      }
      entityDao.saveOrUpdate(switches)
    }
  }

  def search(): String = {
    val query = OqlBuilder.from(classOf[CourseArrangeSwitch], "switch")
    populateConditions(query, "switch")
    query.where("switch.project = :project", getProject)
    query.limit(getPageLimit)
    val orderBy = get(Order.ORDER_STR)
    query.orderBy(if (Strings.isEmpty(orderBy)) "switch.semester.schoolYear desc,switch.semester.name desc" else orderBy)
    put("switches", entityDao.search(query))
    forward()
  }

  def updateStatus(): String = {
    val switches = entityDao.get(classOf[CourseArrangeSwitch], Strings.splitToLong(get("switchIds")))
    val published = getBoolean("published")
    val semesterId = getInt("semesterId")
    for (arrangeSwitch <- switches) {
      arrangeSwitch.setPublished(published.booleanValue())
    }
    try {
      entityDao.saveOrUpdate(switches)
    } catch {
      case e: Exception => return redirect("search", "info.save.failure", if (semesterId == null) null else "switch.semester.id=" + semesterId)
    }
    redirect("search", "info.save.success", if (semesterId == null) null else "switch.semester.id=" + semesterId)
  }
}
