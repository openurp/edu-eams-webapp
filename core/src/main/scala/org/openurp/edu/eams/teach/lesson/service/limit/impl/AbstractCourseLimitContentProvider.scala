package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable
import org.beangle.commons.collection.MapConverter
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitItemContentProvider
import AbstractLessonLimitContentProvider._
import org.openurp.edu.teach.lesson.LessonLimitMeta.LimitMeta

object AbstractLessonLimitContentProvider {

  val converter = new MapConverter(DefaultConversion.Instance)
}

abstract class AbstractLessonLimitContentProvider[T] extends BaseServiceImpl with LessonLimitItemContentProvider[T] {

  var meta: LimitMeta = _

  protected def getContentValues(content: String): Array[java.io.Serializable] = {
    val strValues = Strings.split(content, ",")
    if (Arrays.isEmpty(strValues)) {
      return null
    }
    converter.convert(strValues, meta.contentValueType) match {
      case Some(results) => {
        val r = results.asInstanceOf[Array[java.io.Serializable]]
        if (Arrays.isEmpty(r)) null else r
      }
      case None => null
    }
  }

  def getCascadeContents(content: String,
    term: String,
    limit: PageLimit,
    cascadeField: collection.Map[java.lang.Long, String]): Seq[T] = {
    getCascadeContents(getContentValues(content), term, limit, cascadeField)
  }

  protected def getCascadeContents(content: Array[Serializable],
    term: String,
    limit: PageLimit,
    cascadeField: collection.Map[java.lang.Long, String]): Seq[T]

  def getContents(content: String): collection.Map[String, T] = {
    getContentMap(getContentValues(content))
  }

  def getOtherContents(content: String, term: String, limit: PageLimit): Seq[T] = {
    getOtherContents(getContentValues(content), term, limit)
  }

  protected def getOtherContents(content: Array[Serializable], term: String, limit: PageLimit): Seq[T]

  protected def getContentMap(content: Array[Serializable]): collection.Map[String, T]
}
