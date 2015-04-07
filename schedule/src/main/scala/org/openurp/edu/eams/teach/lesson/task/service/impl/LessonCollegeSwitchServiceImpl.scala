package org.openurp.edu.eams.teach.lesson.task.service.impl


import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.lesson.task.model.LessonCollegeSwitch
import org.openurp.edu.eams.teach.lesson.task.service.LessonCollegeSwitchService
import org.beangle.data.model.dao.EntityDao
import org.beangle.commons.dao.impl.BaseServiceImpl



class LessonCollegeSwitchServiceImpl extends BaseServiceImpl with LessonCollegeSwitchService {
  

  private def setStatus(semesterId: java.lang.Integer, projectId: java.lang.Integer, allow: Boolean) {
    val query = OqlBuilder.from(classOf[LessonCollegeSwitch], "switch")
    query.where("switch.project.id=:projectId", projectId)
      .where("switch.semester.id=:semesterId", semesterId)
    val switches = entityDao.search(query)
    var clswitch: LessonCollegeSwitch = null
    if (Collections.isEmpty(switches)) {
      clswitch = new LessonCollegeSwitch()
      clswitch.project = entityDao.get(classOf[Project], projectId)
      clswitch.semester = entityDao.get(classOf[Semester], semesterId)
    } else {
      clswitch = switches(0)
    }
    clswitch.open = allow
    entityDao.save(clswitch)
  }

  def allow(semesterId: java.lang.Integer, projectId: java.lang.Integer) {
    setStatus(semesterId, projectId, true)
  }

  def disallow(semesterId: java.lang.Integer, projectId: java.lang.Integer) {
    setStatus(semesterId, projectId, false)
  }

  def status(semesterId: java.lang.Integer, projectId: java.lang.Integer): Boolean = {
    val query = OqlBuilder.from(classOf[LessonCollegeSwitch], "switch")
    query.where("switch.project.id=:projectId", projectId)
      .where("switch.semester.id=:semesterId", semesterId)
    val switches = entityDao.search(query)
    if (Collections.isEmpty(switches)) {
      return true
    }
    switches(0).open
  }
}
