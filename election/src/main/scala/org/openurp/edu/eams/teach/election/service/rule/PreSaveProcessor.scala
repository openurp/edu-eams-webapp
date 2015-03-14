package org.openurp.edu.eams.teach.election.service.rule

import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext

import scala.collection.JavaConversions._

trait PreSaveProcessor {

  def process(context: ElectionCourseContext): Unit
}
