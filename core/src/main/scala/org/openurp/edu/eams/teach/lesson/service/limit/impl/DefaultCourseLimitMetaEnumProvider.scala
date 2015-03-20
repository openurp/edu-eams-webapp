package org.openurp.edu.eams.teach.lesson.service.limit.impl



import org.beangle.commons.lang.tuple.Pair
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnumFilter
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnumProvider
import DefaultCourseLimitMetaEnumProvider._



object DefaultCourseLimitMetaEnumProvider {

  private val enums = CourseLimitMetaEnum.values
}

class DefaultCourseLimitMetaEnumProvider extends CourseLimitMetaEnumProvider {

  private var filters: List[CourseLimitMetaEnumFilter] = new ArrayList[CourseLimitMetaEnumFilter]()

  def getCourseLimitMetaEnums(): List[CourseLimitMetaEnum] = {
    val results = new ArrayList[CourseLimitMetaEnum]()
    for (courseLimitMetaEnum <- enums) {
      var append = true
      for (filter <- filters if !filter.accept(courseLimitMetaEnum)) {
        append = false
        //break
      }
      if (append) {
        results.add(courseLimitMetaEnum)
      }
    }
    results
  }

  def getCourseLimitMetaIds(): List[Long] = {
    val results = new ArrayList[Long]()
    for (courseLimitMetaEnum <- enums) {
      var append = true
      for (filter <- filters if !filter.accept(courseLimitMetaEnum)) {
        append = false
        //break
      }
      if (append) {
        results.add(courseLimitMetaEnum.metaId)
      }
    }
    results
  }

  def getCourseLimitMetaPairs(): Pair[List[Long], List[CourseLimitMetaEnum]] = {
    val ids = new ArrayList[Long]()
    val courseLimitMetaEnums = new ArrayList[CourseLimitMetaEnum]()
    for (courseLimitMetaEnum <- enums) {
      var append = true
      for (filter <- filters if !filter.accept(courseLimitMetaEnum)) {
        append = false
        //break
      }
      if (append) {
        courseLimitMetaEnums.add(courseLimitMetaEnum)
        ids.add(courseLimitMetaEnum.metaId)
      }
    }
    new Pair[List[Long], List[CourseLimitMetaEnum]](ids, courseLimitMetaEnums)
  }

  def setFilters(filters: List[CourseLimitMetaEnumFilter]) {
    this.filters = filters
  }
}
