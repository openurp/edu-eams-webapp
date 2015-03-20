package org.openurp.edu.eams.teach.election.web.action.courseTake


import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.struts2.helper.Params
import org.openurp.edu.eams.base.CourseUnit
import org.openurp.base.Semester
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.election.service.CourseTakeService
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.helper.AdminclassSearchHelper



class CourseTakeForTeacherAction extends SemesterSupportAction {

  private var adminclassSearchHelper: AdminclassSearchHelper = _

  private var courseTakeService: CourseTakeService = _

  private var timeSettingService: TimeSettingService = _

  protected def indexSetting() {
    val teacher = getLoginTeacher
    if (null != teacher) {
      val adminclasses = entityDao.search(adminclassSearchHelper.buildQuery(teacher))
      put("adminclasses", adminclasses)
      put("weeks", putSemester(null).getWeeks)
      put("teacher", teacher)
    }
  }

  def search(): String = {
    val teacher = getLoginTeacher
    if (null != teacher) {
      var courseTakes: List[CourseTake] = null
      val semester = putSemester(null)
      val adminclassIds = getAdminclassIds
      if (ArrayUtils.isEmpty(adminclassIds)) {
        val adminclasses = entityDao.search(adminclassSearchHelper.buildQuery(teacher))
        courseTakes = courseTakeService.getCourseTakesByAdminclass(semester, populateWeekCondition(), 
          getProject, adminclasses)
      } else {
        courseTakes = courseTakeService.getCourseTakesByAdminclassId(semester, populateWeekCondition(), 
          getProject, adminclassIds)
      }
      val units = CollectUtils.newArrayList(timeSettingService.getClosestTimeSetting(getProject, semester, 
        null)
        .getDefaultUnits
        .values)
      put("units", units)
      put("weekDays", WeekDays.All)
      if (!courseTakes.isEmpty) {
        put("courseTable", courseTakeService.getCourseTable(courseTakes, units))
      }
      put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY.toString).asInstanceOf[String]))
      put("courseTakes", courseTakes)
      put("teacher", teacher)
    }
    forward()
  }

  private def getAdminclassIds(): Array[Integer] = {
    val adminclassIdSeq = Params.getParams.get("courseTake.std.adminclass.id").asInstanceOf[Array[String]]
    var adminclassIds: Array[Integer] = null
    if (ArrayUtils.isNotEmpty(adminclassIdSeq)) {
      val ids = CollectUtils.newArrayList()
      for (idStr <- adminclassIdSeq if idStr != "") {
        ids.add(java.lang.Long.parseLong(idStr))
      }
      adminclassIds = ids.toArray(Array.ofDim[Integer](0))
    }
    adminclassIds
  }

  private def populateWeekCondition(): Condition = {
    val weekSeq = Params.getParams.get("courseTake.std.adminclass.id").asInstanceOf[Array[String]]
    var condition: Condition = null
    if (ArrayUtils.isNotEmpty(weekSeq)) {
      val weekList = CollectUtils.newArrayList()
      for (week <- weekSeq if week != "") {
        weekList.add(java.lang.Integer.parseInt(week))
      }
      if (weekList.isEmpty) return null
      val builder = new StringBuilder()
      for (i <- 0 until weekList.size) {
        builder.append("courseTake.lesson.courseSchedule.startWeek <=:week" + 
          i + 
          " and courseTake.lesson.courseSchedule.endWeek >=:week" + 
          i)
      }
      condition = new Condition(builder.toString)
      condition.params(weekList)
    }
    condition
  }

  def setCourseTakeService(courseTakeService: CourseTakeService) {
    this.courseTakeService = courseTakeService
  }

  def setAdminclassSearchHelper(adminclassSearchHelper: AdminclassSearchHelper) {
    this.adminclassSearchHelper = adminclassSearchHelper
  }

  def setTimeSettingService(timeSettingService: TimeSettingService) {
    this.timeSettingService = timeSettingService
  }
}
