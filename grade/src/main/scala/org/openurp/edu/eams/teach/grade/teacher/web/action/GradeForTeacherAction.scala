package org.openurp.edu.eams.teach.grade.teacher.web.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.helper.AdminclassSearchHelper



class GradeForTeacherAction extends SemesterSupportAction {

  private var adminclassSearchHelper: AdminclassSearchHelper = _

  private var courseGradeService: CourseGradeService = _

  override def index(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("没有权限")
    }
    put("adminclasses", entityDao.search(adminclassSearchHelper.buildQuery(teacher)))
    forward()
  }

  override def search(): String = {
    val adminclassId = getIntId("adminclass")
    if (null == adminclassId) {
      return forwardError("error.parameters.needed")
    }
    val adminclass = entityDao.get(classOf[Adminclass], adminclassId)
    putSemester(getProject)
    put("adminclass", adminclass)
    forward()
  }

  override def info(): String = {
    val semesterId = getIntId("semester")
    val studentId = getLongId("student")
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std.id = :studentId", studentId)
      .where("courseGrade.semester.id=:semesterId", semesterId)
      .where("courseGrade.project = :project", getProject)
    val courseGrades = entityDao.search(builder)
    put("student", entityDao.get(classOf[Student], studentId))
    put("courseGrades", courseGrades)
    put("courseGradeAlterMap", Collections.emptyMap())
    put("courseGradeStateMap", Collections.emptyMap())
    put("examGradeAlterMap", Collections.emptyMap())
    forward()
  }

  def setAdminclassSearchHelper(adminclassSearchHelper: AdminclassSearchHelper) {
    this.adminclassSearchHelper = adminclassSearchHelper
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }
}
