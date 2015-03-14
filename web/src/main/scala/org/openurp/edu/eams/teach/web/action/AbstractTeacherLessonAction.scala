package org.openurp.edu.eams.teach.web.action

import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.action.common.MultiProjectSupportAction
import org.openurp.edu.eams.web.helper.RestrictionHelper

import scala.collection.JavaConversions._

abstract class AbstractTeacherLessonAction extends MultiProjectSupportAction {

  protected var restrictionHelper: RestrictionHelper = _

  protected override def getProjects(): List[Project] = {
    val teacher = getLoginTeacher
    val builder = OqlBuilder.from(classOf[Lesson], "lesson").join("lesson.teachers", "teacher")
      .where("teacher=:me", teacher)
    builder.select("distinct lesson.project")
    var projects = entityDao.search(builder)
    if (CollectUtils.isEmpty(projects)) {
      projects = entityDao.search(OqlBuilder.from(classOf[Project], "p").orderBy("p.id"))
      return CollectUtils.newArrayList(projects.get(0))
    }
    projects
  }
}
