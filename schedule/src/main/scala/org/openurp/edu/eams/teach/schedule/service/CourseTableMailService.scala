package org.openurp.edu.eams.teach.schedule.service

import org.openurp.edu.eams.teach.schedule.model.CourseArrangeAlteration
import org.openurp.edu.eams.teach.schedule.model.CourseMailSetting



trait CourseTableMailService {

  def sendCourseTableChangeMsg(courseArrangeAlteration: CourseArrangeAlteration, courseMailSetting: CourseMailSetting, userIds: Array[java.lang.Long]): String

  def sendCourseTableChangeMsgToTeacher(courseArrangeAlteration: CourseArrangeAlteration, courseMailSetting: CourseMailSetting): String

  def sendCourseTableChangeMsgToStd(courseArrangeAlteration: CourseArrangeAlteration, courseMailSetting: CourseMailSetting): String
}
