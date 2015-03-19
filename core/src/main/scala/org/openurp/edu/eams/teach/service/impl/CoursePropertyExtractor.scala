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
      val extInfo = courseService.getCourseExtInfo(course.id)
      extInfo.getRequirement
    } else if ("extInfo.description" == property) {
      val extInfo = courseService.getCourseExtInfo(course.id)
      extInfo.getDescription
    } else {
      super.getPropertyValue(target, property)
    }
  }

  def setCourseService(courseService: CourseService) {
    this.courseService = courseService
  }
}
