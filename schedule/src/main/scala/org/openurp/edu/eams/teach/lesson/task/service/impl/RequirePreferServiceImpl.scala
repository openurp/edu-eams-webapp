package org.openurp.edu.eams.teach.lesson.task.service.impl

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Course
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.teach.lesson.task.dao.RequirePreferDao
import org.openurp.edu.eams.teach.lesson.task.model.RequirePrefer
import org.openurp.edu.eams.teach.lesson.task.service.RequirePreferService
import org.openurp.edu.teach.lesson.Lesson
import org.beangle.data.model.dao.EntityDao



class RequirePreferServiceImpl extends BaseServiceImpl with RequirePreferService {

  var preferenceDao: RequirePreferDao = _
  
  var entityDao : EntityDao = _

  def getPreference(preferenceId: java.lang.Long): RequirePrefer = {
    preferenceDao.getPreference(preferenceId)
  }

  def savePreferenceForTask(lesson: Lesson) {
  }

  def getPreference(teacher: Teacher, course: Course): RequirePrefer = null

  def savePreference(preference: RequirePrefer) {
    if (null == 
      getPreference(preference.teacher, preference.course)) preferenceDao.savePreference(preference)
  }

  def setPreferenceFor(lessons: Iterable[_]) {
  }

  def setPreferenceFor(lessonIdSeq: String) {
    if (Strings.isNotEmpty(lessonIdSeq)) setPreferenceFor(entityDao.find(classOf[Lesson], Strings.splitToLong(lessonIdSeq)))
  }

  def updatePreference(preference: RequirePrefer) {
    preferenceDao.updatePreference(preference)
  }

  def setPreferenceDao(preferenceDao: RequirePreferDao) {
    this.preferenceDao = preferenceDao
  }
}
