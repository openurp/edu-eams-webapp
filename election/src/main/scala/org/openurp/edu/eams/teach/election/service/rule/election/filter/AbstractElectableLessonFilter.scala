package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.beangle.ems.rule.Context
import org.openurp.edu.eams.teach.election.service.ElectableLessonFilter
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor



abstract class AbstractElectableLessonFilter extends AbstractElectRuleExecutor with ElectableLessonFilter {

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    onExecuteRuleReturn(isElectable(electContext.getLesson, electContext.getState), electContext)
  }

  protected def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean
}
