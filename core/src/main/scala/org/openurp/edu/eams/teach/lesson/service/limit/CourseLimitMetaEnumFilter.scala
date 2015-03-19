package org.openurp.edu.eams.teach.lesson.service.limit




trait CourseLimitMetaEnumFilter {

  def accept(courseLimitMetaEnum: CourseLimitMetaEnum): Boolean
}
