package org.openurp.eams.grade.service

import org.openurp.teach.code.GradeType
import org.openurp.eams.grade.CourseGradeState
import org.openurp.teach.lesson.Lesson

trait CourseGradeService {

  /**
   * 按照成绩状态，重新计算成绩的<br>
   * 1、首先更改成绩的成绩记录方式<br>
   * 2、score以及是否通过和绩点等项<br>
   * 3、如果成绩状态中发布状态，则进行发布操作
   *
   * @param gradeState
   * @return
   */
  def recalculate(gradeState: CourseGradeState): Unit

  /**
   * 删除考试成绩<br>
   * 同时将该成绩和总评成绩的教师确认位置为0
   *
   * @param task
   * @param gradeType
   */
  def remove(task: Lesson, gradeType: GradeType): Unit

  /**
   * 发布或取消发布成绩
   *
   * @param lessonIdSeq
   * @param gradeType
   *            如果为空,则发布影响总评和最终
   * @param isPublished
   */
  def publish(lessonIds: Array[Integer], gradeTypes: Array[GradeType], isPublished: Boolean): Unit

  /**
   * 查询成绩状态
   *
   * @param lesson
   * @return
   */
  def getState(lesson: Lesson): CourseGradeState
}
