package org.openurp.edu.eams.teach.lesson.dao


import org.openurp.edu.teach.lesson.Lesson



trait LessonSeqNoGenerator {

  def genLessonSeqNo(lesson: Lesson): Unit

  def genLessonSeqNos(tasks: Iterable[Lesson]): Unit
}
