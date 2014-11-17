package org.openurp.eams.grade.model

import org.openurp.eams.grade.{ CourseGradeState, ExamGradeState, GaGradeState, GradeState }
import org.openurp.teach.code.{ GradeType, ScoreMarkStyle }
import org.openurp.teach.code.model.{ GradeTypeBean, ScoreMarkStyleBean }
import org.openurp.teach.lesson.Lesson
import org.openurp.eams.grade.domain.AbstractGradeState
import java.lang.{ Short => JShort }

/**
 * 成绩状态表
 * 记录了对应教学任务成绩<br>
 * 1)记录方式,<br>
 * 2)各种成绩成分的百分比,<br>
 * 3)各种成绩的确认状态,<br>
 * 4)各种成绩的发布状态<br>
 */
class CourseGradeStateBean extends AbstractGradeState with CourseGradeState {

  /**
   * 教学任务
   */
  var lesson: Lesson = _

  /**
   * 可录入各成绩类型的状态设置
   */
  var examStates: collection.mutable.Set[ExamGradeState] = new collection.mutable.HashSet[ExamGradeState]

  /**
   * 可录入各成绩类型的状态设置
   */
  var gaStates: collection.mutable.Set[GaGradeState] = new collection.mutable.HashSet[GaGradeState]


  
  def this(lesson: Lesson) {
    this()
    this.lesson = lesson
    this.scoreMarkStyle = new ScoreMarkStyleBean(ScoreMarkStyle.Percent)
  }

  def updateStatus(gradeType: GradeType, status: Int) {
    val state = getState(gradeType).asInstanceOf[ExamGradeStateBean]
    if (null == state) {
      if (gradeType.isGa) {
        val newstate = new GaGradeStateBean
        newstate.gradeState = this
        newstate.gradeType = gradeType
        newstate.status = status
        newstate.scoreMarkStyle = scoreMarkStyle
        gaStates += newstate
      } else {
        val newstate = new ExamGradeStateBean
        newstate.gradeState = this
        newstate.gradeType = gradeType
        newstate.status = status
        newstate.scoreMarkStyle = scoreMarkStyle
        examStates += newstate
      }
    } else {
      state.status = status
    }
  }

  def getState(gradeType: GradeType): GradeState = {
    if (gradeType.isGa)
      gaStates.find(_.gradeType.id == gradeType.id).getOrElse(null)
    else
      examStates.find(_.gradeType.id == gradeType.id).getOrElse(null)
  }

  /**
   * 是否是指定状态
   */
  def isStatus(gradeType: GradeType, status: Int): Boolean = {
    val gradeTypeState = getState(gradeType)
    if (null == gradeTypeState) false else gradeTypeState.status == status
  }

  def getPercent(gradeType: GradeType): JShort = {
    examStates.find(_.gradeType.id == gradeType.id).map(_.percent).orNull
  }

  def gradeType: GradeType = {
    new GradeTypeBean(GradeType.Final)
  }
}
