package org.openurp.edu.eams.teach.lesson.task.biz

import java.util.ArrayList
import java.util.List
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.plan.MajorPlan
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class LessonGenPreview {

  @BeanProperty
  var plan: MajorPlan = _

  @BeanProperty
  var term: Int = _

  @BeanProperty
  var lessons: List[Lesson] = new ArrayList[Lesson]()

  @BeanProperty
  var error: String = _

  def this(plan: MajorPlan, term: Int) {
    super()
    this.plan = plan
    this.term = term
  }
}
