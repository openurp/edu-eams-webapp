package org.openurp.edu.eams.teach.grade.service.impl

import java.util.Date
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.entity.metadata.Model
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.eams.teach.code.industry.ExamStatus
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.eams.teach.grade.service.NumPrecisionReserveMethod
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGrade
import org.openurp.edu.eams.teach.lesson.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import DefaultCourseGradeCalculator._

import scala.collection.JavaConversions._

object DefaultCourseGradeCalculator {

  private val Ga = new GradeType(GradeTypeConstants.GA_ID)

  private val Makeup = new GradeType(GradeTypeConstants.MAKEUP_ID)

  private val Delay = new GradeType(GradeTypeConstants.DELAY_ID)

  private val End = new GradeType(GradeTypeConstants.END_ID)
}

class DefaultCourseGradeCalculator extends CourseGradeCalculator {

  protected var entityDao: EntityDao = _

  protected var gradeRateService: GradeRateService = _

  protected var makeupAsFinal: Boolean = _

  protected var settings: CourseGradeSettings = _

  protected var numPrecisionReserveMethod: NumPrecisionReserveMethod = new MoreHalfReserveMethod()

  def updateScore(grade: CourseGrade, score: java.lang.Float) {
    grade.setScore(score)
    grade.setPassed(gradeRateService.isPassed(score, grade.getMarkStyle, grade.getProject))
    grade.setScoreText(gradeRateService.convert(score, grade.getMarkStyle, grade.getProject))
    grade.setUpdatedAt(new Date())
  }

  def calc(grade: CourseGrade, state: CourseGradeState) {
    val ga = calcGa(grade, state)
    if (null != ga) {
      getGaGrade(grade).setScore(ga)
    } else {
      val gaGrade = grade.getExamGrade(Ga)
      if (null != gaGrade && 
        (null == gaGrade.getExamStatus || gaGrade.getExamStatus.getId == ExamStatus.NORMAL)) {
        grade.getExamGrades.remove(gaGrade)
      }
    }
    grade.setScore(calcScore(grade, state))
    val project = grade.getProject
    grade.setScoreText(gradeRateService.convert(grade.getScore, grade.getMarkStyle, project))
    if (null != grade.getCourseTakeType && 
      grade.getCourseTakeType.getId == CourseTakeType.UNTAKE) {
      grade.setPassed(true)
    } else {
      grade.setPassed(gradeRateService.isPassed(grade.getScore, grade.getMarkStyle, project))
    }
    for (eg <- grade.getExamGrades) {
      eg.setPassed(gradeRateService.isPassed(eg.getScore, eg.getMarkStyle, project))
      eg.setScoreText(gradeRateService.convert(eg.getScore, eg.getMarkStyle, project))
    }
    grade.setGp(gradeRateService.calcGp(grade))
    grade.setStatus(guessFinalStatus(grade))
    grade.setUpdatedAt(new Date())
  }

  private def guessFinalStatus(grade: CourseGrade): Int = {
    var status = Grade.Status.NEW
    val ga = grade.getExamGrade(Ga)
    if (null != ga && ga.getStatus > status) status = ga.getStatus
    val makeup = grade.getExamGrade(Makeup)
    if (null != makeup && makeup.getStatus > status) status = makeup.getStatus
    status
  }

  def calcScore(grade: CourseGrade, state: CourseGradeState): java.lang.Float = {
    var best = calcDelayGa(grade, state)
    val setting = settings.getSetting(grade.getProject)
    var bonusGrade: ExamGrade = null
    for (examGrade <- grade.getExamGrades) {
      if (examGrade.gradeType.getId == GradeTypeConstants.BONUS_ID) {
        bonusGrade = examGrade
        //continue
      }
      if (!setting.getFinalCandinateTypes.contains(examGrade.gradeType)) //continue
      if (null == examGrade.getScore) //continue
      if (examGrade.gradeType == Makeup) {
        if (!examGrade.isPublished) //continue
        if (makeupAsFinal) {
          return examGrade.getScore
        }
      }
      if (null == best) best = examGrade.getScore
      if (examGrade.getScore.compareTo(best) > -1) best = examGrade.getScore
    }
    if (null != best && null != bonusGrade && bonusGrade.isPublished && 
      null != bonusGrade.getScore) {
      best += bonusGrade.getScore
    }
    best
  }

  def calcGa(grade: CourseGrade, state: CourseGradeState): java.lang.Float = {
    var ga: java.lang.Float = null
    val gaGrade = grade.getExamGrade(Ga)
    if (gaGrade != null) {
      ga = gaGrade.getScore
      if (grade.getExamGrades.size == 1) return ga
    }
    if (null == state) return ga
    if (null != state) {
      ga = calcGaByPercent(grade, state)
    }
    if (null != ga) {
      ga = numPrecisionReserveMethod.reserve(ga, state.getPrecision)
    }
    ga
  }

