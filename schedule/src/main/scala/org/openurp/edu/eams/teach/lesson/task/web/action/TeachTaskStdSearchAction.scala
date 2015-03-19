package org.openurp.edu.eams.teach.lesson.task.web.action

import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student



class TeachTaskStdSearchAction extends TeachTaskSearchAction {

  def index(): String = {
    val std = getLoginStudent
    if (null == std) {
      return forwardError("error.std.stdNo.needed")
    }
    val semesterId = getInt("semester.id")
    val projectId = getInt("project.id")
    val project = (if (null == projectId) std.getProject else entityDao.get(classOf[Project], projectId))
    var semester: Semester = null
    semester = if (null != semesterId && null == projectId) semesterService.getSemester(semesterId) else semesterService.getCurSemester(project)
    if (null == semester) {
      return forwardError("error.semester.notMaded")
    }
    put("semester", semester)
    put("cur_project", project)
    put("semesters", semesterService.getCalendar(project).getSemesters)
    put("departmentAll", departmentService.getColleges)
    forward()
  }
}
