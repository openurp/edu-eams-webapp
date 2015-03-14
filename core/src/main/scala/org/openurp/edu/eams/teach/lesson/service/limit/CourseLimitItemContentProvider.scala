package org.openurp.edu.eams.teach.lesson.service.limit

import java.util.List
import java.util.Map
import org.beangle.commons.collection.page.PageLimit

import scala.collection.JavaConversions._

trait CourseLimitItemContentProvider[T] {

  def getContents(content: String): Map[String, T]

  def getContentIdTitleMap(content: String): Map[String, String]

  def getOtherContents(content: String, term: String, limit: PageLimit): List[T]

  def getCascadeContents(content: String, 
      term: String, 
      limit: PageLimit, 
      cascadeField: Map[Long, String]): List[T]

  def getMetaEnum(): CourseLimitMetaEnum

  def setMetaEnum(metaEnum: CourseLimitMetaEnum): Unit
}
