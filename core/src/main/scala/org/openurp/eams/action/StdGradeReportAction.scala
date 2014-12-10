package org.openurp.eams.action

import org.beangle.data.model.Entity
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import javax.swing.AbstractAction
import org.beangle.webmvc.entity.action.AbstractEntityAction
import org.openurp.teach.core.Student
import org.openurp.teach.grade.CourseGrade
import org.beangle.webmvc.api.context.ContextHolder

class StdGradeReportAction extends AbstractEntityAction {
  def index(): String = {
    val stdCode = "2007137130"
    val stds = entityDao.findBy(classOf[Student], "code", List(stdCode))
    put("stdGradeReports", List(Report(stds.head, List.empty)))
    forward("index_"+ContextHolder.context.locale.getLanguage)
  }
}

case class Report(std: Student, grades: Seq[CourseGrade])