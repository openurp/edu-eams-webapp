package org.openurp.edu.eams.teach.lesson.task.service

import java.util.List
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.teach.lesson.task.biz.LessonGenPreview

import scala.collection.JavaConversions._

trait TeachTaskGenService {

  def previewLessonGen(planIds: Array[Long], params: TaskGenParams): List[LessonGenPreview]

  def genLessons(planIds: Array[Long], observer: TaskGenObserver, params: TaskGenParams): Unit
}
