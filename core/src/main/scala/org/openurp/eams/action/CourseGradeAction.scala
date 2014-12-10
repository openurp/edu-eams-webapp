package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.Semester
import org.openurp.teach.code.{ CourseType, ExamMode, ScoreMarkStyle }
import org.openurp.teach.core.{ Course, Student }
import org.openurp.teach.grade.CourseGrade

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



