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
import org.openurp.teach.code.ExamStatus
import org.openurp.eams.grade._

object DefaultCourseGradeCalculator {

  private val Ga = new GradeTypeBean(GradeType.GA_ID)

  private val Makeup = new GradeTypeBean(GradeType.MAKEUP_ID)

  private val Delay = new GradeTypeBean(GradeType.DELAY_ID)

  private val End = new GradeTypeBean(GradeType.END_ID)
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
   补考成绩是否是最终成绩，如果不是则按照和原来的得分进行比较取最好成绩
   */
    var makeupAsFinal: Boolean = _
  
  var settings: CourseGradeSettings = _

    var numPrecisionReserveMethod: NumPrecisionReserveMethod = new MoreHalfReserveMethod()

    import DefaultCourseGradeCalculator._
  def updateScore(grade: CourseGradeBean, score: java.lang.Float) {
    grade.score = score
    grade.passed = gradeRateService.isPassed(score, grade.markStyle, grade.project)
    grade.scoreText=gradeRateService.convert(score, grade.markStyle, grade.project)
    grade.updatedAt =new Date()
  }

  /**
   * 计算总评成绩,最终成绩,是否通过和绩点以及分数字面值
   *
   * @param grade
   */
  def calc(grade: CourseGradeBean, state: CourseGradeState) {
    val ga = calcGa(grade, state)
    if (null != ga) {
      getGaGrade(grade).score =ga
    } else {
      val gaGrade = grade.getExamGrade(Ga)
      if (null != gaGrade && 
        (null == gaGrade.ExamStatus || gaGrade.ExamStatus.id == ExamStatus.NORMAL)) {
        grade.examGrades -= gaGrade
      }
    }
    grade.score =calcScore(grade, state)
    val project = grade.project
    grade.scoreText =gradeRateService.convert(grade.score, grade.markStyle, project)
    if (null != grade.courseTakeType && 
      grade.courseTakeType.id == CourseTakeType.UNTAKE) {
      grade.passed = true
    } else {
      grade.passed = gradeRateService.isPassed(grade.score, grade.markStyle, project)
    }
    for (eg <- grade.examGrades) {
      eg.passed=gradeRateService.isPassed(eg.score, eg.markStyle, project)
      eg.scoreText=gradeRateService.convert(eg.score, eg.markStyle, project)
    }
    grade.gp=gradeRateService.calcGp(grade)
    grade.status=guessFinalStatus(grade)
    grade.updatedAt = new Date()
  }

  private def guessFinalStatus(grade: CourseGrade): Int = {
    var status = Grade.Status.NEW
    val ga = grade.getExamGrade(Ga)
    if (null != ga && ga.Status > status) status = ga.Status
    val makeup = grade.getExamGrade(Makeup)
    if (null != makeup && makeup.Status > status) status = makeup.Status
    status
  }

  /**
   * 计算最终得分 MAX(GA,确认的缓考总评,发布的补考成绩)+bonus <br>
   * 如果成绩中有加分项，则在最终成绩上添加该分数。
   *
   * @see GradeTypeConstants.BONUS_ID
   * @return 最好的，可以转化为最终成绩的考试成绩,如果没有任何可选记录仍旧返回原值
   */
  def calcScore(grade: CourseGrade, state: CourseGradeState): java.lang.Float = {
    var best = calcDelayGa(grade, state)
    val setting = settings.Setting(grade.project)
    var bonusGrade: ExamGrade = null
    for (examGrade <- grade.examGrades) {
      if (examGrade.gradeType.id == GradeType.BONUS_ID) {
        bonusGrade = examGrade
        //continue
      }
      if (!setting.getFinalCandinateTypes.contains(examGrade.gradeType)) //continue
      if (null == examgrade.score) //continue
      if (examGrade.gradeType == Makeup) {
        if (!examGrade.isPublished) //continue
        if (makeupAsFinal) {
          return examgrade.score
        }
      }
      if (null == best) best = examgrade.score
      if (examgrade.score.compareTo(best) > -1) best = examgrade.score
    }
    if (null != best && null != bonusGrade && bonusGrade.isPublished && 
      null != bonusgrade.score) {
      best += bonusgrade.score
    }
    best
  }

