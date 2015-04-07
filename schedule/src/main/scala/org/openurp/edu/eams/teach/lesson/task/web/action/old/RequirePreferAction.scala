package org.openurp.edu.eams.teach.lesson.task.web.action.old

import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.core.service.TeacherService
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.model.RequirePrefer
import org.openurp.edu.eams.teach.lesson.task.service.RequirePreferService
import RequirePreferAction._



object RequirePreferAction {

  var inPreference: String = "inPreference"

  var inTask: String = "inTask"
}

class RequirePreferAction extends BaseAction {

  var semesterService: SemesterService = _

  var preferenceService: RequirePreferService = _

  var teacherService: TeacherService = _

  def index(): String = {
    val user = getUser
    val teacher = teacherService.getTeacherByNO(user)
    val educationTypes: List[_] = null
    if (educationTypes.isEmpty) {
      return forwardError("error.teacher.noTask")
    }
    put("educationTypes", educationTypes)
    val semester = getSemester
    put(Constants.CALENDAR, semester)
    put(Constants.TEACHER, teacher)
    forward()
  }

  private def getSemester(): Semester = {
    val semester = populate(classOf[Semester], Constants.CALENDAR).asInstanceOf[Semester]
    if (null != semester.id && 0 != semester.id.intValue()) semesterService.getSemester(semester.id) else if (null != semester.getCalendar && semester.getCalendar.isPersisted) {
      semesterService.getSemester(semester.getCalendar, semester.getSchoolYear, semester.getName)
    } else {
      null
    }
  }

  def taskList(): String = {
    val teacher = getLoginTeacher
    val semester = getSemester
    put(Constants.CALENDAR, semester)
    forward()
  }

  def preferList(): String = {
    var teacherId = getLong(Constants.TEACHER_KEY)
    if (null == teacherId) {
      val user = getUser
      val teacher = teacherService.getTeacherByNO(user)
      teacherId = teacher.id
    }
    forward()
  }

  def edit(): String = {
    val `type` = get("requirementType")
    if (Strings.isEmpty(`type`)) return forwardError("error.requirementType.unknown")
    if (`type` == inPreference) {
      val preferenceId = get(Constants.REQUIREMENTPREFERENCE_KEY)
      put(Constants.REQUIREMENTPREFERENCE, preferenceService.getPreference(java.lang.Long.valueOf(preferenceId)))
    } else {
      val taskId = get(Constants.TEACHTASK_KEY)
    }
    put("configTypeList", baseCodeService.getCodes(classOf[RoomType]))
    addBaseCode("teachLangTypes", classOf[TeachLangType])
    forward()
  }

  def editTextbook(): String = {
    val `type` = get("requirementType")
    if (`type` == inPreference) {
      val requirePreferId = getLong("requirePrefer.id")
      val prefer = entityDao.get(classOf[RequirePrefer], requirePreferId).asInstanceOf[RequirePrefer]
    } else {
      val taskId = getLong("task.id")
      val task = entityDao.get(classOf[Lesson], taskId).asInstanceOf[Lesson]
    }
    put("presses", baseCodeService.getCodes(classOf[Press]))
    put("bookTypes", baseCodeService.getCodes(classOf[BookType]))
    forward()
  }

  def saveTextbook(): String = {
    val book = populate(classOf[Textbook], "book").asInstanceOf[Textbook]
    if (!book.isPersisted) {
      entityDao.saveOrUpdate(book)
    }
    val forward = get("forward")
    val `type` = get("requirementType")
    if (`type` == inPreference) {
      val requirePreferId = getLong("requirePrefer.id")
      val prefer = entityDao.get(classOf[RequirePrefer], requirePreferId).asInstanceOf[RequirePrefer]
      entityDao.saveOrUpdate(prefer)
      redirect(forward, "info.save.success")
    } else {
      val taskId = getLong("task.id")
      val task = entityDao.get(classOf[Lesson], taskId).asInstanceOf[Lesson]
      redirect(forward, "info.save.success", "&semester.id=" + task.getSemester.id)
    }
  }

  def removeTextbook(): String = {
    val `type` = get("requirementType")
    val forward = get("forward")
    val bookId = getLong("book.id")
    val book = entityDao.get(classOf[Textbook], bookId).asInstanceOf[Textbook]
    if (`type` == inPreference) {
      val preferenceId = getLong("requirePrefer.id")
      val prefer = entityDao.get(classOf[RequirePrefer], preferenceId).asInstanceOf[RequirePrefer]
      entityDao.saveOrUpdate(prefer)
      redirect(forward, "info.save.success")
    } else {
      val taskId = getLong("task.id")
      val task = entityDao.get(classOf[Lesson], taskId).asInstanceOf[Lesson]
      entityDao.saveOrUpdate(task)
      redirect(forward, "info.delete.success", "&semester.id=" + task.getSemester.id)
    }
  }

  def saveRequirement(): String = null

  def setPreferForTask(): String = {
    val taskIdSeq = get("lessonIds")
    if (Strings.isEmpty(taskIdSeq)) return forwardError("error.teachTask.ids.needed")
    preferenceService.setPreferenceFor(taskIdSeq)
    addFlashMessage("prompt.requirementPreference.update.success")
    redirect("taskList", "info.save.success")
  }

  def retrievePreferenceForTask(): String = {
    val taskId = getLong("lesson.id")
    val task = entityDao.get(classOf[Lesson], taskId)
    put(Constants.TEACHTASK, task)
    put("configTypeList", baseCodeService.getCodes(classOf[RoomType]))
    val teacher = getLoginTeacher
    if (null == teacher) return forwardError("error.parameters.illegal")
    var preference = preferenceService.getPreference(teacher, task.getCourse)
    if (null == preference) {
      preference = new RequirePrefer(teacher, task.getCourse)
      preferenceService.savePreference(preference)
    } else {
      preferenceService.setPreferenceFor(Collections.singletonList(task))
    }
    redirect(new Action("", "edit"), "info.save.success", Array("requirementType", "task"))
  }
}
