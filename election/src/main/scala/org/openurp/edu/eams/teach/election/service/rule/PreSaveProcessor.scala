package org.openurp.edu.eams.teach.election.service.rule

import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext



trait PreSaveProcessor {

  def process(context: ElectionCourseContext): Unit
}
