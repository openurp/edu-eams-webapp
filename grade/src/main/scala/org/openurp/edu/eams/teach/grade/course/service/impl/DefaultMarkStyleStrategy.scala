package org.openurp.edu.eams.teach.grade.course.service.impl

import java.util.List
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.entity.metadata.Model
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.course.service.MarkStyleStrategy
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants

import scala.collection.JavaConversions._

class DefaultMarkStyleStrategy extends MarkStyleStrategy() {

  private var entityDao: EntityDao = _

  private var settings: CourseGradeSettings = _

  private def isDefault(style: ScoreMarkStyle): Boolean = {
    null == style || style.getId == ScoreMarkStyle.PERCENT
  }

  def configMarkStyle(gradeState: CourseGradeState, gradeTypes: List[GradeType]) {
    val setting = settings.getSetting(gradeState.getLesson.getProject)
    if (isDefault(gradeState.getScoreMarkStyle)) gradeState.setScoreMarkStyle(getDefaultCourseGradeMarkStyle(gradeState, 
      setting))
    for (`type` <- gradeTypes) {
      val typeState = getState(gradeState, `type`)
      if (null == typeState.getScoreMarkStyle) {
        typeState.setScoreMarkStyle(getDefaultExamGradeMarkStyle(typeState, setting))
      }
    }
    entityDao.saveOrUpdate(gradeState)
  }

  protected def getDefaultCourseGradeMarkStyle(state: CourseGradeState, setting: CourseGradeSetting): ScoreMarkStyle = {
    var defaultMarkStyle = state.getLesson.getCourse.getMarkStyle
    if (null == defaultMarkStyle) defaultMarkStyle = entityDao.get(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT)
    defaultMarkStyle
  }

  protected def getDefaultExamGradeMarkStyle(typeState: ExamGradeState, setting: CourseGradeSetting): ScoreMarkStyle = {
    if (setting.getFinalCandinateTypes.contains(typeState.gradeType)) {
      typeState.gradeState.getScoreMarkStyle
    } else {
      if (typeState.gradeType.getId == GradeTypeConstants.DELAY_ID) {
        val endGradeState = typeState.gradeState.getState(new GradeType(GradeTypeConstants.END_ID))
        if (null == endGradeState) typeState.gradeState.getScoreMarkStyle else endGradeState.getScoreMarkStyle
      } else {
        entityDao.get(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT)
      }
    }
  }

  private def getState(gradeState: CourseGradeState, gradeType: GradeType): ExamGradeState = {
    var gradeTypeState = gradeState.getState(gradeType)
    if (null == gradeTypeState) {
      gradeTypeState = Model.newInstance(classOf[ExamGradeState]).asInstanceOf[ExamGradeState]
      gradeTypeState.setGradeType(gradeType)
      gradeTypeState.setGradeState(gradeState)
      gradeState.getStates.add(gradeTypeState)
    }
    gradeTypeState
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setSettings(settings: CourseGradeSettings) {
    this.settings = settings
  }
}
