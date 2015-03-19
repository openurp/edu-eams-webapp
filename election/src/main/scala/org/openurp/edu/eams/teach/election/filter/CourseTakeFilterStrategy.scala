package org.openurp.edu.eams.teach.election.filter



import org.openurp.edu.teach.lesson.CourseTake



trait CourseTakeFilterStrategy {

  def getToBeRemoved(amout: Int, originalTakes: List[CourseTake], params: Map[String, Any]): List[CourseTake]

  def postFilter(take: CourseTake): Unit
}
