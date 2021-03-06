package org.openurp.edu.eams.teach.web.action


import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.action.common.MultiProjectSupportAction
import org.openurp.edu.eams.web.helper.RestrictionHelper



abstract class AbstractTeacherLessonAction extends MultiProjectSupportAction {

  var restrictionHelper: RestrictionHelper = _

  protected override def getProjects(): List[Project] = {
    val teacher = getLoginTeacher
    val builder = OqlBuilder.from(classOf[Lesson], "lesson").join("lesson.teachers", "teacher")
      .where("teacher=:me", teacher)
    builder.select("distinct lesson.project")
    var projects = entityDao.search(builder)
    if (Collections.isEmpty(projects)) {
      projects = entityDao.search(OqlBuilder.from(classOf[Project], "p").orderBy("p.id"))
      return Collections.newBuffer[Any](projects.get(0))
    }
    projects
  }
}
