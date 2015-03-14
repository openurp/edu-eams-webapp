package org.openurp.edu.eams.teach.lesson.dao

import java.util.Collection
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait LessonSeqNoGenerator {

  def genLessonSeqNo(lesson: Lesson): Unit

  def genLessonSeqNos(tasks: Collection[Lesson]): Unit
}
