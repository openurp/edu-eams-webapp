package org.openurp.edu.eams.teach.grade.transcript.service.impl


import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.eams.teach.grade.service.impl.GradeFilter
import org.openurp.edu.eams.teach.grade.service.impl.GradeFilterRegistry
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider
import org.openurp.edu.teach.grade.CourseGrade



class TranscriptPublishedGradeProvider extends TranscriptDataProvider {

  private var gradeFilterRegistry: GradeFilterRegistry = _

  private var courseGradeProvider: CourseGradeProvider = _

  def getDataName(): String = "grades"

  def getData[T](std: Student, options: Map[String, String]): T = {
    var grades = courseGradeProvider.getPublished(std)
    val matched = getFilters(options)
    for (filter <- matched) grades = filter.filter(grades)
    grades.asInstanceOf[T]
  }

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T] = {
    val datas = CollectUtils.newHashMap()
    val matched = getFilters(options)
    val gradeMap = courseGradeProvider.getPublished(stds)
    for (std <- stds) {
      var grades = gradeMap.get(std)
      for (filter <- matched) grades = filter.filter(grades)
      datas.put(std, grades.asInstanceOf[T])
    }
    datas
  }

  protected def getFilters(options: Map[String, String]): List[GradeFilter] = {
    if (null == options || options.isEmpty) return Collections.emptyList()
    gradeFilterRegistry.getFilters(options.get("grade.filters"))
  }

  def setGradeFilterRegistry(gradeFilterRegistry: GradeFilterRegistry) {
    this.gradeFilterRegistry = gradeFilterRegistry
  }

  def setCourseGradeProvider(courseGradeProvider: CourseGradeProvider) {
    this.courseGradeProvider = courseGradeProvider
  }
}
