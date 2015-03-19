package org.openurp.edu.eams.teach.grade.transcript.service.impl


import org.beangle.commons.collection.CollectUtils
import org.springframework.beans.factory.InitializingBean
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.eams.teach.grade.service.impl.DefaultGpaStatService
import org.openurp.edu.eams.teach.grade.service.impl.GpaPolicy
import org.openurp.edu.eams.teach.grade.service.impl.GradeFilter
import org.openurp.edu.eams.teach.grade.service.impl.GradeFilterRegistry
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider
import org.openurp.edu.teach.grade.CourseGrade



class TranscriptGpaProvider extends TranscriptDataProvider with InitializingBean {

  private var courseGradeProvider: CourseGradeProvider = _

  private var gpaPolicy: GpaPolicy = _

  private var gradeFilterRegistry: GradeFilterRegistry = _

  private var gpaStatService: DefaultGpaStatService = new DefaultGpaStatService()

  def afterPropertiesSet() {
    gpaStatService.setGpaPolicy(gpaPolicy)
    gpaStatService.setCourseGradeProvider(courseGradeProvider)
  }

  def getDataName(): String = "gpas"

  def getData[T](std: Student, options: Map[String, String]): T = {
    var grades = courseGradeProvider.getPublished(std)
    val matched = getFilters(options)
    for (filter <- matched) grades = filter.filter(grades)
    gpaStatService.statGpa(std, grades).asInstanceOf[T]
  }

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T] = {
    val matched = getFilters(options)
    val datas = CollectUtils.newHashMap()
    val gradeMap = courseGradeProvider.getPublished(stds)
    for (std <- stds) {
      var grades = gradeMap.get(std)
      for (filter <- matched) grades = filter.filter(grades)
      datas.put(std, gpaStatService.statGpa(std, grades).asInstanceOf[T])
    }
    datas
  }

  def setCourseGradeProvider(courseGradeProvider: CourseGradeProvider) {
    this.courseGradeProvider = courseGradeProvider
  }

  def setGradeFilterRegistry(gradeFilterRegistry: GradeFilterRegistry) {
    this.gradeFilterRegistry = gradeFilterRegistry
  }

  protected def getFilters(options: Map[String, String]): List[GradeFilter] = {
    if (null == options || options.isEmpty) return Collections.emptyList()
    gradeFilterRegistry.getFilters(options.get("gpa.filters"))
  }

  def setGpaPolicy(gpaPolicy: GpaPolicy) {
    this.gpaPolicy = gpaPolicy
  }
}
