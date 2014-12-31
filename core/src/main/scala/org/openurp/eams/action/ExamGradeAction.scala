package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.edu.teach.grade.{CourseGrade, ExamGrade}
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.ScoreMarkStyle

class ExamGradeAction extends RestfulAction[ExamGrade] {
  override def editSetting(entity: ExamGrade) = {
    val gradeTypes = findItems(classOf[GradeType])
    put("gradeTypes", gradeTypes)

    val markStyles = findItems(classOf[ScoreMarkStyle])
    put("markStyles", markStyles)

    val examStatuses = findItems(classOf[ExamStatus])
    put("examStatuses", examStatuses)

    val courseGrades = findItems(classOf[CourseGrade])
    put("courseGrades", courseGrades)

    super.editSetting(entity)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("name")
    val items = entityDao.search(query)
    items
  }
}

