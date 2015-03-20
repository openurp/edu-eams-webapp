package org.openurp.edu.eams.teach.service.impl

import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.transfer.exporter.DefaultPropertyExtractor
import org.openurp.edu.base.Course
import org.openurp.edu.base.CourseExtInfo
import org.openurp.edu.eams.teach.service.CourseService



class CoursePropertyExtractor(resource: TextResource) extends DefaultPropertyExtractor(resource) {

  protected var courseService: CourseService = _

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    val course = target.asInstanceOf[Course]
    if ("extInfo.requirement" == property) {
      val extInfo = courseService.courseExtInfo(course.id)
      extInfo.requirement
    } else if ("extInfo.description" == property) {
      val extInfo = courseService.courseExtInfo(course.id)
      extInfo.description
    } else {
      super.propertyValue(target, property)
    }
  }

  def setCourseService(courseService: CourseService) {
    this.courseService = courseService
  }
}
