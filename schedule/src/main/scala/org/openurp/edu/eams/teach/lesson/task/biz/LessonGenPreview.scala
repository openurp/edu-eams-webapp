package org.openurp.edu.eams.teach.lesson.task.biz



import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.plan.MajorPlan




class LessonGenPreview {

  
  var plan: MajorPlan = _

  
  var term: Int = _

  
  var lessons: List[Lesson] = new ArrayList[Lesson]()

  
  var error: String = _

  def this(plan: MajorPlan, term: Int) {
    super()
    this.plan = plan
    this.term = term
  }
}
