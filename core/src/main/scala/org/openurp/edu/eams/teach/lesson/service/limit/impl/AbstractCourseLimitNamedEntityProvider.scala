package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable
import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.tuple.Pair

import scala.collection.JavaConversions._

abstract class AbstractCourseLimitNamedEntityProvider[T <: Entity[ID], ID <: Serializable]
    extends AbstractCourseLimitEntityProvider[T, ID] {

  def getContentIdTitle(entity: T): Pair[String, String] = {
    val name = PropertyUtils.getProperty(entity, "name")
    new Pair[String, String](entity.getId.toString, name)
  }
}
