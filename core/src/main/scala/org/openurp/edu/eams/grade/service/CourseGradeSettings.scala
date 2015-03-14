package org.openurp.edu.eams.grade.service

import org.openurp.edu.eams.grade.domain.CourseGradeSetting
import org.openurp.teach.core.Project

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
