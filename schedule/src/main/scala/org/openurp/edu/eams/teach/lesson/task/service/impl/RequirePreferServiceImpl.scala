package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.util.Collection
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.dao.RequirePreferDao
import org.openurp.edu.eams.teach.lesson.task.model.RequirePrefer
import org.openurp.edu.eams.teach.lesson.task.service.RequirePreferService

import scala.collection.JavaConversions._

class RequirePreferServiceImpl extends BaseServiceImpl with RequirePreferService {

  private var preferenceDao: RequirePreferDao = _

  def getPreference(preferenceId: java.lang.Long): RequirePrefer = {
    preferenceDao.getPreference(preferenceId)
  }

  def savePreferenceForTask(lesson: Lesson) {
  }

  def getPreference(teacher: Teacher, course: Course): RequirePrefer = null

  def savePreference(preference: RequirePrefer) {
    if (null == 
      getPreference(preference.getTeacher, preference.getCourse)) preferenceDao.savePreference(preference)
  }

  def setPreferenceFor(lessons: Collection[_]) {
  }

  def setPreferenceFor(lessonIdSeq: String) {
    if (Strings.isNotEmpty(lessonIdSeq)) setPreferenceFor(entityDao.get(classOf[Lesson], Strings.splitToLong(lessonIdSeq)))
  }

  def updatePreference(preference: RequirePrefer) {
    preferenceDao.updatePreference(preference)
  }

  def setPreferenceDao(preferenceDao: RequirePreferDao) {
    this.preferenceDao = preferenceDao
  }
}
