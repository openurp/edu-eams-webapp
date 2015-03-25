package org.openurp.edu.eams.teach.schedule.web.action



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.BitStrings
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.beangle.struts2.helper.ContextHelper
import org.beangle.struts2.helper.Params
import org.openurp.edu.eams.base.Building
import org.openurp.base.Semester
import org.openurp.base.TimeSetting
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.schedule.service.propertyExtractor.SchedulePropertyExtractor
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class ScheduleSearchAction extends SemesterSupportAction {

  var lessonService: LessonService = _

  var timeSettingService: TimeSettingService = _

  var lessonSearchHelper: LessonSearchHelper = _

  protected override def indexSetting() {
    val semesterId = getInt("semester.id")
    if (semesterId != null) ContextHelper.put("semester", entityDao.get(classOf[Semester], semesterId))
    val project = getProject
    put("courseTypes", lessonService.courseTypesOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      getAttribute("semester").asInstanceOf[Semester]))
    put("teachDepartList", lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      getAttribute("semester").asInstanceOf[Semester]))
    put("stdTypeList", getStdTypes)
    addBaseCode("languages", classOf[TeachLangType])
    put("campuses", project.getCampuses)
    put("weeks", WeekDays.All)
    var num = 0
    if (null != 
      timeSettingService.getClosestTimeSetting(project, getAttribute("semester").asInstanceOf[Semester], 
      null)) {
      val timeSetting = timeSettingService.getClosestTimeSetting(project, getAttribute("semester").asInstanceOf[Semester], 
        null).asInstanceOf[org.openurp.base.TimeSetting]
      if (null != timeSetting && null != timeSetting.getDefaultUnits) {
        num = timeSetting.getDefaultUnits.size
      }
    }
    put("buildings", baseInfoService.getBaseInfos(classOf[Building]))
    put("units", num)
  }

  override def search(): String = {
    val lessons = entityDao.search(getQueryBuilder)
    put("teacherIsNull", getBool("fake.teacher.null"))
    put("lessons", lessons)
    val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
    val arrangeInfo = CollectUtils.newHashMap()
    for (oneTask <- lessons) {
      arrangeInfo.put(oneTask.id.toString, digestor.digest(getTextResource, oneTask, ":day :units :weeks :room"))
    }
    put("arrangeInfo", arrangeInfo)
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[Lesson] = {
    val builder = lessonSearchHelper.buildQuery()
    val std = getLoginStudent
    if (std != null) {
      builder.where("lesson.teachDepart=:department", std.majorDepart)
    } else {
      builder.where("lesson.teachDepart in (:departments)", getDeparts)
    }
    if (Strings.isEmpty(get(Order.ORDER_STR))) builder.orderBy("lesson.no")
    builder
  }

  protected def getPropertyExtractor(): PropertyExtractor = {
    val weekday = Params.getInt("fake.time.day")
    var courseUnit = Params.getInt("courseActivity.time.startUnit")
    var activityWeekStart = Params.getInt("fake.time.weekstart")
    var activityWeekEnd = Params.getInt("fake.time.weekend")
    var activityWeekState: java.lang.Long = null
    if (null != activityWeekStart || null != activityWeekEnd) {
      if (null == activityWeekStart) activityWeekStart = activityWeekEnd
      if (null == activityWeekEnd) activityWeekEnd = activityWeekStart
      if (activityWeekEnd >= activityWeekStart && activityWeekEnd < 52 && 
        activityWeekStart > 0) {
        val sb = new StringBuilder(Strings.repeat("0", 53))
        var i = activityWeekStart
        while (i <= activityWeekEnd) {sb.setCharAt(i, '1')i += 1
        }
        activityWeekState = BitStrings.binValueOf(sb.toString)
      }
    }
    if (null == courseUnit) courseUnit = Params.getInt("fake.time.unit")
    val extrator = new SchedulePropertyExtractor(getTextResource, entityDao)
    extrator.setWeekday(weekday)
    extrator.setWeekState(activityWeekState)
    extrator.setCourseUnit(courseUnit)
    extrator.setBuildingId(getLong("fake.building.id"))
    extrator
  }
}