  protected def calcGaByPercent(grade: CourseGrade, gradeState: CourseGradeState): java.lang.Float = {
    var ga = 0
    var percent = 0
    var scorePercent = 0
    val calcGaExamStatus = settings.getSetting(grade.getProject).isCalcGaExamStatus
    var gaExamStatusId = ExamStatus.NORMAL
    val endExamGrade = grade.getExamGrade(End)
    if (null != endExamGrade && null != endExamGrade.getExamStatus) {
      gaExamStatusId = endExamGrade.getExamStatus.getId
    }
    for (state <- gradeState.getStates) {
      var myPercent = state.getPercent
      if (null == myPercent || myPercent <= 0) //continue
      val examGrade = grade.getExamGrade(state.gradeType)
      if (null == examGrade) //continue
      if (null != examGrade.getExamStatus) {
        if (examGrade.getExamStatus.getId == ExamStatus.VIOLATION || 
          examGrade.getExamStatus.getId == ExamStatus.CHEAT) {
          gaExamStatusId = examGrade.getExamStatus.getId
        }
      }
      if (null == examGrade.getScore && 
        (null == examGrade.getExamStatus || examGrade.getExamStatus.getId == ExamStatus.NORMAL)) //continue
      val score = examGrade.getScore
      if (examGrade.getPercent != null) {
        myPercent = examGrade.getPercent.toFloat / 100
      }
      percent += myPercent
      if (null != score) {
        scorePercent += myPercent
        ga += myPercent * score.doubleValue()
      }
    }
    if (calcGaExamStatus) getGaGrade(grade).setExamStatus(new ExamStatus(gaExamStatusId))
    if (java.lang.Double.compare(percent, 0.9999) < 0) {
      if (java.lang.Double.compare(percent, 0.0001) > 0) {
        null
      } else {
        val gaGrade = grade.getExamGrade(Ga)
        if (gaGrade != null) gaGrade.getScore else null
      }
    } else {
      if (java.lang.Double.compare(scorePercent, 0.51) <= 0) {
        null
      } else {
        if (java.lang.Double.compare(scorePercent, 0.9999) >= 0 && 
          (ExamStatus.CHEAT == gaExamStatusId || ExamStatus.VIOLATION == gaExamStatusId)) ga = 0
        new java.lang.Float(ga)
      }
    }
  }

  protected def calcDelayGa(grade: CourseGrade, gradeState: CourseGradeState): java.lang.Float = {
    if (null == gradeState) return null
    val setting = settings.getSetting(grade.getProject)
    if (setting.getFinalCandinateTypes.contains(Delay)) {
      val delayGrade = grade.getExamGrade(Delay)
      if (null != delayGrade && delayGrade.isConfirmed) return numPrecisionReserveMethod.reserve(delayGrade.getScore, 
        gradeState.getPrecision) else return null
    }
    var ga = 0
    var percent = 0
    var scorePercent = 0
    for (state <- gradeState.getStates) {
      var myPercent: java.lang.Float = null
      myPercent = if (state.gradeType == Delay) state.gradeState.getPercent(End) else state.getPercent
      if (null == myPercent || myPercent <= 0) //continue
      val examGrade = grade.getExamGrade(state.gradeType)
      if (null == examGrade) //continue
      if (null == examGrade.getScore && 
        (null == examGrade.getExamStatus || examGrade.getExamStatus.getId == ExamStatus.NORMAL)) //continue
      val score = examGrade.getScore
      if (examGrade.getPercent != null) {
        myPercent = examGrade.getPercent.toFloat / 100
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
        gradeState.getPrecision)
    }
  }

  private def getGaGrade(grade: CourseGrade): ExamGrade = {
    var examGrade = grade.getExamGrade(Ga)
    if (null != examGrade) return examGrade
    examGrade = Model.newInstance(classOf[ExamGrade]).asInstanceOf[ExamGrade]
    examGrade.setMarkStyle(grade.getMarkStyle)
    examGrade.setExamStatus(new ExamStatus(ExamStatus.NORMAL))
    examGrade.setCourseGrade(grade)
    examGrade.setGradeType(new GradeType(GradeTypeConstants.GA_ID))
    examGrade.setCreatedAt(new Date())
    examGrade.setUpdatedAt(new Date())
    examGrade.setStatus(grade.getStatus)
    grade.getExamGrades.add(examGrade)
    examGrade
  }

  def setGradeRateService(gradeRateService: GradeRateService) {
    this.gradeRateService = gradeRateService
  }

  def isMakeupAsFinal(): Boolean = makeupAsFinal

  def setMakeupAsFinal(makeupAsFinal: Boolean) {
    this.makeupAsFinal = makeupAsFinal
  }

  def getNumPrecisionReserveMethod(): NumPrecisionReserveMethod = numPrecisionReserveMethod

  def setNumPrecisionReserveMethod(numPrecisionReserveMethod: NumPrecisionReserveMethod) {
    this.numPrecisionReserveMethod = numPrecisionReserveMethod
  }

  def setSettings(settings: CourseGradeSettings) {
    this.settings = settings
  }

  def getGradeRateService(): GradeRateService = gradeRateService

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
