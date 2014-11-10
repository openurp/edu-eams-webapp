package org.openurp.eams.grade.domain.impl

import org.beangle.data.model.dao.EntityDao
import org.openurp.teach.grade.domain.NumPrecisionReserveMethod
import org.openurp.eams.grade.service._
import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.openurp.teach.grade.domain.impl.MoreHalfReserveMethod
import org.openurp.teach.grade.ExamGrade
import org.openurp.teach.grade.Grade
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.ExamStatus
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.code.CourseTakeType
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.model.GradeTypeBean
import org.openurp.teach.grade.model._
import java.util.Date
import org.openurp.teach.code.model.ExamStatusBean
import org.openurp.eams.grade._
import java.lang.{ Double => JDouble }

object DefaultCourseGradeCalculator {

  val Ga = new GradeTypeBean(GradeType.Ga)

  val Makeup = new GradeTypeBean(GradeType.Makeup)

  val Delay = new GradeTypeBean(GradeType.Delay)

  val End = new GradeTypeBean(GradeType.End)
}

/**
 * 缺省的成绩计算器
 *
 * @author chaostone
 */
class DefaultCourseGradeCalculator extends CourseGradeCalculator {

  var entityDao: EntityDao = _

  var gradeRateService: GradeRateService = _

  /**
   * 补考成绩是否是最终成绩，如果不是则按照和原来的得分进行比较取最好成绩
   */
  var makeupAsFinal: Boolean = _

  var settings: CourseGradeSettings = _

  var numPrecisionReserveMethod: NumPrecisionReserveMethod = new MoreHalfReserveMethod()

  import DefaultCourseGradeCalculator._

  def updateScore(grade: CourseGradeBean, score: java.lang.Float) {
    grade.score = score
    grade.passed = gradeRateService.isPassed(score, grade.markStyle, grade.project)
    grade.scoreText = gradeRateService.convert(score, grade.markStyle, grade.project)
    grade.updatedAt = new Date()
  }

  /**
   * 计算总评成绩,最终成绩,是否通过和绩点以及分数字面值
   *
   * @param grade
   */
  def calc(grade: CourseGradeBean, state: CourseGradeState) {
    val ga = calcGa(grade, state)
    if (null != ga) {
      getGaGrade(grade).score = ga
    } else {
      val gaGrade = grade.getExamGrade(Ga)
      if (null != gaGrade &&
        (null == gaGrade.examStatus || gaGrade.examStatus.id == ExamStatus.Normal)) {
        grade.examGrades -= gaGrade
      }
    }
    grade.score = calcScore(grade, state)
    val project = grade.project
    grade.scoreText = gradeRateService.convert(grade.score, grade.markStyle, project)
    if (null != grade.courseTakeType &&
      grade.courseTakeType.id == CourseTakeType.UNTAKE) {
      grade.passed = true
    } else {
      grade.passed = gradeRateService.isPassed(grade.score, grade.markStyle, project)
    }
    for (eg <- grade.examGrades) {
      val egb = eg.asInstanceOf[ExamGradeBean]
      egb.passed = gradeRateService.isPassed(eg.score, eg.markStyle, project)
      egb.scoreText = gradeRateService.convert(eg.score, eg.markStyle, project)
    }
    grade.gp = gradeRateService.calcGp(grade)
    grade.status = guessFinalStatus(grade)
    grade.updatedAt = new Date()
  }

  private def guessFinalStatus(grade: CourseGrade): Int = {
    var status = Grade.Status.New
    val ga = grade.getExamGrade(Ga)
    if (null != ga && ga.status > status) status = ga.status
    val makeup = grade.getExamGrade(Makeup)
    if (null != makeup && makeup.status > status) status = makeup.status
    status
  }

  /**
   * 计算最终得分 MAX(GA,确认的缓考总评,发布的补考成绩)+bonus <br>
   * 如果成绩中有加分项，则在最终成绩上添加该分数。
   *
   * @see GradeTypeConstants.BONUS_ID
   * @return 最好的，可以转化为最终成绩的考试成绩,如果没有任何可选记录仍旧返回原值
   */
  override def calcScore(grade: CourseGrade, state: CourseGradeState): java.lang.Float = {
    var best = calcDelayGa(grade, state)
    val setting = settings.getSetting(grade.project)
    var bonusGrade: ExamGrade = null
    val examGradeIter = grade.examGrades.iterator
    while (examGradeIter.hasNext) {
      val examGrade = examGradeIter.next
      if (examGrade.gradeType.id == GradeType.Bonus) {
        bonusGrade = examGrade
      } else {
        if (setting.finalCandinateTypes.contains(examGrade.gradeType) && null != examGrade.score) {
          if (examGrade.gradeType == Makeup) {
            if (examGrade.published && makeupAsFinal) return examGrade.score
          }
          if (null == best) best = examGrade.score
          if (examGrade.score.compareTo(best) > -1) best = examGrade.score
        }
      }
    }
    if (null != best && null != bonusGrade && bonusGrade.published &&
      null != bonusGrade.score) {
      best += bonusGrade.score
    }
    best
  }

