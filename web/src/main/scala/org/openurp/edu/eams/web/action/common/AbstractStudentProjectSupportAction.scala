package org.openurp.edu.eams.web.action.common

import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student

import scala.collection.JavaConversions._

abstract class AbstractStudentProjectSupportAction extends MultiProjectSupportAction {

  protected override def getProjects(): List[Project] = {
    var projects = CollectUtils.newArrayList()
    val student = getLoginStudent
    if (student.getPerson != null) {
      val builder = OqlBuilder.from(classOf[Project], "project").select("select distinct project")
        .where("exists(from " + classOf[Student].getName + 
        " std where std.person.id = :personId and project = std.project and std.graduateOn > current_date())", 
        student.getPerson.getId)
      projects = entityDao.search(builder)
    } else {
      projects.add(student.getProject)
    }
    projects
  }

  protected def getProject(): Project = getLoginStudent.getProject
}
