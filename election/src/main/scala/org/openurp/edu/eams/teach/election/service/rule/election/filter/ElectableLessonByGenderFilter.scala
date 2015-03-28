package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.beangle.commons.collection.Collections
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.helper.LessonLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaEnum



class ElectableLessonByGenderFilter extends AbstractElectableLessonFilter {

  order = AbstractElectRuleExecutor.Priority.FIFTH.ordinal()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    if (Collections.isEmpty(lesson.getTeachClass.getLimitGroups)) {
      return true
    }
    LessonLimitGroupHelper.isElectable(lesson, state, LessonLimitMeta.Gender)
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    false
  }
}
