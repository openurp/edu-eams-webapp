package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable
import org.beangle.commons.bean.PropertyUtils
import org.beangle.data.model.Entity




abstract class AbstractLessonLimitNamedEntityProvider[T <: Entity[ID], ID <: Serializable]
    extends AbstractLessonLimitEntityProvider[T, ID] {

  def getContentIdTitle(entity: T): Pair[String, String] = {
    val name = PropertyUtils.getProperty(entity, "name")
    new Pair[String, String](entity.id.toString, name)
  }
}
