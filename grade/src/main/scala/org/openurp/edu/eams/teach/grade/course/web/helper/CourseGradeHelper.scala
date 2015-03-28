package org.openurp.edu.eams.teach.grade.course.web.helper

import java.util.Date


import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.Collections
import org.beangle.data.model.dao.EntityDao
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.ems.dictionary.service.BaseCodeService
import org.beangle.security.blueprint.User
import org.beangle.struts2.helper.ContextHelper
import org.beangle.struts2.helper.Params
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseGradeStateBean



class CourseGradeHelper {

  protected var entityDao: EntityDao = _

  protected var baseCodeService: BaseCodeService = _

  protected var calculator: CourseGradeCalculator = _

  protected var courseGradeService: CourseGradeService = _

  protected var gradeRateService: GradeRateService = _

  def editGrade() {
    var courseGradeId = Params.getLong("courseGradeId")
    if (courseGradeId == null) {
      courseGradeId = Params.getLong("courseGrade.id")
    }
    val courseGrade = entityDao.get(classOf[CourseGrade], courseGradeId)
    ContextHelper.put("courseGrade", courseGrade)
    ContextHelper.put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    ContextHelper.put("examStatuses", baseCodeService.getCodes(classOf[ExamStatus]))
    ContextHelper.put("courseTakeTypes", baseCodeService.getCodes(classOf[CourseTakeType]))
    ContextHelper.put("markStyles", gradeRateService.getMarkStyles(courseGrade.getProject))
    ContextHelper.put("converter", gradeRateService)
    ContextHelper.put("courseGradeAlterInfos", Collections.emptyList())
    ContextHelper.put("examGradeAlterInfos", Collections.emptyList())
    ContextHelper.put("gradeState", courseGradeService.getState(courseGrade.getLesson))
    val allTypes = baseCodeService.getCodes(classOf[GradeType])
    for (`type` <- allTypes) {
      if (`type`.id == GradeTypeConstants.FINAL_ID) {
        //continue
      }
      if (null == courseGrade.getExamGrade(`type`)) {
        val grade = Model.newInstance(classOf[ExamGrade])
        grade.setMarkStyle(courseGrade.getMarkStyle)
        grade.setGradeType(`type`)
        grade.setExamStatus(entityDao.get(classOf[ExamStatus], ExamStatus.NORMAL))
        courseGrade.addExamGrade(grade)
      }
    }
  }

