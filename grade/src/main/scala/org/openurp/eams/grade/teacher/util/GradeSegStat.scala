package org.openurp.eams.grade.teacher.util

import org.openurp.teach.code.GradeType
import org.openurp.teach.grade.CourseGrade
import scala.collection.mutable.ListBuffer
import java.util.Collections
import org.openurp.teach.grade.Grade
import scala.collection.JavaConversions._

class GradeSegStat {

  /** 成绩类型 */
  var gradeType: GradeType = _

  /** 总人数 */
  var stdCount: Int = _

  /** 各段统计人数数据 */
  var scoreSegments: ListBuffer[FloatSegment] = _

  /** 最高分 */
  var heighest: Float = _

  /** 最底分 */
  var lowest: Float = _

  /** 平均分 */
  var average: Float = _

  def this(gradeType: GradeType, scoreSegments: List[FloatSegment], grades: List[Grade]) = {
    this()
    this.gradeType = gradeType
    this.scoreSegments = new ListBuffer[FloatSegment]
    for (ss <- scoreSegments) {
      this.scoreSegments.append(ss.clone().asInstanceOf[FloatSegment])
    }
    val grades2 = new collection.mutable.ListBuffer[Grade]
    grades2 ++= grades
    grades2.sorted
    if (!grades2.isEmpty) {
      heighest = grades2(0).score
      lowest = grades2(grades.size() - 1).score
      var sum = 0D
      this.stdCount = grades.size()
      for (grade <- grades) {
        var score = grade.score
        if (null == score) {
          score = 0
        }
        sum = sum + score.doubleValue()
        var appended = false
        for (scoreSeg <- this.scoreSegments) {
          if (!appended && scoreSeg.add(score)) {
            appended = true
          }
        }
      }
      if (0 != this.stdCount) {
        this.average = (sum / this.stdCount).floatValue()
      }
    }

  }

}