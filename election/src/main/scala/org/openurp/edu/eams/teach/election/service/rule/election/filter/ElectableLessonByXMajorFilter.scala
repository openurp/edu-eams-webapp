package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.openurp.edu.eams.core.model.MajorBean
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class ElectableLessonByXMajorFilter extends AbstractElectableLessonFilter {

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    lesson.getCourse.getXmajors.isEmpty || 
      !lesson.getCourse.getXmajors.contains(new MajorBean(state.getStd.majorId))
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("不开放给" + context.getStudent.major.getName + "专业的学生", ElectRuleType.ELECTION, 
        false, context.getLesson))
    }
    result
  }
}
