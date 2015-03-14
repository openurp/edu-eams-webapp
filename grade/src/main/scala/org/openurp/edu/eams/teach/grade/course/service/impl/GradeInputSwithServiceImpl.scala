package org.openurp.edu.eams.teach.grade.course.service.impl

import java.util.Date
import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.grade.course.service.GradeInputSwitchService
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch

import scala.collection.JavaConversions._

class GradeInputSwithServiceImpl extends BaseServiceImpl with GradeInputSwitchService {

  def getSwitch(project: Project, semester: Semester): GradeInputSwitch = {
    val query = OqlBuilder.from(classOf[GradeInputSwitch], "switch")
    query.where("switch.project=:project", project)
    query.where("switch.semester=:semester", semester)
    query.where("switch.opened = true")
    entityDao.uniqueResult(query)
  }

  def getOpenedSemesters(project: Project): List[Semester] = {
    val query = OqlBuilder.from(classOf[GradeInputSwitch], "switch")
    query.where("switch.project=:project", project)
    query.where("switch.opened = true and switch.endAt>=:now", new Date())
    query.orderBy("switch.semester.beginOn").select("switch.semester")
    entityDao.search(query)
  }
}
