package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.eams.teach.election.service.rule.election.CourseGradePrepare
import org.openurp.edu.teach.lesson.Lesson
import ElectableLessonNoRetakeFilter._



object ElectableLessonNoRetakeFilter {

  val PARAM = "NORETAKE"
}

class ElectableLessonNoRetakeFilter extends AbstractElectableLessonFilter with ElectRulePrepare {

  protected var courseGradePrepare: CourseGradePrepare = _

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    !state.isRetakeCourse(lesson.getCourse.id)
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("本轮次不开放重修课", ElectRuleType.ELECTION, false, context.getLesson))
    }
    result
  }

  def prepare(electContext: PrepareContext) {
    electContext.getState.getParams.put(PARAM, true)
    courseGradePrepare.prepare(electContext)
  }

  def setCourseGradePrepare(courseGradePrepare: CourseGradePrepare) {
    this.courseGradePrepare = courseGradePrepare
  }
}
