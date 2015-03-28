package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.beangle.commons.collection.Collections
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.helper.LessonLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.teach.lesson.Lesson



class ElectableLessonByTeachClassFilter extends AbstractElectableLessonFilter() {

  order = AbstractElectRuleExecutor.Priority.FIFTH.ordinal()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    if (retakeService.isRetakeCourse(state, lesson.getCourse.id) && 
      !retakeService.isCheckTeachClass(state.getProfile(entityDao).getElectConfigs)) {
      return true
    }
    if (Collections.isEmpty(lesson.getTeachClass.getLimitGroups)) {
      return true
    }
    LessonLimitGroupHelper.isElectable(lesson, state)
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("只开放给:" + context.getLesson.getTeachClass.getName + "的学生", 
        ElectRuleType.ELECTION, false, context.getLesson))
    }
    result
  }
}
