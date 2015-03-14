package org.openurp.edu.eams.teach.lesson.service.limit

import java.util.List
import org.beangle.commons.lang.tuple.Pair

import scala.collection.JavaConversions._

trait CourseLimitMetaEnumProvider {

  def getCourseLimitMetaEnums(): List[CourseLimitMetaEnum]

  def getCourseLimitMetaIds(): List[Long]

  def getCourseLimitMetaPairs(): Pair[List[Long], List[CourseLimitMetaEnum]]
}
