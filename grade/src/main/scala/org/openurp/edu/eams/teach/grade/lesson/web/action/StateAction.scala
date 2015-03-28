package org.openurp.edu.eams.teach.grade.lesson.web.action


import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class StateAction extends SemesterSupportAction {

  def index(): String = {
    val lesson = entityDao.get(classOf[Lesson], getLong("lessonId"))
    val courseGradeStates = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    var gradeState: CourseGradeState = null
    if (Collections.isNotEmpty(courseGradeStates)) {
      gradeState = courseGradeStates.get(0)
    }
    put("gradeState", gradeState)
    put("lesson", lesson)
    put("grades", entityDao.search(OqlBuilder.from(classOf[CourseGrade], "cg").where("cg.lesson=:lesson", 
      lesson)))
    forward()
  }
}
