package org.openurp.eams.grade

import org.beangle.data.model.TemporalOn
import org.openurp.teach.code.GradeType
import org.openurp.teach.lesson.Lesson
import java.lang.{ Short => JShort }
/**
 * 课程成绩成绩状态
 *
 * @author chaostone
 */
trait CourseGradeState extends GradeState {

  /**
   * 教学任务
   *
   * @return
   */
  def lesson: Lesson

  /**
   * 是否为指定状态
   *
   * @param gradeType
   */
  def isStatus(gradeType: GradeType, status: Int): Boolean

  /**
   * 更新状态
   */
  def updateStatus(gradeType: GradeType, status: Int): Unit

  /**
   * 返回指定成绩类型的成绩状态
   */
  def getState(gradeType: GradeType): GradeState

  /**
   * 所有成绩状态
   */
  def examStates: collection.Set[ExamGradeState]

  /**
   * 所有成绩状态
   */
  def gaStates: collection.Set[GaGradeState]

  def getPercent(gradeType: GradeType): JShort

}
