package org.openurp.edu.eams.teach.schedule.web.action





import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Predicate
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Room
import org.openurp.edu.eams.base.CourseUnit
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.base.TimeSetting
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.model.TeacherBean
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.eams.teach.code.school.CourseCategory
import org.openurp.edu.eams.teach.lesson.ArrangeSuggest
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.SuggestActivity
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.model.SuggestActivityBean
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.util.SuggestActivityDigestor
import org.openurp.edu.eams.teach.schedule.json.ArrangeSuggestGsonBuilder
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



class ArrangeSuggestAction extends SemesterSupportAction {

  var lessonSearchHelper: LessonSearchHelper = _

  var timeSettingService: TimeSettingService = _

  var lessonService: LessonService = _

  protected override def indexSetting() {
    val project = getProject
    val semester = putSemester(null)
    val teachDeparts = CollectUtils.newArrayList()
    val departs = getTeachDeparts
    for (department <- getDeparts if departs.contains(department)) {
      teachDeparts.add(department)
    }
    if (semester != null) {
      put("attendDeparts", getCollegeOfDeparts)
      put("teachDeparts", teachDeparts)
      put("courseTypes", lessonService.courseTypesOfSemester(Collections.singletonList(project), teachDeparts, 
        semester))
      put("weeks", WeekDays.All)
    }
    put("departs", getDeparts)
    put("teacherDeparts", getDeparts)
    put("tags", entityDao.getAll(classOf[LessonTag]))
    addBaseCode("langTypes", classOf[TeachLangType])
    put("campuses", project.getCampuses)
  }

