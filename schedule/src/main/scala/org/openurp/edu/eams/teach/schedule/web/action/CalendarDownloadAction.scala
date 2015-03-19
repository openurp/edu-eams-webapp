package org.openurp.edu.eams.teach.schedule.web.action

import java.io.File
import org.beangle.commons.lang.Strings
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.util.DownloadHelper



class CalendarDownloadAction extends SemesterSupportAction {

  def index(): String = {
    val lessonId = getLongId("lesson")
    if (lessonId == null) {
      return forwardError("没有教学任务")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    var displayName = get("display")
    if (Strings.isEmpty(displayName)) {
      displayName = lesson.getNo + "  " + lesson.getCourse.getName + " 教学日历"
    }
    val path = getConfig.get("teach.calendarFilePath").asInstanceOf[String]
    if (null == path || !new File(path).exists()) {
      put("path", "")
      return "errorpath"
    }
    val file = new File(Strings.concat(path, File.separator, lesson.getSemester.id.toString, File.separator, 
      lesson.getNo, ".doc"))
    if (!file.exists()) {
      "errorpath"
    } else {
      DownloadHelper.download(getRequest, getResponse, file, displayName)
      null
    }
  }
}
