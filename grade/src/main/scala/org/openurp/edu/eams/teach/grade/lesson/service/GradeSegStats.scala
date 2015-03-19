package org.openurp.edu.eams.teach.grade.lesson.service


import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.util.stat.FloatSegment
import GradeSegStats._



object GradeSegStats {

  val COURSE = "course"

  val LESSON = "lesson"

  def defaultStats(): GradeSegStats = {
    val stats = new GradeSegStats()
    val scoreSegments = CollectUtils.newArrayList(6)
    scoreSegments.add(new FloatSegment(90, 100))
    scoreSegments.add(new FloatSegment(80, 89))
    scoreSegments.add(new FloatSegment(70, 79))
    scoreSegments.add(new FloatSegment(60, 69))
    scoreSegments.add(new FloatSegment(50, 59))
    scoreSegments.add(new FloatSegment(0, 49))
    stats.setScoreSegments(scoreSegments)
    stats
  }
}

class GradeSegStats {

  protected var gradeSegStats: List[GradeSegStat] = _

  protected var scoreSegments: List[FloatSegment] = _

  protected var courseGrades: List[CourseGrade] = _

  def this(segs: Int) {
    this()
    scoreSegments = CollectUtils.newArrayList(segs)
    for (i <- 0 until segs) {
      scoreSegments.add(new FloatSegment())
    }
  }

  def buildScoreSegments() {
    var iter = scoreSegments.iterator()
    while (iter.hasNext) {
      val ss = iter.next()
      if (ss.emptySeg()) {
        iter.remove()
      }
    }
    Collections.sort(scoreSegments)
  }

  def stat(gradeTypes: List[GradeType]) {
    gradeSegStats = CollectUtils.newArrayList()
    val endGradeType = new GradeType(GradeTypeConstants.END_ID)
    val normalStatus = new ExamStatus(ExamStatus.NORMAL)
    for (gradeType <- gradeTypes) {
      val grades = CollectUtils.newArrayList()
      for (courseGrade <- courseGrades) {
        if (gradeType.id == GradeTypeConstants.FINAL_ID) {
          grades.add(courseGrade)
        } else {
          val examGrade = courseGrade.getExamGrade(gradeType)
          var examStatus: ExamStatus = null
          if (null != examGrade) examStatus = examGrade.getExamStatus
          if (gradeType.id == GradeTypeConstants.GA_ID) {
            val endGrade = courseGrade.getExamGrade(endGradeType)
            if (null != endGrade) examStatus = endGrade.getExamStatus
          }
          if (null == examStatus) examStatus = normalStatus
          if (null != examGrade && null != examGrade.getScore && examStatus.id == normalStatus.id) {
            grades.add(examGrade)
          }
        }
      }
      if (!grades.isEmpty) {
        gradeSegStats.add(new GradeSegStat(gradeType, scoreSegments, grades))
      }
    }
  }

  def getGradeSegStats(): List[GradeSegStat] = gradeSegStats

  def setGradeSegStats(gradeSegStats: List[GradeSegStat]) {
    this.gradeSegStats = gradeSegStats
  }

  def getScoreSegments(): List[FloatSegment] = scoreSegments

  def setScoreSegments(scoreSegments: List[FloatSegment]) {
    this.scoreSegments = scoreSegments
  }

  def getCourseGrades(): List[CourseGrade] = courseGrades

  def setCourseGrades(courseGrades: List[CourseGrade]) {
    this.courseGrades = courseGrades
  }
}
