package org.openurp.edu.eams.teach.lesson.task.service

import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.splitter.AbstractTeachClassSplitter

import scala.collection.JavaConversions._

trait LessonMergeSplitService {

  def merge(taskIds: Array[java.lang.Long]): Lesson

  def merge(taskIds: Array[java.lang.Long], reservedId: java.lang.Long): Lesson

  def split(task: Lesson, 
      num: Int, 
      mode: AbstractTeachClassSplitter, 
      splitUnitNums: Array[Integer]): Array[Lesson]
}
