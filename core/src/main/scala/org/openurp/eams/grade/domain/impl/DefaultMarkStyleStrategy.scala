package org.openurp.eams.grade.domain.impl

import org.openurp.eams.grade.service.CourseGradeSettings
import org.openurp.eams.grade.ExamGradeState
import org.openurp.eams.grade.domain.MarkStyleStrategy
import org.springframework.ui.Model
import org.beangle.data.model.dao.EntityDao
import org.openurp.eams.grade.CourseGradeState
import org.openurp.eams.grade.model.{ CourseGradeStateBean, ExamGradeStateBean, GaGradeStateBean }
import org.openurp.eams.grade.domain.CourseGradeSetting
import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.model.GradeTypeBean
import org.openurp.eams.grade.GradeState
import org.openurp.eams.grade.GaGradeState

/**
 * 默认成绩记录方式配置方法
 * @author chaostone
 *
 */
class DefaultMarkStyleStrategy extends MarkStyleStrategy {

  var entityDao: EntityDao = _

  var settings: CourseGradeSettings = _

  private def isDefault(style: ScoreMarkStyle): Boolean = {
    null == style || style.id == ScoreMarkStyle.Percent
  }

  def configMarkStyle(gs: CourseGradeState, gradeTypes: Seq[GradeType]) {
    val gradeState = gs.asInstanceOf[CourseGradeStateBean]
    val setting = settings.getSetting(gradeState.lesson.project)
    if (isDefault(gradeState.scoreMarkStyle))
      gradeState.scoreMarkStyle = getDefaultCourseGradeMarkStyle(gradeState, setting)
    for (`type` <- gradeTypes) {
      val typeState = getState(gradeState, `type`)
      if (null == typeState.scoreMarkStyle) {
        typeState.scoreMarkStyle = getDefaultExamGradeMarkStyle(typeState, setting)
      }
    }
    entityDao.saveOrUpdate(gradeState)
  }

  /**
   * 查询缺省的总成绩记录方式
   *
   * @param state
   * @param setting
   * @return
   */
  protected def getDefaultCourseGradeMarkStyle(state: CourseGradeState, setting: CourseGradeSetting): ScoreMarkStyle = {
    var defaultMarkStyle = state.lesson.course.markStyle
    if (null == defaultMarkStyle) defaultMarkStyle = entityDao.get(classOf[ScoreMarkStyle], ScoreMarkStyle.Percent)
    defaultMarkStyle
  }

  /**
   * 查询缺省的考试成绩类型对应的记录方式
   *
   * @param typeState
   * @param setting
   * @return
   */
  protected def getDefaultExamGradeMarkStyle(state: GradeState, setting: CourseGradeSetting): ScoreMarkStyle = {
    if (state.gradeType.isGa) {
      state.asInstanceOf[GaGradeState].gradeState.scoreMarkStyle
    } else {
      val typeState=state.asInstanceOf[ExamGradeState]
      if (typeState.gradeType.id == GradeType.Delay) {
        val endGradeState = typeState.gradeState.getState(new GradeTypeBean(GradeType.End))
        if (null == endGradeState) typeState.gradeState.scoreMarkStyle else endGradeState.scoreMarkStyle
      } else {
        entityDao.get(classOf[ScoreMarkStyle], Integer.valueOf(ScoreMarkStyle.Percent))
      }
    }
  }

  private def getState(gradeState: CourseGradeStateBean, gradeType: GradeType): GradeState = {
    var gradeTypeState = gradeState.getState(gradeType)
    if (null == gradeTypeState) {
      if (gradeType.isGa) {
        val gaState = new GaGradeStateBean
        gaState.gradeType = gradeType
        gaState.gradeState = gradeState
        gradeState.gaStates += gaState
        gradeTypeState = gaState
      } else {
        val examState = new ExamGradeStateBean
        examState.gradeType = gradeType
        examState.gradeState = gradeState
        gradeState.examStates += examState
       gradeTypeState = examState
      }
    }
    gradeTypeState
  }
}