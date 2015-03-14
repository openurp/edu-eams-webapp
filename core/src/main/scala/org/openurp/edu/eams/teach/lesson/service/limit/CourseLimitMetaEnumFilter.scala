package org.openurp.edu.eams.teach.lesson.service.limit


import scala.collection.JavaConversions._

trait CourseLimitMetaEnumFilter {

  def accept(courseLimitMetaEnum: CourseLimitMetaEnum): Boolean
}
