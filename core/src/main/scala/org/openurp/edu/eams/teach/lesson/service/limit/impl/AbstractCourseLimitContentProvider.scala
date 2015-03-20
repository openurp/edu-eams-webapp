package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable


import org.beangle.commons.collection.MapConverter
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import AbstractCourseLimitContentProvider._




object AbstractCourseLimitContentProvider {

  val converter = new MapConverter(DefaultConversion.Instance)
}

abstract class AbstractCourseLimitContentProvider[T] extends BaseServiceImpl with CourseLimitItemContentProvider[T] {

  
  var metaEnum: CourseLimitMetaEnum = _

  protected def getContentValues(content: String): Array[Serializable] = {
    val strValues = Strings.split(content, ",")
    if (Arrays.isEmpty(strValues)) {
      return null
    }
    val values = converter.convert(strValues, metaEnum.contentValueType)
    if (Arrays.isEmpty(values)) {
      return null
    }
    values
  }

  def getCascadeContents(content: String, 
      term: String, 
      limit: PageLimit, 
      cascadeField: Map[Long, String]): List[T] = {
    getCascadeContents(getContentValues(content), term, limit, cascadeField)
  }

  protected def getCascadeContents(content: Array[Serializable], 
      term: String, 
      limit: PageLimit, 
      cascadeField: Map[Long, String]): List[T]

  def getContents(content: String): Map[String, T] = {
    getContentMap(getContentValues(content))
  }

  def getOtherContents(content: String, term: String, limit: PageLimit): List[T] = {
    getOtherContents(getContentValues(content), term, limit)
  }

  protected def getOtherContents(content: Array[Serializable], term: String, limit: PageLimit): List[T]

  protected def getContentMap(content: Array[Serializable]): Map[String, T]
}
