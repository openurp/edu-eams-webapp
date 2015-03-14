package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.helper.CourseLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum

import scala.collection.JavaConversions._

class ElectableLessonByGenderFilter extends AbstractElectableLessonFilter {

  order = AbstractElectRuleExecutor.Priority.FIFTH.ordinal()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    if (CollectUtils.isEmpty(lesson.getTeachClass.getLimitGroups)) {
      return true
    }
    CourseLimitGroupHelper.isElectable(lesson, state, CourseLimitMetaEnum.GENDER)
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    false
  }
}
