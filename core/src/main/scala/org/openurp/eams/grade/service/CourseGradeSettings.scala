package org.openurp.eams.grade.service

import org.openurp.eams.grade.domain.CourseGradeSetting
import org.openurp.teach.Project

/**
 * 课程成绩设置服务
 *
 * @author chaostone
 */
trait CourseGradeSettings {

  /**
   * 查询课程成绩设置
   *
   * @param project
   * @return
   */
  def getSetting(project: Project): CourseGradeSetting
}
