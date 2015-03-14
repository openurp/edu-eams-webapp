package org.openurp.edu.eams.teach.grade.service.impl

import java.util.Collections
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.model.CourseGradeBean
import org.openurp.edu.teach.plan.CourseSubstitution
import org.openurp.edu.eams.teach.program.service.CourseSubstitutionService

import scala.collection.JavaConversions._

class BestOriginGradeFilter extends GradeFilter {

  private var courseSubstitutionService: CourseSubstitutionService = _

  private def buildGradeMap(grades: List[CourseGrade]): Map[Course, CourseGrade] = {
    val gradesMap = CollectUtils.newHashMap()
    var old: CourseGrade = null
    for (grade <- grades) {
      old = gradesMap.get(grade.getCourse)
      if (GradeComparator.betterThan(grade, old)) {
        if (null != old) {
          var cloned = grade
          if (grade.getSemester.after(old.getSemester)) {
            cloned = clone(grade)
            cloned.setSemester(old.getSemester)
          }
          gradesMap.put(grade.getCourse, cloned)
        } else {
          gradesMap.put(grade.getCourse, grade)
        }
      }
    }
    gradesMap
  }

  private def clone(grade: CourseGrade): CourseGrade = {
    val cloned = new CourseGradeBean()
    cloned.setStd(grade.getStd)
    cloned.setCourse(grade.getCourse)
    cloned.setSemester(grade.getSemester)
    cloned.setLesson(grade.getLesson)
    cloned.setLessonNo(grade.getLessonNo)
    cloned.setCourseType(grade.getCourseType)
    cloned.setCourseTakeType(grade.getCourseTakeType)
    cloned.setExamMode(grade.getExamMode)
    cloned.setMarkStyle(grade.getMarkStyle)
    cloned.setProject(grade.getProject)
    cloned.setGp(grade.getGp)
    cloned.setPassed(grade.isPassed)
    cloned.setScore(grade.getScore)
    cloned.setScoreText(grade.getScoreText)
    cloned.setStatus(grade.getStatus)
    cloned.setCreatedAt(grade.getCreatedAt)
    cloned.getExchanges.addAll(grade.getExchanges)
    cloned.getExamGrades.addAll(grade.getExamGrades)
    cloned
  }

  def filter(grades: List[CourseGrade]): List[CourseGrade] = {
    val gradesMap = buildGradeMap(grades)
    val substituteCourses = getSubstituteCourses(grades)
    for (subCourse <- substituteCourses) {
      val origin = gradesMap.get(subCourse.getOrigins.iterator().next())
      val sub = gradesMap.get(subCourse.getSubstitutes.iterator().next())
      if (null == origin || null == sub) //continue
      if (GradeComparator.betterThan(sub, origin)) {
        gradesMap.remove(sub.getCourse.getId)
        val subClone = clone(sub)
        subClone.setSemester(origin.getSemester)
        subClone.setCourse(origin.getCourse)
        gradesMap.put(origin.getCourse, subClone)
      }
    }
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