  /**
   * 计算总评成绩及其考试情况
   * <p>
   * 如果仅包含总评，仍旧返回原来的值
   */
  override def calcGa(grade: CourseGrade, state: CourseGradeState): java.lang.Float = {
    var ga: java.lang.Float = null
    val gaGrade = grade.getExamGrade(Ga)
    if (gaGrade != null) {
      ga = gaGrade.score
      if (grade.examGrades.size == 1) return ga
    }
    if (null == state) return ga
    if (null != state) {
      ga = calcGaByPercent(grade, state)
    }
    if (null != ga) {
      ga = numPrecisionReserveMethod.reserve(ga, state.precision)
    }
    ga
  }

  /**
   * 按照正常考试的百分比计算总评<br>
   *
   * @param grade
   * @param gradeState
   * @return 如果百分比未满或考试成绩无效原来的值，否则返回新值
   */
  protected def calcGaByPercent(grade: CourseGrade, gradeState: CourseGradeState): java.lang.Float = {
    var ga = 0d
    var percent = 0f
    var scorePercent = 0f
    val calcGaExamStatus = settings.getSetting(grade.project).calcGaExamStatus
    var gaExamStatusId = ExamStatus.Normal
    val endExamGrade = grade.getExamGrade(End)
    if (null != endExamGrade && null != endExamGrade.examStatus) {
      gaExamStatusId = endExamGrade.examStatus.id
    }
    for (state <- gradeState.states) {
      var myPercent = state.percent
      val examGrade = grade.getExamGrade(state.gradeType)
      if (null != myPercent && myPercent > 0 && null != examGrade) {
        if (null != examGrade.examStatus) {
          if (examGrade.examStatus.id == ExamStatus.Violation ||
            examGrade.examStatus.id == ExamStatus.Cheat) {
            gaExamStatusId = examGrade.examStatus.id
          }
        }
        if (!(null == examGrade.score && (null == examGrade.examStatus || examGrade.examStatus.id == ExamStatus.Normal))) {
          val score = examGrade.score
          if (examGrade.percent != null) {
            myPercent = examGrade.percent.toFloat / 100
          }
          percent += myPercent
          if (null != score) {
            scorePercent += myPercent
            ga += myPercent * score.doubleValue()
          }
        }
      }
    }
    if (calcGaExamStatus) getGaGrade(grade).examStatus = new ExamStatusBean(gaExamStatusId)
    if (JDouble.compare(percent, 0.9999) < 0) {
      if (JDouble.compare(percent, 0.0001) > 0) {
        null
      } else {
        val gaGrade = grade.getExamGrade(Ga)
        if (gaGrade != null) gaGrade.score else null
      }
    } else {
      if (JDouble.compare(scorePercent, 0.51) <= 0) {
        null
      } else {
        if (JDouble.compare(scorePercent, 0.9999) >= 0 &&
          (ExamStatus.Cheat == gaExamStatusId || ExamStatus.Violation == gaExamStatusId)) ga = 0
        new java.lang.Float(ga)
      }
    }
  }

  /**
   * 计算缓考总评
   *
   * @param grade
   * @param gradeState
   */
  protected def calcDelayGa(grade: CourseGrade, gradeState: CourseGradeState): java.lang.Float = {
    if (null == gradeState) return null
    val setting = settings.getSetting(grade.project)
    if (setting.finalCandinateTypes.contains(Delay)) {
      val delayGrade = grade.getExamGrade(Delay)
      return if (null != delayGrade && delayGrade.confirmed) numPrecisionReserveMethod.reserve(delayGrade.score, gradeState.precision)
      else null
    }
    var ga = 0d
    var percent = 0f
    var scorePercent = 0f
    for (state <- gradeState.states) {
      var myPercent: java.lang.Float = null
      myPercent = if (state.gradeType == Delay) state.gradeState.percent(End) else state.percent
      val examGrade = grade.getExamGrade(state.gradeType)
      if (null != myPercent && myPercent > 0 && null != examGrade) {
        if (!(null == examGrade.score &&
          (null == examGrade.examStatus || examGrade.examStatus.id == ExamStatus.Normal))) {
          val score = examGrade.score
          if (examGrade.percent != null) {
            myPercent = examGrade.percent.toFloat / 100
          }
          percent += myPercent
          if (null != score) {
            scorePercent += myPercent
            ga += myPercent * score.doubleValue()
          }
        }
      }
    }
    if (java.lang.Double.compare(percent, 0.9999) < 0) {
      null
    } else {
      if ((java.lang.Double.compare(scorePercent, 0.51) <= 0)) null else numPrecisionReserveMethod.reserve(new java.lang.Float(ga),
        gradeState.precision)
    }
  }

  /**
   * 返回考试成绩中的总评成绩
   *
   * @param grade
   * @return
   */
  private def getGaGrade(grade: CourseGrade): ExamGradeBean = {
    var examGrade = grade.getExamGrade(Ga).asInstanceOf[ExamGradeBean]
    if (null != examGrade) return examGrade
    examGrade = new ExamGradeBean
    examGrade.markStyle = grade.markStyle
    examGrade.examStatus = new ExamStatusBean(ExamStatus.Normal)
    examGrade.courseGrade = grade
    examGrade.gradeType = new GradeTypeBean(GradeType.Ga)
    examGrade.updatedAt = new Date()
    examGrade.status = grade.status
    grade.asInstanceOf[CourseGradeBean].examGrades += examGrade
    examGrade
  }

}
