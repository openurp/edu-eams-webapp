package org.openurp.eams.grade.domain.impl

import java.util.Date
import java.lang.{ Double => JDouble }
import org.beangle.data.model.dao.EntityDao
import org.openurp.eams.grade.CourseGradeState
import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.openurp.eams.grade.service.{ CourseGradeSettings, GradeRateService }
import org.openurp.teach.code.{ CourseTakeType, ExamStatus, GradeType }
import org.openurp.teach.code.GradeType.{ EndGa, DelayGa, MakeupGa, End, Makeup }
import org.openurp.teach.code.model.{ ExamStatusBean, GradeTypeBean }
import org.openurp.teach.grade.{ CourseGrade, ExamGrade, Grade }
import org.openurp.teach.grade.domain.NumPrecisionReserveMethod
import org.openurp.teach.grade.domain.impl.MoreHalfReserveMethod
import org.openurp.teach.grade.model.{ CourseGradeBean, ExamGradeBean }
import org.openurp.teach.grade.model.GaGradeBean
import org.openurp.teach.grade.GaGrade
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
  def calc(g: CourseGrade) {
    val grade = g.asInstanceOf[CourseGradeBean]

    calcGaGrade(grade, EndGa)
    calcGaGrade(grade, DelayGa)
    calcGaGrade(grade, MakeupGa)

    grade.score = calcScore(grade)
    val project = grade.project
    grade.scoreText = gradeRateService.convert(grade.score, grade.markStyle, project)
    if (null != grade.courseTakeType &&
      grade.courseTakeType.id == CourseTakeType.UNTAKE) {
      grade.passed = true
    } else {
      grade.passed = gradeRateService.isPassed(grade.score, grade.markStyle, project)
    }
    for (g <- grade.gaGrades) {
      val gab = g.asInstanceOf[GaGradeBean]
      gab.gp = gradeRateService.calcGp(gab, gab.gradeType)
      gab.passed = gradeRateService.isPassed(gab.score, gab.markStyle, project)
      gab.scoreText = gradeRateService.convert(gab.score, gab.markStyle, project)
    }

    for (eg <- grade.examGrades) {
      val egb = eg.asInstanceOf[ExamGradeBean]
      egb.passed = gradeRateService.isPassed(eg.score, eg.markStyle, project)
      egb.scoreText = gradeRateService.convert(eg.score, eg.markStyle, project)
    }
    grade.gp = gradeRateService.calcGp(grade, grade.gradeType)
    grade.status = guessFinalStatus(grade)
    grade.updatedAt = new Date()
  }

  private def guessFinalStatus(grade: CourseGrade): Int = {
    var status = Grade.Status.New
    for (ga <- grade.gaGrades) {
      if (ga.status > status) status = ga.status
    }
    status
  }

  /**
   * 计算最终得分 MAX(GA,确认的缓考总评,发布的补考成绩)+bonus <br>
   * 如果成绩中有加分项，则在最终成绩上添加该分数。
   *
   * @see GradeTypeConstants.BONUS_ID
   * @return 最好的，可以转化为最终成绩的考试成绩,如果没有任何可选记录仍旧返回原值
   */
  override def calcScore(grade: CourseGrade): java.lang.Float = {
    var best = calcDelayGa(grade)
    val setting = settings.getSetting(grade.project)
    var bonusGrade: ExamGrade = null
    val gaGradeIter = grade.gaGrades.iterator
    while (gaGradeIter.hasNext) {
      val gaGrade = gaGradeIter.next
      if (null != gaGrade.score) {
        if (gaGrade.gradeType == Makeup) {
          if (gaGrade.published && makeupAsFinal) return gaGrade.score
        }
        if (null == best) best = gaGrade.score
        if (gaGrade.score.compareTo(best) > -1) best = gaGrade.score
      }
    }
    if (null != best && null != grade.bonus) best += grade.bonus
    best
  }

  /**
   * 计算总评成绩及其考试情况
   * <p>
   * 如果仅包含总评，仍旧返回原来的值
   */
  override def calcDelayGa(grade: CourseGrade): java.lang.Float = {
    var ga: java.lang.Float = null
    val gaGrade = grade.getGrade(DelayGa)
    if (gaGrade != null) {
      ga = gaGrade.score
      if (grade.examGrades.size == 1) return ga
    }
    val setting = settings.getSetting(grade.project)
    ga = calcGaByPercent(grade, new GradeTypeBean(DelayGa), setting.endGaElements)
    if (null != ga) ga = numPrecisionReserveMethod.reserve(ga, setting.precision)
    ga
  }

  /**
   * 计算总评成绩及其考试情况
   * <p>
   * 如果仅包含总评，仍旧返回原来的值
   */
  override def calcEndGa(grade: CourseGrade): java.lang.Float = {
    var ga: java.lang.Float = null
    val gaGrade = grade.getGrade(EndGa)
    if (gaGrade != null) {
      ga = gaGrade.score
      if (grade.examGrades.size == 1) return ga
    }
    val setting = settings.getSetting(grade.project)
    ga = calcGaByPercent(grade, new GradeTypeBean(EndGa), setting.endGaElements)
    if (null != ga) ga = numPrecisionReserveMethod.reserve(ga, setting.precision)
    ga
  }
  /**
   * 计算总评成绩及其考试情况
   * <p>
   * 如果仅包含总评，仍旧返回原来的值
   */
  override def calcMakeupGa(grade: CourseGrade): java.lang.Float = {
    val examGrade = grade.getGrade(Makeup)
    if (null == examGrade) null
    else {
      if (examGrade.passed) 60 else examGrade.score
    }
  }
  /**
   * 按照正常考试的百分比计算总评<br>
   *
   * @param grade
   * @return 如果百分比未满或考试成绩无效原来的值，否则返回新值
   */
  protected def calcGaByPercent(grade: CourseGrade, gaType: GradeType, elementTypes: collection.Set[GradeType]): java.lang.Float = {
    var ga = 0d
    var percent = 0
    var scorePercent = 0
    var gaExamStatusId = ExamStatus.Normal
    for (gradeType <- elementTypes) {
      val examGrade = grade.getGrade(gradeType).asInstanceOf[ExamGrade]
      if (null != examGrade && null != examGrade.percent && examGrade.percent > 0) {
        var myPercent = examGrade.percent
        if (null != examGrade.examStatus) {
          if (examGrade.examStatus.id == ExamStatus.Violation ||
            examGrade.examStatus.id == ExamStatus.Cheat) {
            gaExamStatusId = examGrade.examStatus.id
          }
        }
        if (!(null == examGrade.score && (null == examGrade.examStatus || examGrade.examStatus.id == ExamStatus.Normal))) {
          val score = examGrade.score
          percent += myPercent
          if (null != score) {
            scorePercent += myPercent
            ga += myPercent * score.doubleValue / 100
          }
        }
      }
    }
    if (JDouble.compare(percent, 0.9999) < 0) {
      if (JDouble.compare(percent, 0.0001) > 0) {
        null
      } else {
        val gaGrade = grade.getGrade(gaType)
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
   */
  private def calcGaGrade(grade: CourseGradeBean, gaTypeId: Integer): Unit = {
    val ga = calcEndGa(grade)
    if (null != ga) {
      var gaGrade = grade.getGrade(gaTypeId).asInstanceOf[GaGradeBean]
      if (null == gaGrade) {
        gaGrade = new GaGradeBean
        gaGrade.markStyle = grade.markStyle
        gaGrade.courseGrade = grade
        gaGrade.gradeType = new GradeTypeBean(gaTypeId)
        gaGrade.updatedAt = new Date()
        gaGrade.status = grade.status
        grade.asInstanceOf[CourseGradeBean].gaGrades += gaGrade
      }
      gaGrade.score = ga * gaGrade.ratio / 100
    } else {
      val gaGrade = grade.getGrade(gaTypeId).asInstanceOf[GaGrade]
      if (null != gaGrade) grade.gaGrades -= gaGrade
    }

  }

}
