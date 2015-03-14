package org.openurp.edu.eams.teach.lesson.task.biz

import java.util.Set
import java.util.TreeSet
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.biz.comparator.LessonComparator
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class CourseTypePackage {

  @BeanProperty
  var courseType: CourseType = _

  @BeanProperty
  var lessons: Set[Lesson] = new TreeSet[Lesson](LessonComparator.COMPARATOR)

  def getCredits(): Float = {
    var credits = 0f
    for (lesson <- lessons) {
      credits += lesson.getCourse.getCredits
    }
    credits
  }
}
