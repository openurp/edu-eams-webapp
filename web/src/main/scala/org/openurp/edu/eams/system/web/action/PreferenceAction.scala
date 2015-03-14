package org.openurp.edu.eams.system.web.action

import java.util.ArrayList
import java.util.Iterator
import java.util.List
import org.beangle.commons.config.property.PropertyConfig
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.nav.Menu
import org.beangle.security.blueprint.nav.MenuProfile
import org.beangle.security.blueprint.nav.service.MenuService
import org.openurp.edu.eams.base.Calendar
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class PreferenceAction extends SemesterSupportAction {

  protected var menuService: MenuService = _

  def index(): String = {
    var calendar: Calendar = null
    val calendarId = getCookieValue("semester.calendar.id")
    val year = getCookieValue("semester.schoolYear")
    val term = getCookieValue("semester.name")
    if (Strings.isNotEmpty(calendarId) && Strings.isNotEmpty(year) && 
      Strings.isNotEmpty(term)) {
      val builder = OqlBuilder.from(classOf[Semester], "semester").where("semester.schoolYear=:schoolYear", 
        year)
        .where("semester.name=:name", term)
        .where("semester.calendar.id=:id", java.lang.Integer.valueOf(calendarId))
      val semesterIterator = entityDao.search(builder).iterator()
      if (semesterIterator.hasNext) {
        put("semester", semesterIterator.next())
        calendar = entityDao.get(classOf[Calendar], java.lang.Integer.valueOf(calendarId))
        put("academicCalendar", calendar)
      }
    }
    val calendars = entityDao.getAll(classOf[Calendar])
    if (null == getAttribute("semester")) {
      val builder = OqlBuilder.from(classOf[Semester], "semester")
      builder.where("semester.calendar=:calendar", calendars.get(0))
      val semesters = entityDao.search(builder)
      val semester = semesters.get(0).asInstanceOf[Semester]
      put("semester", semester)
    }
    put("academicCalendar", calendar)
    val builder = OqlBuilder.from(classOf[Semester], "semester").where("semester.calendar=:calendar", 
      calendar)
    val semesters = entityDao.search(builder)
    put("calendars", semesters)
    var pageSize = getCookieValue("pageSize")
    if (null == pageSize) pageSize = "20"
    put("pageSize", pageSize)
    val config = getConfig
    var facade = getCookieValue("system.facade")
    if (null == facade) facade = config.get("system.facade").asInstanceOf[String]
    put("facade", facade)
    var language = getCookieValue("language")
    if (null == language) language = "zh_CN"
    put("language", language)
    val username = getCookieValue("name")
    if (null == username) put("rememberMe", false) else put("rememberMe", true)
    forward()
  }

  def save(): String = {
    val cookieAge = 60 * 60 * 24 * 30 * 6
    addCookie("semester.calendar.id", get("semester.calendar.id"), cookieAge)
    addCookie("semester.schoolYear", get("semester.schoolYear"), cookieAge)
    addCookie("semester.name", get("semester.name"), cookieAge)
    addCookie("pageSize", get("pageSize"), cookieAge)
    addCookie("system.facade", get("system.facade"), cookieAge)
    val rememberMe = getBoolean("rememberMe")
    val user = entityDao.get(classOf[User], getUserId)
    if (true == rememberMe) {
      addCookie("name", user.getName, cookieAge)
      addCookie("password", user.getPassword, cookieAge)
    } else {
      deleteCookie("name")
      deleteCookie("password")
    }
    addCookie("language", get("language"), cookieAge)
    redirect("index", "info.set.success")
  }

  def menuList(): String = {
    val user = entityDao.get(classOf[User], getUserId)
    val builder = OqlBuilder.from(classOf[MenuProfile], "profile").where("profile.role in(:roles)", user.getRoles)
    val profiles = entityDao.search(builder)
    var menus = new ArrayList[Menu]()
    if (!profiles.isEmpty) {
      var profile = profiles.get(0).asInstanceOf[MenuProfile]
      val profileId = getInt("menuProfile.id")
      if (null != profileId) {
        for (one <- profiles if one.getId == profileId) {
          profile = one
          //break
        }
      }
      menus = menuService.getMenus(profile, user, user.getProfiles)
    }
    put("menus", menus)
    forward()
  }

  def setMenuService(menuService: MenuService) {
    this.menuService = menuService
  }
}
