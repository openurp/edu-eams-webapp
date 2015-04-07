package org.openurp.edu.eams.teach.lesson.task.biz



import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.plan.MajorPlan
import org.beangle.commons.collection.Collections




class LessonGenPreview {

  
  var plan: MajorPlan = _

  
  var term: Int = _

  
  var lessons = Collections.newBuffer[Lesson]

  
  var error: String = _

  def this(plan: MajorPlan, term: Int) {
    this()
    this.plan = plan
    this.term = term
  }
}
