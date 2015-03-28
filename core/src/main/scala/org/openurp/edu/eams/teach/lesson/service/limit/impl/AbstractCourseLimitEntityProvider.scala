package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable
import java.util.LinkedHashMap
import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Arrays
import org.beangle.commons.entity.metadata.Model

abstract class AbstractLessonLimitEntityProvider[T <: Entity[ID], ID <: Serializable]
  extends AbstractLessonLimitContentProvider[T] {

  protected override def getContentMap(content: Array[Serializable]): collection.Map[String, T] = {
    val entities: Seq[Entity[_]] = entityDao.findBy(this.meta.contentType.getName, "id", content)
    val results = new LinkedHashMap[String, Any]
    for (entity <- entities) {
      results.put(entity.id.toString, entity)
    }
    results.asInstanceOf[collection.Map[String, T]]
  }

  def getQueryBuilder(content: Array[Serializable], term: String, limit: PageLimit): OqlBuilder[T] = {
    val queryBuilder = OqlBuilder.from[T](this.meta.contentType.getName, "entity")
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
    term: String, limit: PageLimit, cascadeField: Map[Long, String]): Seq[T] = {
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
      if (classOf[String].isAssignableFrom(Model.getType(this.meta.contentType)("name").returnedClass)) {
        sb.append("entity.name like :codeOrName ")
        hasName = true
      }
    } catch {
      case e: Exception =>
    }
    try {
      if (classOf[String].isAssignableFrom(Model.getType(this.meta.contentType)("code")
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

  def getContentIdTitleMap(content: String): collection.Map[String, String] = {
    val entities = entityDao.findBy(this.meta.contentType.getName, "id", getContentValues(content))
    val results = new scala.collection.mutable.LinkedHashMap[String, String]
    for (entity <- entities) {
      val idTitle = getContentIdTitle(entity)
      results.put(idTitle._1, idTitle._2)
    }
    results
  }

  def getContentIdTitle(entity: T): Pair[String, String]
}