  override def search(): String = {
    val query = getQueryBuilder
    val lessons = entityDao.search(query)
    val arrangeInfo = CollectUtils.newHashMap()
    val digestor = SuggestActivityDigestor.getInstance
    if (CollectUtils.isNotEmpty(lessons)) {
      val suggestQuery = OqlBuilder.from(classOf[ArrangeSuggest], "suggest")
      suggestQuery.where("suggest.lesson in (:lessons)", lessons)
      val suggests = entityDao.search(suggestQuery)
      for (suggest <- suggests) {
        val oneTask = suggest.getLesson
        arrangeInfo.put(oneTask.id.toString, digestor.digest(getTextResource, suggest, ":teacher+ :day :units :weeks")
          .replaceAll(",", "<br>"))
      }
    }
    put("arrangeInfo", arrangeInfo)
    put("lessons", lessons)
    put("teachDeparts", getTeachDeparts)
    put("semester", putSemester(getProject))
    put("teachDepartSize", getTeachDeparts.size)
    put("courseCategories", baseCodeService.getCodes(classOf[CourseCategory]))
    put("campuses", getProject.getCampuses)
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[Lesson] = lessonSearchHelper.buildQuery()

  override def edit(): String = {
    val project = getProject
    val semester = putSemester(null)
    val lessonId = getLongId("lesson")
    if (null == lessonId) {
      return forwardError("error.model.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val suggest = getSuggest(lesson)
    val gson = ArrangeSuggestGsonBuilder.build()
    val timeSetting = timeSettingService.getClosestTimeSetting(lesson.getProject, lesson.getSemester, 
      lesson.getCampus)
    putCommonInfoThings(lesson, suggest, gson, timeSetting)
    var flattenedOtherActivities = CollectUtils.newHashSet()
    if (CollectUtils.isNotEmpty(lesson.getTeachers)) {
      val otherActivitiesQuery = OqlBuilder.from(classOf[SuggestActivity], "otherActivity")
      otherActivitiesQuery.where("otherActivity.arrangeSuggest.lesson.id != :meLessonId", lessonId)
        .where("otherActivity.arrangeSuggest.lesson.project = :project", project)
        .where("otherActivity.arrangeSuggest.lesson.semester = :semester", semester)
        .where("exists ( from otherActivity.teachers otherTeacher where otherTeacher in ( :meTeachers ) )", 
        lesson.getTeachers)
      flattenedOtherActivities = SuggestActivityBean.flatten(entityDao.search(otherActivitiesQuery), 
        timeSetting)
      CollectionUtils.filter(flattenedOtherActivities, new Predicate() {

        def evaluate(`object`: AnyRef): Boolean = {
          return lesson.getTeachers.containsAll(`object`.asInstanceOf[SuggestActivity].getTeachers)
        }
      })
    }
    put("otherActivitiesJSON", gson.toJson(flattenedOtherActivities))
    val departments = getDeparts
    var optionalRooms = Collections.emptyList()
    if (CollectUtils.isNotEmpty(departments)) {
      optionalRooms = entityDao.search(OqlBuilder.from(classOf[Room], "room").where("exists(from room.departments department where department in (:departments))", 
        departments)
        .where("room.effectiveAt <= current_time() and (room.invalidAt is null or room.invalidAt >= current_time())")
        .where("room.campus = :campus", lesson.getCampus)
        .where("room.capacity >= :lowerLimit", lesson.getTeachClass.getLimitCount)
        .orderBy("room.campus.name, room.building.name, room.name, room.capacity"))
    }
    put("optionalRoomsJSON", gson.toJson(optionalRooms))
    put("roomsJSON", gson.toJson(suggest.getRooms))
    put("weekStateLength", ExamYearWeekTimeUtil.OVERALLWEEKS)
    forward()
  }

  private def putCommonInfoThings(lesson: Lesson, 
      suggest: ArrangeSuggest, 
      gson: Gson, 
      timeSetting: TimeSetting) {
    put("activitiesJSON", gson.toJson(SuggestActivityBean.flatten(suggest.getActivities, timeSetting)))
    put("weekDayList", WeekDays.All)
    if (CollectUtils.isEmpty(lesson.getTeachers)) {
      val emptyTeacher = new TeacherBean()
      emptyTeacher.setCode("")
      emptyTeacher.setName("无教师")
      emptyTeacher.setId(-1l)
      put("teachers", Collections.singletonList(emptyTeacher))
    } else {
      put("teachers", lesson.getTeachers)
    }
    put("lesson", lesson)
    put("suggest", suggest)
    val unitList = new ArrayList[CourseUnit](timeSetting.getDefaultUnits.values)
    Collections.sort(unitList)
    put("timeSetting", timeSetting)
    put("unitList", unitList)
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    put("firstWeekFrom", CourseTime.FIRST_WEEK_FROM)
  }

  private def getSuggest(lesson: Lesson): ArrangeSuggest = {
    val suggests = entityDao.get(classOf[ArrangeSuggest], "lesson", lesson)
    val suggest = if (CollectUtils.isNotEmpty(suggests)) suggests.get(0) else Model.newInstance(classOf[ArrangeSuggest])
    if (suggest.getLesson == null) {
      suggest.setLesson(lesson)
    }
    suggest
  }

  override def info(): String = {
    val lessonId = getLongId("lesson")
    if (null == lessonId) {
      return forwardError("error.model.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val suggest = getSuggest(lesson)
    val timeSetting = timeSettingService.getClosestTimeSetting(lesson.getProject, lesson.getSemester, 
      lesson.getCampus)
    val gson = ArrangeSuggestGsonBuilder.build()
    putCommonInfoThings(lesson, suggest, gson, timeSetting)
    forward()
  }

  override def save(): String = {
    val gson = ArrangeSuggestGsonBuilder.build()
    val suggest = populateEntity(classOf[ArrangeSuggest], "suggest")
    suggest.getRooms.clear()
    val roomIds = getIntIds("room")
    if (ArrayUtils.isNotEmpty(roomIds)) {
      suggest.addRooms(entityDao.get(classOf[Room], roomIds))
    }
    suggest.getActivities.clear()
    val activitiesJSON = get("activitiesJSON")
    val activities = gson.fromJson(activitiesJSON, new TypeToken[Set[SuggestActivityBean]]() {
    }.getType)
    val mergedActivities = SuggestActivityBean.mergeActivities(CollectUtils.newArrayList(activities))
    for (activity <- mergedActivities) {
      activity.setId(null)
    }
    suggest.addActivities(mergedActivities)
    entityDao.saveOrUpdate(suggest)
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    val lessonIds = getLongIds("lesson")
    if (ArrayUtils.isEmpty(lessonIds)) {
      return forwardError("error.model.id.needed")
    }
    val suggests = entityDao.get(classOf[ArrangeSuggest], "lesson.id", lessonIds)
    entityDao.remove(suggests)
    redirect("search", "info.delete.success")
  }
}
