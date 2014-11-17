package org.openurp.eams.grade.service.internal

import org.openurp.eams.grade.service.GradeInputSwitchService
import org.openurp.eams.grade.GradeInputSwitch
import org.openurp.teach.core.Project
import org.openurp.base.Semester
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.dao.EntityDao

class GradeInputSwitchServiceImpl extends GradeInputSwitchService {

  var entityDao: EntityDao = _
  /**
   */
  def getSwitch(project: Project, semester: Semester): GradeInputSwitch = {
    val query = OqlBuilder.from(classOf[GradeInputSwitch], "switch");
    query
      .where("switch.opened = true")
      .where("current_time() between switch.startAt and switch.endAt")
      .orderBy("switch.id")
    val switches = entityDao.search(query)
    if (switches.isEmpty) null else switches(0)
  }
}