  /**
   * 计算总评成绩及其考试情况
   * <p>
   * 如果仅包含总评，仍旧返回原来的值
   */
  def calcGa(grade: CourseGrade, state: CourseGradeState): java.lang.Float = {
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
    var ga = 0
    var percent = 0
    var scorePercent = 0
    val calcGaExamStatus = settings.Setting(grade.project).isCalcGaExamStatus
    var gaExamStatusId = ExamStatus.NORMAL
    val endExamGrade = grade.getExamGrade(End)
    if (null != endExamGrade && null != endExamGrade.ExamStatus) {
      gaExamStatusId = endExamGrade.ExamStatus.id
    }
    for (state <- gradeState.states) {
      var myPercent = state.percent
      if (null == myPercent || myPercent <= 0) //continue
      val examGrade = grade.getExamGrade(state.gradeType)
      if (null == examGrade) //continue
      if (null != examGrade.ExamStatus) {
        if (examGrade.ExamStatus.id == ExamStatus.VIOLATION || 
          examGrade.ExamStatus.id == ExamStatus.CHEAT) {
          gaExamStatusId = examGrade.ExamStatus.id
        }
      }
      if (null == examgrade.score && 
        (null == examGrade.ExamStatus || examGrade.ExamStatus.id == ExamStatus.NORMAL)) //continue
      val score = examgrade.score
      if (examGrade.percent != null) {
        myPercent = examGrade.percent.toFloat / 100
      }
      percent += myPercent
      if (null != score) {
        scorePercent += myPercent
        ga += myPercent * score.doubleValue()
      }
    }
    if (calcGaExamStatus) getGaGrade(grade).setExamStatus(new ExamStatus(gaExamStatusId))
    if (Double.compare(percent, 0.9999) < 0) {
      if (Double.compare(percent, 0.0001) > 0) {
        null
      } else {
        val gaGrade = grade.ExamGrade(Ga)
        if (gaGrade != null) gagrade.score else null
      }
    } else {
      if (Double.compare(scorePercent, 0.51) <= 0) {
        null
      } else {
        if (Double.compare(scorePercent, 0.9999) >= 0 && 
          (ExamStatus.CHEAT == gaExamStatusId || ExamStatus.VIOLATION == gaExamStatusId)) ga = 0
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
    val setting = settings.Setting(grade.project)
    if (setting.getFinalCandinateTypes.contains(Delay)) {
      val delayGrade = grade.getExamGrade(Delay)
      if (null != delayGrade && delayGrade.isConfirmed) return numPrecisionReserveMethod.reserve(delaygrade.score, 
        gradeState.precision) else return null
    }
    var ga = 0
    var percent = 0
    var scorePercent = 0
    for (state <- gradeState.states) {
      var myPercent: java.lang.Float = null
      myPercent = if (state.gradeType == Delay) state.GradeState.percent(End) else state.percent
      if (null == myPercent || myPercent <= 0) //continue
      val examGrade = grade.getExamGrade(state.gradeType)
      if (null == examGrade) //continue
      if (null == examgrade.score && 
        (null == examGrade.ExamStatus || examGrade.examStatus.id == ExamStatus.NORMAL)) //continue
      val score = examgrade.score
      if (examGrade.percent != null) {
        myPercent = examGrade.percent.toFloat / 100
      }
      percent += myPercent
      if (null != score) {
        scorePercent += myPercent
        ga += myPercent * score.doubleValue()
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
  private def getGaGrade(grade: CourseGrade): ExamGrade = {
    var examGrade = grade.getExamGrade(Ga).asInstanceOf[ExamGradeBean]
    if (null != examGrade) return examGrade
    examGrade = Model.newInstance(classOf[ExamGrade]).asInstanceOf[ExamGrade]
    examGrade.setMarkStyle(grade.markStyle)
    examGrade.setExamStatus(new ExamStatus(ExamStatus.NORMAL))
    examGrade.setCourseGrade(grade)
    examGrade.setGradeType(new GradeType(GradeTypeConstants.GA_ID))
    examGrade.setCreatedAt(new Date())
    examGrade.setUpdatedAt(new Date())
    examGrade.setStatus(grade.Status)
    grade.examGrades.add(examGrade)
    examGrade
  }

}
