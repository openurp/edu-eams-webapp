package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.lesson.task.model.LessonCollegeSwitch
import org.openurp.edu.eams.teach.lesson.task.service.LessonCollegeSwitchService

import scala.collection.JavaConversions._

class LessonCollegeSwitchServiceImpl extends BaseServiceImpl with LessonCollegeSwitchService {

  private def setStatus(semesterId: java.lang.Integer, projectId: java.lang.Integer, allow: Boolean) {
    val query = OqlBuilder.from(classOf[LessonCollegeSwitch], "switch")
    query.where("switch.project.id=:projectId", projectId)
      .where("switch.semester.id=:semesterId", semesterId)
    val switches = entityDao.search(query)
    var clswitch: LessonCollegeSwitch = null
    if (CollectUtils.isEmpty(switches)) {
      clswitch = new LessonCollegeSwitch()
      clswitch.setProject(entityDao.get(classOf[Project], projectId))
      clswitch.setSemester(entityDao.get(classOf[Semester], semesterId))
    } else {
      clswitch = switches.get(0)
    }
    clswitch.setOpen(allow)
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
    if (CollectUtils.isEmpty(switches)) {
      return true
    }
    switches.get(0).isOpen
  }
}
