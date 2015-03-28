package org.openurp.edu.eams.teach.lesson.service.limit

import org.beangle.commons.collection.page.PageLimit
import org.openurp.edu.teach.lesson.LessonLimitMeta.LimitMeta

trait LessonLimitItemContentProvider[T] {

  def getContents(content: String): collection.Map[String, T]

  def getContentIdTitleMap(content: String): collection.Map[String, String]

  def getOtherContents(content: String, term: String, limit: PageLimit): List[T]

  def getCascadeContents(content: String,
    term: String,
    limit: PageLimit,
    cascadeField: Map[Long, String]): List[T]

  def meta: LimitMeta

  def meta_=(metaEnum: LimitMeta): Unit
}

trait LessonLimitItemContentProviderFactory {

  def getProvider(lessonLimitMeta: LimitMeta): LessonLimitItemContentProvider[_]

  def getProvider(lessonLimitMetaId: Int): LessonLimitItemContentProvider[_]
}
