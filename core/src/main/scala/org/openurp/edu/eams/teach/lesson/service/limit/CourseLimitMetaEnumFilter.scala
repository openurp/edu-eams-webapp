package org.openurp.edu.eams.teach.lesson.service.limit

trait LessonLimitMetaFilter {

  def accept(courseLimitMetaEnum: LessonLimitMeta): Boolean
}
