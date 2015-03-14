package org.openurp.edu.eams.teach.election.filter

import java.util.List
import java.util.Map
import org.openurp.edu.teach.lesson.CourseTake

import scala.collection.JavaConversions._

trait CourseTakeFilterStrategy {

  def getToBeRemoved(amout: Int, originalTakes: List[CourseTake], params: Map[String, Any]): List[CourseTake]

  def postFilter(take: CourseTake): Unit
}
