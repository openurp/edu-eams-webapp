package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.eams.teach.election.service.rule.election.CourseGradePrepare
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class ElectableLessonOnlyRetakeFilter extends AbstractElectableLessonFilter with ElectRulePrepare {

  protected var courseGradePrepare: CourseGradePrepare = _

  order = AbstractElectRuleExecutor.Priority.FIRST.ordinal()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    state.isRetakeCourse(lesson.getCourse.getId)
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("只能选择重修课", ElectRuleType.ELECTION, false, context.getLesson))
    }
    result
  }

  def prepare(context: PrepareContext) {
    courseGradePrepare.prepare(context)
  }

  def setCourseGradePrepare(courseGradePrepare: CourseGradePrepare) {
    this.courseGradePrepare = courseGradePrepare
  }
}
