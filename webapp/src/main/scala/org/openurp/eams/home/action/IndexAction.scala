package org.openurp.eams.home.action

import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.annotation.action
import java.util.Date
import java.util.Calendar
import org.openurp.base.model.PersonBean
import scala.collection.mutable.ListBuffer
import org.openurp.base.Person
import org.beangle.data.model.dao.EntityDao
import org.openurp.teach.core.Project
import org.openurp.base.Semester
import ch.qos.logback.core.util.EnvUtil
import org.openurp.eams.home.EnvUtils

@action("")
class IndexAction(entityDao: EntityDao) extends ActionSupport {

  def index(): String = {
    put("projects", entityDao.getAll(classOf[Project]))
    put("semesters", entityDao.getAll(classOf[Semester]))
    val projectId = addToCookie(EnvUtils.PROJECT_ID)
    val semesterId = addToCookie(EnvUtils.SEMESTER_ID)
    put(EnvUtils.PROJECT_ID, projectId)
    put(EnvUtils.SEMESTER_ID, semesterId)
    forward()
  }

  def submenus(): String = {
    forward()
  }

  def childmenus() = {
    forward()
  }

  private def addToCookie(name: String): String = {
    val value = get(name)
    value.foreach(projectId => {
      addCookie(name, projectId, Int.MaxValue)
    })
    value.getOrElse(getCookieValue(name))
  }
}