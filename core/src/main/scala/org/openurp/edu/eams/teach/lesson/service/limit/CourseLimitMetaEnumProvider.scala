package org.openurp.edu.eams.teach.lesson.service.limit


trait CourseLimitMetaEnumProvider {

  def getCourseLimitMetaEnums(): List[CourseLimitMetaEnum]

  def getCourseLimitMetaIds(): List[Long]

  def getCourseLimitMetaPairs(): Pair[List[Long], List[CourseLimitMetaEnum]]
}
