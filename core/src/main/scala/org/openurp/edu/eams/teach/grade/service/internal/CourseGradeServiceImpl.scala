package org.openurp.edu.eams.teach.grade.service.internal

import org.openurp.edu.eams.teach.Grade.Status.CONFIRMED
import org.openurp.edu.eams.teach.Grade.Status.NEW
import org.openurp.edu.eams.teach.Grade.Status.PUBLISHED
import java.util.Collection
import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradePublishStack
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.eams.teach.grade.service.GradeCourseTypeProvider
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGrade
import org.openurp.edu.eams.teach.lesson.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class CourseGradeServiceImpl extends BaseServiceImpl with CourseGradeService {

  protected var calculator: CourseGradeCalculator = _

  protected var gradeCourseTypeProvider: GradeCourseTypeProvider = _

  protected var publishStack: CourseGradePublishStack = _

  protected var settings: CourseGradeSettings = _

  private def getGrades(lesson: Lesson): List[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    query.where("courseGrade.lesson = :lesson", lesson)
    entityDao.search(query)
  }

  def getState(lesson: Lesson): CourseGradeState = {
    val list = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    if (list.isEmpty) {
      return null
    }
    list.get(0)
  }

  def publish(lessonIdSeq: String, gradeTypes: Array[GradeType], isPublished: Boolean) {
    val lessons = entityDao.get(classOf[Lesson], Strings.transformToLong(lessonIdSeq.split(",")))
    if (CollectUtils.isNotEmpty(lessons)) {
      val setting = settings.getSetting(lessons.get(0).getProject)
      if (setting.isSubmitIsPublish) {
        for (lesson <- lessons) {
          updateState(lesson, gradeTypes, if (isPublished) PUBLISHED else NEW)
        }
      } else {
        for (lesson <- lessons) {
          updateState(lesson, gradeTypes, if (isPublished) PUBLISHED else CONFIRMED)
        }
      }
    }
  }

  def recalculate(gradeState: CourseGradeState) {
    if (null == gradeState) {
      return
    }
    val published = CollectUtils.newArrayList()
    for (egs <- gradeState.getStates if egs.getStatus == PUBLISHED) published.add(egs.gradeType)
    val grades = getGrades(gradeState.getLesson)
    for (grade <- grades) {
      updateGradeState(grade, gradeState)
      for (state <- gradeState.getStates) {
        val gradeType = state.gradeType
        val examGrade = grade.getExamGrade(gradeType)
        val examState = gradeState.getState(gradeType)
        updateGradeState(examGrade, examState)
      }
      calculator.calc(grade, gradeState)
    }
    entityDao.saveOrUpdate(grades)
    if (!published.isEmpty) publish(gradeState.getLesson.getId.toString, published.toArray(Array.ofDim[GradeType](published.size)), 
      true)
  }

  def remove(lesson: Lesson, gradeType: GradeType) {
    val state = getState(lesson)
    val courseGrades = entityDao.get(classOf[CourseGrade], "lesson", lesson)
    val gradeSetting = settings.getSetting(lesson.getProject)
    val save = CollectUtils.newArrayList()
    val remove = CollectUtils.newArrayList()
    for (courseGrade <- courseGrades) {
      if (GradeTypeConstants.FINAL_ID == gradeType.getId) {
        if (NEW == courseGrade.getStatus) remove.add(courseGrade)
      } else if (GradeTypeConstants.GA_ID == gradeType.getId) {
        for (`type` <- gradeSetting.getGaElementTypes) {
          val exam = courseGrade.getExamGrade(`type`)
          if (null != exam && NEW == exam.getStatus) courseGrade.getExamGrades.remove(exam)
        }
        val ga = courseGrade.getExamGrade(gradeType)
        if (null != ga && NEW == ga.getStatus) courseGrade.getExamGrades.remove(ga)
        if (CollectUtils.isNotEmpty(courseGrade.getExamGrades)) {
          calculator.calc(courseGrade, state)
          save.add(courseGrade)
        } else {
          remove.add(courseGrade)
        }
      } else {
        val examGrade = courseGrade.getExamGrade(gradeType)
        if (null == examGrade || NEW != examGrade.getStatus) //continue
        courseGrade.getExamGrades.remove(examGrade)
        if (CollectUtils.isNotEmpty(courseGrade.getExamGrades)) {
          calculator.calc(courseGrade, state)
          save.add(courseGrade)
        } else {
          remove.add(courseGrade)
        }
      }
    }
    if (null != state) {
      state.setAuditReason(null)
      state.setAuditStatus(null)
      if (GradeTypeConstants.FINAL_ID == gradeType.getId) {
        state.setStatus(NEW)
        state.getStates.clear()
      } else {
        if (GradeTypeConstants.GA_ID == gradeType.getId) {
          state.setStatus(NEW)
          for (`type` <- gradeSetting.getGaElementTypes) {
            state.getStates.remove(state.getState(`type`))
          }
        }
        state.getStates.remove(state.getState(gradeType))
      }
      if (state.getStates.isEmpty) {
        remove.add(state)
      } else {
        save.add(state)
      }
    }
    entityDao.execute(Operation.saveOrUpdate(save).remove(remove))
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }

  def setCourseGradePublishStack(courseGradePublishStack: CourseGradePublishStack) {
    this.publishStack = courseGradePublishStack
  }

  def setGradeCourseTypeProvider(gradeCourseTypeProvider: GradeCourseTypeProvider) {
    this.gradeCourseTypeProvider = gradeCourseTypeProvider
  }

  private def updateGradeState(grade: Grade, state: GradeState) {
    if (null != grade && null != state) {
      grade.setMarkStyle(state.getScoreMarkStyle)
      grade.setStatus(state.getStatus)
    }
  }

  private def updateState(lesson: Lesson, gradeTypes: Array[GradeType], status: Int) {
    val courseGradeStates = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    var gradeState: CourseGradeState = null
    for (gradeType <- gradeTypes) {
      gradeState = if (courseGradeStates.isEmpty) Model.newInstance(classOf[CourseGradeState]) else courseGradeStates.get(0)
      if (gradeType.getId == GradeTypeConstants.FINAL_ID) {
        gradeState.setStatus(status)
      } else {
        gradeState.updateStatus(gradeType, status)
      }
    }
    val grades = entityDao.get(classOf[CourseGrade], "lesson", lesson)
    val toBeSaved = CollectUtils.newArrayList()
    val published = CollectUtils.newHashSet()
    for (grade <- grades; gradeType <- gradeTypes) {
      if (gradeType.getId == GradeTypeConstants.FINAL_ID) {
        grade.setStatus(status)
      } else {
        val examGrade = grade.getExamGrade(gradeType)
        if (null != examGrade) {
          examGrade.setStatus(status)
          published.add(grade)
        }
      }
    }
    if (status == PUBLISHED) toBeSaved.addAll(publishStack.onPublish(published, gradeState, gradeTypes))
    toBeSaved.addAll(Operation.saveOrUpdate(lesson, gradeState).saveOrUpdate(published)
      .build())
    entityDao.execute(toBeSaved.toArray(Array.ofDim[Operation](toBeSaved.size)))
  }

  def setSettings(settings: CourseGradeSettings) {
    this.settings = settings
  }
}
