package org.openurp.edu.eams.grade.model

import org.openurp.edu.eams.grade.{ CourseGradeState, ExamGradeState }
import org.openurp.teach.code.{ GradeType, ScoreMarkStyle }
import org.openurp.teach.code.model.{ GradeTypeBean, ScoreMarkStyleBean }
import org.openurp.teach.lesson.Lesson
import org.openurp.edu.eams.grade.domain.AbstractGradeState

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
  var states: collection.Set[ExamGradeState] = new collection.mutable.HashSet[ExamGradeState]

  /**
   * 其他录入人
   */
  var extraInputer: String = _

  def this(lesson: Lesson) {
    this()
    this.lesson = lesson
    this.scoreMarkStyle = new ScoreMarkStyleBean(ScoreMarkStyle.Percent)
  }

  def updateStatus(gradeType: GradeType, status: Int) {
    val state = getState(gradeType).asInstanceOf[ExamGradeStateBean]
    if (null == state) {
      val newstate = new ExamGradeStateBean
      newstate.gradeState = this
      newstate.gradeType = gradeType
      newstate.status = status
      newstate.scoreMarkStyle = scoreMarkStyle
      states += newstate
    } else {
      state.status = status
    }
  }

  def getState(gradeType: GradeType): ExamGradeState = {
    states.find(_.gradeType.id == gradeType.id).getOrElse(null)
  }

  /**
   * 是否是指定状态
   */
  def isStatus(gradeType: GradeType, status: Int): Boolean = {
    val gradeTypeState = getState(gradeType)
    if (null == gradeTypeState) false else gradeTypeState.status == status
  }

  def getPercent(gradeType: GradeType): java.lang.Float = {
    var iter = states.iterator
    while (iter.hasNext) {
      val gradeTypeState = iter.next().asInstanceOf[ExamGradeState]
      if (null != gradeType &&
        gradeTypeState.gradeType.id == gradeType.id) {
        return gradeTypeState.percent
      }
    }
    null
  }

  def gradeType: GradeType = {
    new GradeTypeBean(GradeType.Final)
  }
}
