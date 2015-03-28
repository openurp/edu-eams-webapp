package org.openurp.edu.eams.teach.grade.course.web.action




import org.beangle.commons.lang.Strings
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.eams.teach.grade.service.GpaService
import org.openurp.edu.eams.teach.grade.service.stat.GradeReportSetting
import org.openurp.edu.eams.teach.grade.service.stat.MultiStdGrade
import org.openurp.edu.eams.teach.grade.service.stat.StdGpaHelper
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.helper.BaseInfoSearchHelper



class MultiStdReportAction extends SemesterSupportAction {

  var baseInfoSearchHelper: BaseInfoSearchHelper = _

  var courseGradeService: CourseGradeService = _

  var gpaService: GpaService = _

  var settings: CourseGradeSettings = _

  def adminClassList(): String = {
    put("adminclasses", baseInfoSearchHelper.searchAdminclass())
    val builder = OqlBuilder.from(classOf[GradeType], "gradeType").where("gradeType in (:gradeType)", 
      settings.getSetting(getProject).getPublishableTypes)
    put("gradeTypes", entityDao.search(builder))
    putSemester(getProject)
    forward()
  }

  def classGradeReport(): String = {
    val semesterId = getInt("semester.id")
    val semester = semesterService.getSemester(semesterId)
    val setting = new GradeReportSetting()
    populate(setting, "reportSetting")
    if (Strings.isEmpty(setting.getOrder.getProperty)) {
      setting.setOrder(Order.desc("stdGpa.gpa"))
    }
    if (null != setting.gradeType) {
      setting.setGradeType(entityDao.get(classOf[GradeType], setting.gradeType.id))
    }
    if (setting.getPageSize == null || setting.getPageSize.intValue() < 0) {
      setting.setPageSize(new java.lang.Integer(20))
    }
    var ratio = getFloat("ratio")
    if (null == ratio || ratio.floatValue() < 0 || ratio.floatValue() >= 1) {
      ratio = new java.lang.Float(0.15)
    }
    var adminclassIds = get("adminclassIds")
    if (Strings.isBlank(adminclassIds)) {
      adminclassIds = get("adminclass.ids")
    }
    val adminclasses = entityDao.get(classOf[Adminclass], Strings.splitToInt(adminclassIds))
    val multiStdGrades = Collections.newBuffer[Any]
    for (adminclass <- adminclasses) {
      val grades = getCourseGrades(semester, adminclass.getStudents)
      val multiStdGrade = new MultiStdGrade(semester, grades, ratio)
      StdGpaHelper.statGpa(multiStdGrade, gpaService)
      multiStdGrade.sortStdGrades(setting.getOrder.getProperty, setting.getOrder.isAscending)
      multiStdGrade.setAdminclass(adminclass)
      multiStdGrades.add(multiStdGrade)
    }
    put("setting", setting)
    val orders = Order.parse(get("orderBy"))
    if (Collections.isNotEmpty(orders)) {
      val order = orders.get(0)
      if (Strings.isNotBlank(order.getProperty) && order.getProperty != "null") {
        Collections.sort(multiStdGrades, new PropertyComparator(order.getProperty, order.isAscending))
      }
    }
    put("school", getProject.getSchool)
    put("multiStdGrades", multiStdGrades)
    put("FINAL_ID", GradeTypeConstants.FINAL_ID)
    forward()
  }

  private def getCourseGrades(semester: Semester, stds: Iterable[Student]): Map[Student, List[CourseGrade]] = {
    val gradeMap = Collections.newMap[Any]
    for (std <- stds) {
      gradeMap.put(std, new ArrayList[CourseGrade]())
    }
    if (stds.isEmpty) return gradeMap
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.semester = :semesterId", semester)
    query.where("grade.std in (:stds)", stds)
    val allGrades = entityDao.search(query)
    for (g <- allGrades) gradeMap.get(g.getStd).add(g)
    gradeMap
  }
}
