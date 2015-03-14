package org.openurp.edu.eams.teach.lesson.task.biz.comparator

import java.util.Comparator
import org.beangle.commons.lang.Objects
import org.openurp.edu.teach.lesson.Lesson
import LessonComparator._

import scala.collection.JavaConversions._

object LessonComparator {

  val COMPARATOR = new LessonComparator()
}

class LessonComparator private () extends Comparator[Lesson]() {

  def compare(o1: Lesson, o2: Lesson): Int = {
    Objects.compareBuilder().add(o1.getCourseType.getCode, o2.getCourseType.getCode)
      .add(o1.getCourse.getCode, o2.getCourse.getCode)
      .add(o1.getNo, o2.getNo)
      .add(o1.getId, o2.getId)
      .toComparison()
  }
}
