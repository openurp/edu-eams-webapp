package org.openurp.edu.eams.teach.lesson.task.biz


import java.util.TreeSet
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.biz.comparator.LessonComparator
import scala.collection.mutable.HashSet




class CourseTypePackage {

  
  var courseType: CourseType = _

  
  var lessons = new HashSet[Lesson]

  def getCredits(): Float = {
    var credits = 0f
    for (lesson <- lessons) {
      credits += lesson.course.credits
    }
    credits
  }
}
