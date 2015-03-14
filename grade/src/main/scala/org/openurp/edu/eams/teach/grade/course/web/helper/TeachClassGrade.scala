package org.openurp.edu.eams.teach.grade.course.web.helper

import java.util.Collections
import java.util.List
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import TeachClassGrade._

import scala.collection.JavaConversions._

object TeachClassGrade {

  def buildTaskClassGrade(gradeTypes: List[GradeType], 
      lesson: Lesson, 
      courseGrades: List[CourseGrade], 
      courseGradeState: CourseGradeState, 
      stdPerClass: java.lang.Integer): List[TeachClassGrade] = {
    val classGrades = CollectUtils.newArrayList()
    val teachClassGrade = new TeachClassGrade(gradeTypes, lesson, courseGrades, courseGradeState)
    var begin = 0
    while (teachClassGrade.getCourseGrades.size - begin > stdPerClass.intValue()) {
      val other = new TeachClassGrade()
      other.setLesson(teachClassGrade.getLesson)
      other.setGradeTypes(teachClassGrade.gradeTypes)
      var end = begin + stdPerClass.intValue()
      if (teachClassGrade.getCourseGrades.size < begin + stdPerClass.intValue()) {
        end = teachClassGrade.getCourseGrades.size
      }
      other.setCourseGrades(CollectUtils.newArrayList(teachClassGrade.getCourseGrades.subList(begin, 
        end)))
      other.setBeginIndex(begin)
      classGrades.add(other)
      begin += stdPerClass.intValue()
    }
    for (element <- classGrades) {
      teachClassGrade.getCourseGrades.removeAll(element.getCourseGrades)
    }
    teachClassGrade.setBeginIndex(begin)
    classGrades.add(teachClassGrade)
    classGrades
  }
}

class TeachClassGrade {

  var gradeTypes: List[GradeType] = _

  var lesson: Lesson = _

  var courseGrades: List[CourseGrade] = _

  var courseGradeState: CourseGradeState = _

  var username: String = _

  var beginIndex: Int = 0

  def getUsername(): String = username

  def setUsername(username: String) {
    this.username = username
  }

  def this(gradeTypes: List[GradeType], 
      lesson: Lesson, 
      courseGrades: List[CourseGrade], 
      courseGradeState: CourseGradeState, 
      username: String) {
    this(gradeTypes, lesson, courseGrades, courseGradeState)
    this.username = username
  }

  def this(gradeTypes: List[GradeType], 
      lesson: Lesson, 
      courseGrades: List[CourseGrade], 
      courseGradeState: CourseGradeState) {
    this()
    this.courseGradeState = courseGradeState
    this.gradeTypes = CollectUtils.newArrayList()
    val noPercentGradeTypes = CollectUtils.newArrayList()
    noPercentGradeTypes.add(GradeTypeConstants.FINAL_ID)
    noPercentGradeTypes.add(GradeTypeConstants.GA_ID)
    noPercentGradeTypes.add(GradeTypeConstants.MAKEUP_ID)
    noPercentGradeTypes.add(GradeTypeConstants.DELAY_ID)
    noPercentGradeTypes.add(GradeTypeConstants.BONUS_ID)
    for (gradeType <- gradeTypes) {
      val egs = courseGradeState.getState(gradeType)
      if (!noPercentGradeTypes.contains(gradeType.getId) && 
        (egs == null || null == egs.getPercent || egs.getPercent <= 0)) //continue
      this.gradeTypes.add(gradeType)
    }
    this.lesson = lesson
    this.courseGrades = courseGrades
    if (CollectUtils.isNotEmpty(this.courseGrades)) {
      Collections.sort(this.courseGrades, new PropertyComparator("std.code"))
    }
  }

  def indexNo(index: Int): String = {
    if (index <= courseGrades.size) {
      beginIndex + index + ""
    } else {
      ""
    }
  }

  def getCourseGrades(): List[CourseGrade] = courseGrades

  def setCourseGrades(courseGrades: List[CourseGrade]) {
    this.courseGrades = courseGrades
  }

  def getGradeTypes(): List[GradeType] = gradeTypes

  def setGradeTypes(gradeTypes: List[GradeType]) {
    this.gradeTypes = gradeTypes
  }

  def getLesson(): Lesson = lesson

  def setLesson(lesson: Lesson) {
    this.lesson = lesson
  }

  def getBeginIndex(): Int = beginIndex

  def setBeginIndex(beginIndex: Int) {
    this.beginIndex = beginIndex
  }

  def getCourseGradeState(): CourseGradeState = courseGradeState

  def setCourseGradeState(courseGradeState: CourseGradeState) {
    this.courseGradeState = courseGradeState
  }
}
