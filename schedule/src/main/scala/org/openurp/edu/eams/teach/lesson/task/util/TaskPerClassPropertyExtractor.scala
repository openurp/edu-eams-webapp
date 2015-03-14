package org.openurp.edu.eams.teach.lesson.task.util

import java.util.HashMap
import java.util.Map
import org.beangle.commons.text.i18n.TextResource
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class TaskPerClassPropertyExtractor(textResource: TextResource) extends TeachTaskPropertyExtractor(textResource) {

  var courseAndClassMap: Map[_,_] = new HashMap()

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    val lesson = target.asInstanceOf[Lesson]
    if ("teachClass.adminClasses" == property) {
      null
    } else super.getPropertyValue(target, property)
  }
}
