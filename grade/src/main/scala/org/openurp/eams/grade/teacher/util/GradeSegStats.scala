package org.openurp.eams.grade.teacher.util

import org.openurp.teach.code.GradeType
import org.openurp.teach.grade.CourseGrade
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import java.util.Collections
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.code.ExamStatus
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.code.model.ExamStatusBean
import org.openurp.teach.grade.ExamGrade
import org.springframework.ui.Model
import org.openurp.teach.grade.Grade
import org.openurp.teach.code.model.GradeTypeBean
import org.openurp.teach.lesson.Lesson

object GradeSegStats {
  val COURSE = "course"

  val LESSON = "lesson"
  def defaultStats(): GradeSegStats = {
    val stats = new GradeSegStats()
    val scoreSegments = new ListBuffer[FloatSegment]
    scoreSegments.append(new FloatSegment(90, 100))
    scoreSegments.append(new FloatSegment(80, 89))
    scoreSegments.append(new FloatSegment(70, 79))
    scoreSegments.append(new FloatSegment(60, 69))
    scoreSegments.append(new FloatSegment(50, 59))
    scoreSegments.append(new FloatSegment(0, 49))
    stats.scoreSegments = scoreSegments
    stats
  }
}
class GradeSegStats {

  /** 各类成绩分段统计数据 */
  var gradeSegStats: ListBuffer[GradeSegStat] = _

  var scoreSegments: ListBuffer[FloatSegment] = _

  var courseGrades: ListBuffer[CourseGrade] = new ListBuffer[CourseGrade]

  var lesson: Lesson = _
  def this(segs: Float) {
    this()
    scoreSegments = new ListBuffer[FloatSegment]
    for (i <- 0 until segs.intValue) {
      scoreSegments.append(new FloatSegment())
    }
  }

  /**
   * 删除空的统计段，并把统计段按照从大到小的顺序排列
   */
  def buildScoreSegments() = {
    val iter = scoreSegments.iterator
    while (iter.hasNext()) {
      val ss = iter.next()
      if (ss.emptySeg()) {
        iter.remove()
      }
    }
    Collections.sort(scoreSegments)
  }

  def stat(gradeTypes: Seq[GradeType], includeFinal: Boolean) {

    gradeSegStats = new ListBuffer[GradeSegStat]
    val normalStatus = new ExamStatusBean(ExamStatus.Normal)
    for (gradeType <- gradeTypes) {
      val grades = new ListBuffer[Grade]
      for (courseGrade <- courseGrades) {
        val examGrade = courseGrade.getGrade(gradeType)
        // 查找考试情况
        val examStatus =
          examGrade match {
            case eg: ExamGrade => eg.examStatus
            case _ => normalStatus
          }
        if (null != examGrade && null != examGrade.score
          && examStatus.id.equals(normalStatus.id)) {
          grades.add(examGrade)
        }
      }
      if (!grades.isEmpty()) {
        // FIX ME
        gradeSegStats.append(new GradeSegStat(gradeType, scoreSegments.toList, grades.toList.asInstanceOf[List[CourseGrade]]))
      }
    }
    if (includeFinal) {
      // 把最终成绩补上
      val gradeType = new GradeTypeBean()
      gradeType.name = "最终成绩"
      val grades = new ListBuffer[CourseGrade]
      grades ++= courseGrades
      gradeSegStats.append(new GradeSegStat(gradeType, scoreSegments.toList, grades.toList))
    }
  }

  /**
   * 进行统计
   *
   * @param gradeTypes
   */
  def stat(gradeTypes: Seq[GradeType]) {
    stat(gradeTypes, true)
  }

}