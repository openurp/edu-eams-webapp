package org.openurp.edu.eams.teach.grade.service.impl

import java.util.Collections
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.plan.CourseSubstitution
import org.openurp.edu.eams.teach.program.service.CourseSubstitutionService

import scala.collection.JavaConversions._

class BestGradeFilter extends GradeFilter {

  private var courseSubstitutionService: CourseSubstitutionService = _

  protected def buildGradeMap(grades: List[CourseGrade]): Map[Course, CourseGrade] = {
    val gradesMap = CollectUtils.newHashMap()
    var old: CourseGrade = null
    for (grade <- grades) {
      old = gradesMap.get(grade.getCourse)
      if (GradeComparator.betterThan(grade, old)) gradesMap.put(grade.getCourse, grade)
    }
    gradesMap
  }

  def filter(grades: List[CourseGrade]): List[CourseGrade] = {
    val gradesMap = buildGradeMap(grades)
    val substituteCourses = getSubstituteCourses(grades)
    for (subCourse <- substituteCourses if GradeComparator.isSubstitute(subCourse, gradesMap); c <- subCourse.getOrigins) gradesMap.remove(c)
    CollectUtils.newArrayList(gradesMap.values)
  }

  private def getSubstituteCourses(grades: List[CourseGrade]): List[CourseSubstitution] = {
    if (grades.isEmpty) {
      return Collections.emptyList()
    }
    val grade = grades.get(0)
    courseSubstitutionService.getCourseSubstitutions(grade.getStd)
  }

  def setCourseSubstitutionService(courseSubstitutionService: CourseSubstitutionService) {
    this.courseSubstitutionService = courseSubstitutionService
  }
}
