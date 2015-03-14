package org.openurp.edu.eams.teach.lesson.task.web.action

import java.util.List
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonOperateViolation
import org.openurp.edu.eams.teach.lesson.task.web.action.parent.LessonManagerCoreAction

import scala.collection.JavaConversions._

class TeachTaskCollegeAction extends LessonManagerCoreAction {

  override def copyViolationCheck(lesson: Lesson, semester: Semester): LessonOperateViolation = {
    if (!lessonCollegeSwitchService.status(semester.getId, lesson.getProject.getId)) {
      return LessonOperateViolation.PERMIT_VIOLATION
    }
    LessonOperateViolation.NO_VIOLATION
  }

  override def operateViolationCheck(lesson: Lesson): LessonOperateViolation = {
    if (!lessonCollegeSwitchService.status(lesson.getSemester.getId, lesson.getProject.getId)) {
      return LessonOperateViolation.PERMIT_VIOLATION
    }
    if (lesson.isPersisted) {
      val state = lesson.getAuditStatus
      if (CommonAuditState.ACCEPTED == state || CommonAuditState.SUBMITTED == state) {
        return LessonOperateViolation.LESSON_VIOLATION
      }
    }
    LessonOperateViolation.NO_VIOLATION
  }

  override def operateViolationCheck(lessons: List[Lesson]): LessonOperateViolation = {
    for (lesson <- lessons) operateViolationCheck(lesson) match {
      case PERMIT_VIOLATION => return LessonOperateViolation.PERMIT_VIOLATION
      case LESSON_VIOLATION => return LessonOperateViolation.LESSON_VIOLATION
    }
    LessonOperateViolation.NO_VIOLATION
  }

  def submitLessons(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    for (lesson <- lessons if lesson.getAuditStatus == CommonAuditState.UNSUBMITTED || 
      lesson.getAuditStatus == CommonAuditState.REJECTED) {
      lesson.setAuditStatus(CommonAuditState.SUBMITTED)
    }
    entityDao.saveOrUpdate(lessons)
    redirect("search", "info.save.success", get("params"))
  }
}
