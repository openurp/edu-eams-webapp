package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.Semester
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.code.ScoreMarkStyle
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.code.ExamMode
import org.openurp.edu.teach.Course
import org.openurp.edu.base.Student

class CourseGradeAction extends RestfulAction[CourseGrade] {
  override def editSetting(entity: CourseGrade) = {

    val courseTypes = findItems(classOf[CourseType])
    put("courseTypes", courseTypes)

    val courses = findItems(classOf[Course])
    put("courses", courses)

    val examModes = findItems(classOf[ExamMode])
    put("examModes", examModes)

    val markStyles = findItems(classOf[ScoreMarkStyle])
    put("markStyles", markStyles)

    val semesters = findItems(classOf[Semester])
    put("semesters", semesters)

    val stds = findItems(classOf[Student])
    put("stds", stds)

    super.editSetting(entity)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("name")
    val items = entityDao.search(query)
    items
  }

}



