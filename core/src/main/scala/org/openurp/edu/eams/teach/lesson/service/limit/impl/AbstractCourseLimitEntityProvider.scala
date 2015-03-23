package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable
import java.util.LinkedHashMap

import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Arrays

abstract class AbstractLessonLimitEntityProvider[T <: Entity[ID], ID <: Serializable]
    extends AbstractLessonLimitContentProvider[T] {

  protected override def getContentMap(content: Array[Serializable]): Map[String, T] = {
    val entities = entityDao.get(getMetaEnum.contentType.name, "id", content)
    val results = new LinkedHashMap[String, T]()
    for (entity <- entities) {
      results.put(entity.id.toString, entity)
    }
    results
  }

  def getQueryBuilder(content: Array[Serializable], term: String, limit: PageLimit): OqlBuilder[T] = {
    val queryBuilder = OqlBuilder.from(getMetaEnum.contentType.name, "entity")
    if (!Arrays.isEmpty(content)) {
      queryBuilder.where("entity.id not in(:ids)", content)
    }
    if (null != term) {
      addTermCondition(queryBuilder, term)
    }
    queryBuilder.orderBy("id")
    queryBuilder.limit(limit)
    queryBuilder
  }

  protected override def getCascadeContents(content: Array[Serializable], 
      term: String,       limit: PageLimit,       cascadeField: Map[Long, String]): Seq[T] = {
    val builder = getQueryBuilder(content, term, limit)
    addCascadeQuery(builder, cascadeField)
    entityDao.search(builder)
  }

  protected def addCascadeQuery(builder: OqlBuilder[T], cascadeField: Map[Long, String]) {
  }

  protected override def getOtherContents(content: Array[Serializable], term: String, limit: PageLimit): Seq[T] = {
    entityDao.search(getQueryBuilder(content, term, limit))
  }

  protected def addTermCondition(queryBuilder: OqlBuilder[T], term: String) {
    val sb = new StringBuilder()
    var hasName = false
    try {
      if (classOf[String].isAssignableFrom(Model.getType(getMetaEnum.contentType).propertyType("name")
        .returnedClass)) {
        sb.append("entity.name like :codeOrName ")
        hasName = true
      }
    } catch {
      case e: Exception => 
    }
    try {
      if (classOf[String].isAssignableFrom(Model.getType(getMetaEnum.contentType).propertyType("code")
        .returnedClass)) {
        if (hasName) {
          sb.append("or ")
        }
        sb.append("entity.code like :codeOrName ")
      }
    } catch {
      case e: Exception => 
    }
    if (sb.length > 0) {
      queryBuilder.where(sb.toString, "%" + term + "%")
    }
  }

  def getContentIdTitleMap(content: String): Map[String, String] = {
    val entities = entityDao.get(getMetaEnum.contentType.name, "id", getContentValues(content))
    val results = new LinkedHashMap[String, String]()
    for (entity <- entities) {
      val idTitle = getContentIdTitle(entity)
      results.put(idTitle.left, idTitle.right)
    }
    results
  }

  def getContentIdTitle(entity: T): Pair[String, String]
}
