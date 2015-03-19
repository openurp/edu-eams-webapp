package org.openurp.edu.eams.teach.lesson.task.service


import org.openurp.base.Semester
import org.openurp.edu.eams.teach.lesson.task.biz.LessonGenPreview



trait TeachTaskGenService {

  def previewLessonGen(planIds: Array[Long], params: TaskGenParams): List[LessonGenPreview]

  def genLessons(planIds: Array[Long], observer: TaskGenObserver, params: TaskGenParams): Unit
}
