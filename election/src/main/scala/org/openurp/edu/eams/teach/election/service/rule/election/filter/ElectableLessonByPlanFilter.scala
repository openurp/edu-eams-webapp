package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectCourseGroup
import org.openurp.edu.eams.teach.election.service.context.ElectCoursePlan
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.teach.lesson.Lesson



class ElectableLessonByPlanFilter extends AbstractElectableLessonFilter {

  order = AbstractElectRuleExecutor.Priority.FOURTH.ordinal()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    val coursePlan = state.getCoursePlan
    if (null == coursePlan) {
      return false
    }
    val courseTypeId = coursePlan.getCourseIds.get(lesson.getCourse.id)
    var inplan = false
    var group: ElectCourseGroup = null
    group = if (null != courseTypeId) coursePlan.groups.get(courseTypeId) else coursePlan.groups.get(lesson.getCourseType.id)
    inplan = if (null != courseTypeId) true else (null != group && group.getCourses.isEmpty)
    if (inplan && null != group && !group.isHasLesson) {
      group.setHasLesson(true)
    }
    inplan
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("不在培养计划范围内", ElectRuleType.ELECTION, false, context.getLesson))
    }
    result
  }
}
