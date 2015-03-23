package org.openurp.edu.eams.teach.lesson.service.limit.impl


Filter
Provider
import DefaultLessonLimitMetaProvider._



object DefaultLessonLimitMetaProvider {

  private val enums = LessonLimitMeta.values
}

class DefaultLessonLimitMetaProvider extends LessonLimitMetaProvider {

  private var filters: List[LessonLimitMetaFilter] = new ArrayList[LessonLimitMetaFilter]()

  def getLessonLimitMetas(): List[LessonLimitMeta] = {
    val results = new ArrayList[LessonLimitMeta]()
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

  def getLessonLimitMetaIds(): List[Long] = {
    val results = new ArrayList[Long]()
    for (courseLimitMetaEnum <- enums) {
      var append = true
      for (filter <- filters if !filter.accept(courseLimitMetaEnum)) {
        append = false
        //break
      }
      if (append) {
        results.add(courseLimitMetaEnum.id)
      }
    }
    results
  }

  def getLessonLimitMetaPairs(): Pair[List[Long], List[LessonLimitMeta]] = {
    val ids = new ArrayList[Long]()
    val courseLimitMetaEnums = new ArrayList[LessonLimitMeta]()
    for (courseLimitMetaEnum <- enums) {
      var append = true
      for (filter <- filters if !filter.accept(courseLimitMetaEnum)) {
        append = false
        //break
      }
      if (append) {
        courseLimitMetaEnums.add(courseLimitMetaEnum)
        ids.add(courseLimitMetaEnum.id)
      }
    }
    new Pair[List[Long], List[LessonLimitMeta]](ids, courseLimitMetaEnums)
  }

  def setFilters(filters: List[LessonLimitMetaFilter]) {
    this.filters = filters
  }
}
