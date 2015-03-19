package org.openurp.edu.eams.teach.election.service

import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.teach.lesson.Lesson



trait ElectableLessonFilter extends Comparable[AbstractElectRuleExecutor] {

  def isElectable(lesson: Lesson, state: ElectState): Boolean

  def getOrder(): Int
}
