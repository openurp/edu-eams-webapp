package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.teach.lesson.Lesson



class CampusFilter extends AbstractElectableLessonFilter {

  order = AbstractElectRuleExecutor.Priority.SECOND.ordinal()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    if (null == lesson.getCampus) {
      true
    } else {
      if (lesson.getCampus.id == state.getStd.getCampusId) {
        return true
      }
      false
    }
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("只开放给" + context.getLesson.getCampus.getName + "校区的同学选课", ElectRuleType.ELECTION, 
        false, context.getLesson))
    }
    result
  }
}
