package org.openurp.edu.eams.base.web.action

import java.util.Date
import org.apache.commons.lang.time.DateUtils
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.struts2.convention.route.Action
import org.openurp.edu.eams.base.Calendar
import org.openurp.base.Semester
import org.openurp.base.model.SemesterBean
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction



class SemesterAction extends RestrictionSupportAction {

  def index(): String = {
    put("calendars", entityDao.getAll(classOf[Calendar]))
    forward()
  }

  def calendarInfo(): String = {
    val calendarId = getInt("calendar.id")
    put("calendar", entityDao.get(classOf[Calendar], calendarId))
    forward()
  }

  def semesterList(): String = {
    val calendarId = getInt("calendar.id")
    if (null == calendarId) return forwardError("error.education.id.needed")
    val query = OqlBuilder.from(classOf[Semester], "semester")
    query.where("semester.calendar.id=:calendarId", calendarId)
    query.orderBy(Order.parse(get(Order.ORDER_STR)))
    query.limit(getPageLimit)
    put("semesters", entityDao.search(query))
    forward()
  }

  def editSemester(): String = {
    val calendarId = getInt("calendar.id")
    val calendar = entityDao.get(classOf[Calendar], calendarId).asInstanceOf[Calendar]
    val semester = getEntity(classOf[Semester], "semester")
    put("calendar", calendar)
    put("semester", semester)
    forward()
  }

  def editCalendar(): String = {
    put("calendar", getEntity(classOf[Calendar], "calendar"))
    forward()
  }

  def saveSemester(): String = {
    val semester = populateEntity(classOf[Semester], "semester").asInstanceOf[Semester]
    val calendarId = getInt("calendar.id")
    val schoolcalendar = entityDao.get(classOf[Calendar], calendarId).asInstanceOf[Calendar]
    if (null == semester.getCalendar) {
      semester.setCalendar(schoolcalendar)
    }
    if (semesterService.checkDateCollision(semester)) return forwardError("error.semester.dateCollision")
    semesterService.saveSemester(semester)
    logHelper.info("add or update semester")
    redirect(new Action("", "semesterList", "&calendar.id=" + get("calendar.id")), "info.save.success")
  }

  def saveCalendar(): String = {
    val calendar = populateEntity(classOf[Calendar], "calendar")
    val query = OqlBuilder.from(classOf[Calendar], "calendar")
    query.where("calendar.name =:calendarName", calendar.getName)
    if (calendar.id != null) {
      query.where("calendar.id <> :calendarId", calendar.id)
    }
    if (Collections.isNotEmpty(entityDao.search(query))) {
      return redirect("index", "名称重复,保存失败")
    }
    calendar.setSchool(getSchool)
    var effectiveAt = calendar.getEffectiveAt
    effectiveAt = DateUtils.truncate(effectiveAt, java.util.Calendar.DATE)
    calendar.setEffectiveAt(effectiveAt)
    if (calendar.getInvalidAt != null) {
      var invalidAt = calendar.getInvalidAt
      invalidAt = DateUtils.truncate(invalidAt, java.util.Calendar.DATE)
      calendar.setEffectiveAt(invalidAt)
    }
    entityDao.saveOrUpdate(calendar)
    redirect("index", "info.save.success")
  }

  def removeCalendar(): String = {
    val calendar = getEntity(classOf[Calendar], "calendar")
    if (calendar.isPersisted) {
      try {
        entityDao.remove(calendar)
      } catch {
        case e: Exception => return redirect("index", "info.delete.failure")
      }
    }
    redirect("index", "info.delete.success")
  }

  def removeSemester(): String = {
    val semesterId = get("semester.id")
    val semester = semesterService.getSemester(java.lang.Integer.valueOf(semesterId))
    try {
      semesterService.removeSemester(semester)
    } catch {
      case e: Exception => return redirect("semesterList", "error.semester.deletedFailure", "&calendar.id=" + semester.getCalendar.id)
    }
    redirect("semesterList", "info.delete.success", "&calendar.id=" + semester.getCalendar.id)
  }

  def semesterInfo(): String = {
    val semesterId = getIntId("semester")
    if (null == semesterId) return forwardError(Array("entity.semester", "error.model.id.needed"))
    val semester = semesterService.getSemester(semesterId)
    if (null == semester) return forwardError(Array("entity.semester", "error.model.notExist"))
    put("dates", semester.asInstanceOf[SemesterBean].getWeekDates)
    put("semester", semester)
    forward()
  }
}
