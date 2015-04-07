package org.openurp.edu.eams.teach.lesson.task.util



import org.beangle.commons.text.i18n.TextResource
import org.openurp.edu.teach.lesson.Lesson
import org.beangle.commons.collection.Collections



class TaskPerClassPropertyExtractor(textResource: TextResource) extends TeachTaskPropertyExtractor(textResource) {

  var courseAndClassMap: collection.mutable.Map[_,_] = Collections.newMap[Any,Any]

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    val lesson = target.asInstanceOf[Lesson]
    if ("teachClass.adminClasses" == property) {
      null
    } else super.getPropertyValue(target, property)
  }
}