  def saveGrade(user: User) {
    val courseGradeId = Params.getLong("courseGrade.id")
    val courseGrade = entityDao.get(classOf[CourseGrade], courseGradeId)
    courseGrade.setUpdatedAt(new Date())
    courseGrade.setOperator(user.getName)
    courseGrade.setCourseType(entityDao.get(classOf[CourseType], Params.getInt("courseGrade.courseType.id")))
    courseGrade.setCourseTakeType(entityDao.get(classOf[CourseTakeType], Params.getInt("courseGrade.courseTakeType.id")))
    courseGrade.setMarkStyle(entityDao.get(classOf[ScoreMarkStyle], Params.getInt("courseGrade.markStyle.id")))
    val status = Params.getInt("courseGrade.status")
    if (null != status) {
      courseGrade.setStatus(status)
    }
    val gradeTypeIds = Strings.splitToInt(Params.get("gradeTypeId"))
    if (null != gradeTypeIds && gradeTypeIds.length != 0) {
      for (i <- 0 until gradeTypeIds.length) {
        val gradeType = entityDao.get(classOf[GradeType], gradeTypeIds(i))
        var examGrade = courseGrade.getExamGrade(gradeType)
        val score = Params.getFloat("score" + gradeTypeIds(i))
        val examStatusId = Params.getInt("examStatusId" + gradeTypeIds(i))
        if (null == examGrade) {
          examGrade = Model.newInstance(classOf[ExamGrade])
          examGrade.setGradeType(gradeType)
          var examMarkStyle = courseGrade.getMarkStyle
          if (null != courseGrade.getLesson) {
            val courseGradeState = courseGradeService.getState(courseGrade.getLesson)
            if (null != courseGradeState) {
              val examGradeTypeState = courseGradeState.getState(gradeType)
              if (null != examGradeTypeState) {
                examMarkStyle = examGradeTypeState.getScoreMarkStyle
              }
            }
          }
          examGrade.setMarkStyle(examMarkStyle)
        }
        if (null != score || 
          null != examStatusId && examStatusId != ExamStatus.NORMAL) {
          var examStatus = Params.getInt("status" + gradeTypeIds(i))
          if (null == examStatus) {
            examStatus = new java.lang.Integer(Grade.Status.CONFIRMED)
          }
          examGrade.setStatus(examStatus)
          examGrade.setExamStatus(new ExamStatus(examStatusId))
          val examMarkStyleId = Params.getInt("markStyleId" + gradeTypeIds(i))
          if (null != examMarkStyleId) {
            examGrade.setMarkStyle(entityDao.get(classOf[ScoreMarkStyle], examMarkStyleId))
          }
          if (examGrade.isTransient) {
            examGrade.setScore(score)
            courseGrade.addExamGrade(examGrade)
          } else {
            examGrade.setScore(score)
          }
        } else {
          if (examGrade.isPersisted) {
            courseGrade.getExamGrades.remove(examGrade)
          }
        }
      }
    }
    val updateGrade = Params.getBoolean("updateGrade")
    if (true == updateGrade) {
      val score = Params.getFloat("courseGrade.score")
      calculator.updateScore(courseGrade, score)
    } else {
      if (null != courseGrade.getLesson) {
        val state = courseGradeService.getState(courseGrade.getLesson)
        calculator.calc(courseGrade, state)
      } else {
        calculator.calc(courseGrade, null)
      }
    }
    entityDao.saveOrUpdate(courseGrade)
  }

  def removeLessonGrade(userId: java.lang.Long): String = {
    val lesson = entityDao.get(classOf[Lesson], Params.getLong("lessonId"))
    var state = courseGradeService.getState(lesson)
    if (null == state) {
      state = new CourseGradeStateBean(lesson)
      entityDao.saveOrUpdate(state)
    }
    val gradeTypeIds = Collections.newSet[Any]
    val gradeTypeId = Params.getInt("gradeTypeId")
    if (null != gradeTypeId) {
      gradeTypeIds.add(gradeTypeId)
    } else {
      val gradeTypeIdSeq = Params.get("gradeTypeIds")
      if (Strings.isNotEmpty(gradeTypeIdSeq)) {
        gradeTypeIds.addAll(Collections.newBuffer[Any](Strings.splitToInt(gradeTypeIdSeq)))
      }
    }
    val gradeTypes = entityDao.get(classOf[GradeType], gradeTypeIds)
    for (gradeType <- gradeTypes if null != state.getState(gradeType) && state.getState(gradeType).isPublished) {
      return "error.grade.modifyPublished"
    }
    if (gradeTypeIds.contains(GradeTypeConstants.GA_ID)) {
      courseGradeService.remove(lesson, entityDao.get(classOf[GradeType], GradeTypeConstants.GA_ID))
    } else {
      for (gradeType <- gradeTypes) {
        courseGradeService.remove(lesson, gradeType)
      }
    }
    null
  }

  def removeStdGrade(): String = {
    val lesson = entityDao.get(classOf[Lesson], Params.getLong("lessonId"))
    var state = courseGradeService.getState(lesson)
    if (null == state) {
      state = new CourseGradeStateBean(lesson)
      entityDao.saveOrUpdate(state)
    }
    if (state.isPublished) {
      return "error.grade.modifyPublished"
    }
    val gradeIds = Strings.splitToLong(Params.get("courseGradeIds"))
    if (null != gradeIds && gradeIds.length != 0) {
      entityDao.remove(entityDao.get(classOf[CourseGrade], gradeIds))
    }
    null
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setBaseCodeService(baseCodeService: BaseCodeService) {
    this.baseCodeService = baseCodeService
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }

  def setGradeRateService(gradeRateService: GradeRateService) {
    this.gradeRateService = gradeRateService
  }
}
