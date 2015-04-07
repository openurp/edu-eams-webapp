package org.openurp.edu.eams.teach.lesson.task.biz.comparator

import java.util.Comparator
import org.beangle.commons.lang.Objects
import org.openurp.edu.teach.lesson.Lesson
import LessonComparator._



object LessonComparator {

  val COMPARATOR = new LessonComparator()
}

class LessonComparator private () extends Comparator[Lesson]() {

  def compare(o1: Lesson, o2: Lesson): Int = {
    Objects.compareBuilder.add(o1.courseType.code, o2.courseType.code)
      .add(o1.course.code, o2.course.code)
      .add(o1.no, o2.no)
      .add(o1.id, o2.id)
      .toComparison()
  }
}
