package org.openurp.edu.eams.teach.lesson.service.limit


trait LessonLimitMetaProvider {

  def getLessonLimitMetas(): List[LessonLimitMeta]

  def getLessonLimitMetaIds(): List[Long]

  def getLessonLimitMetaPairs(): Pair[List[Long], List[LessonLimitMeta]]
}
