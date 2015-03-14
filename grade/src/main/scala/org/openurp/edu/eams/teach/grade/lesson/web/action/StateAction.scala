package org.openurp.edu.eams.teach.grade.lesson.web.action

import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class StateAction extends SemesterSupportAction {

  def index(): String = {
    val lesson = entityDao.get(classOf[Lesson], getLong("lessonId"))
    val courseGradeStates = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    var gradeState: CourseGradeState = null
    if (CollectUtils.isNotEmpty(courseGradeStates)) {
      gradeState = courseGradeStates.get(0)
    }
    put("gradeState", gradeState)
    put("lesson", lesson)
    put("grades", entityDao.search(OqlBuilder.from(classOf[CourseGrade], "cg").where("cg.lesson=:lesson", 
      lesson)))
    forward()
  }
}
