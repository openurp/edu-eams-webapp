package org.openurp.edu.eams.teach.grade.adminclass.web.action


import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.eams.teach.grade.service.GpaService
import org.openurp.edu.eams.teach.grade.service.stat.GradeReportSetting
import org.openurp.edu.eams.teach.grade.service.stat.MultiStdGrade
import org.openurp.edu.eams.teach.grade.service.stat.StdGpaHelper
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class TermAction extends ProjectSupportAction {

  var courseGradeProvider: CourseGradeProvider = _

  var gpaService: GpaService = _

  def index(): String = {
    pushGradeData()
    forward()
  }

  def report(): String = {
    pushGradeData()
    forward()
  }

  private def pushGradeData() {
    val semesterId = getInt("semester.id")
    val setting = new GradeReportSetting()
    populate(setting, "reportSetting")
    if (Strings.isEmpty(setting.getOrder.getProperty)) {
      setting.setOrder(Order.desc("stdGpa.gpa"))
    }
    if (null != setting.gradeType) {
      setting.setGradeType(entityDao.get(classOf[GradeType], setting.gradeType.id))
    }
    if (setting.getPageSize.intValue() < 0) {
      setting.setPageSize(new java.lang.Integer(20))
    }
    val semester = semesterService.getSemester(semesterId)
    var semesters = Collections.singletonList(semester)
    val isSchoolYear = getBool("schoolYear")
    if (isSchoolYear) {
      val builder = OqlBuilder.from(classOf[Semester], "semester")
      builder.where("semester.schoolYear=:schoolYear", semester.getSchoolYear)
      semesters = entityDao.search(builder)
    }
    val adminclass = entityDao.get(classOf[Adminclass], getInt("adminclass.id"))
    val grades = courseGradeProvider.getPublished(adminclass.getStudents, semesters.toArray(Array.ofDim[Semester](semesters.size)))
    val multiStdGrade = new MultiStdGrade(semester, grades, 0.15f)
    StdGpaHelper.statGpa(multiStdGrade, gpaService)
    multiStdGrade.sortStdGrades(setting.getOrder.getProperty, setting.getOrder.isAscending)
    multiStdGrade.setAdminclass(adminclass)
    put("school", getProject.getSchool)
    put("multiStdGrades", Collections.singleton(multiStdGrade))
    put("setting", setting)
    put("FINAL_ID", GradeTypeConstants.FINAL_ID)
    put("adminclass", adminclass)
    put("semester", semester)
    put("semesters", semesters)
  }
}
