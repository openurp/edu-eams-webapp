package org.openurp.edu.eams.teach.grade.lesson.web.action

import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants

import scala.collection.JavaConversions._

class AuditAction extends InputAction {

  def drawback(): String = {
    val gradeState = getEntity(classOf[CourseGradeState], "gradeState")
    gradeState.setAuditReason(get("auditReason"))
    gradeState.setStatus(Grade.Status.NEW)
    val gradeTypeId = getIntId("gradeType")
    for (state <- gradeState.getStates if (gradeTypeId == GradeTypeConstants.GA_ID && null != state.getPercent) || 
      gradeTypeId == state.gradeType.getId) {
      state.setStatus(Grade.Status.NEW)
    }
    courseGradeService.recalculate(gradeState)
    redirect("search", "info.save.success")
  }
}
