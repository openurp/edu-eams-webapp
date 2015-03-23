package org.openurp.edu.eams.teach.lesson.service.limit



import org.beangle.commons.collection.page.PageLimit



trait LessonLimitItemContentProvider[T] {

  def getContents(content: String): Map[String, T]

  def getContentIdTitleMap(content: String): Map[String, String]

  def getOtherContents(content: String, term: String, limit: PageLimit): List[T]

  def getCascadeContents(content: String, 
      term: String, 
      limit: PageLimit, 
      cascadeField: Map[Long, String]): List[T]

  def getMetaEnum(): LessonLimitMeta

  def setMetaEnum(metaEnum: LessonLimitMeta): Unit
}
