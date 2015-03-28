package org.openurp.edu.eams.teach.election.filter



import org.beangle.commons.collection.Collections
import org.openurp.edu.teach.lesson.CourseTake



class DefaultCourseTakeFilterStrategy extends CourseTakeFilterStrategy {

  def getToBeRemoved(amount: Int, takes: List[CourseTake], params: Map[String, Any]): List[CourseTake] = {
    if (amount <= 0) {
      return Collections.newBuffer[Any]
    }
    val toBeRemoved = Collections.newBuffer[Any]
    while (amount > 0 && takes.size > 0) {
      val i = (Math.random() * takes.size).toInt
      toBeRemoved.add(takes.get(i))
      takes.remove(i)
      amount -= 1
    }
    toBeRemoved
  }

  def postFilter(take: CourseTake) {
  }
}
