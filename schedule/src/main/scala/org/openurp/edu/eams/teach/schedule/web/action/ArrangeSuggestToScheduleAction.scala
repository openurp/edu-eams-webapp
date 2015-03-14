package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Collection
import java.util.Collections
import java.util.List
import java.util.Map
import java.util.Set
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.eams.teach.code.school.CourseCategory
import org.openurp.edu.eams.teach.lesson.ArrangeSuggest
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.SuggestActivity
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.model.CourseActivityBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.SuggestActivityDigestor
import org.openurp.edu.eams.teach.schedule.json.ArrangeSuggestGsonBuilder
import org.openurp.edu.eams.teach.schedule.log.ScheduleLogBuilder
import org.openurp.edu.eams.teach.schedule.service.BruteForceArrangeContext
import org.openurp.edu.eams.teach.schedule.service.BruteForceArrangeService
import org.openurp.edu.eams.teach.schedule.service.CourseActivityService
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper
import org.openurp.edu.eams.web.action.common.ProjectSupportAction
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import scala.collection.JavaConversions._

class ArrangeSuggestToScheduleAction extends ProjectSupportAction {

  protected var lessonSearchHelper: LessonSearchHelper = _

  protected var lessonService: LessonService = _

  protected var bruteForceArrangeService: BruteForceArrangeService = _

  protected var courseActivityService: CourseActivityService = _

  protected var scheduleLogHelper: ScheduleLogHelper = _

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
    put("courseStatusEnums", CourseStatusEnum.values)
  }

  override def search(): String = {
    val query = getQueryBuilder
    val lessons = entityDao.search(query)
    put("lessons", lessons)
    put("arrangeInfo", makeArrangeDigest(lessons))
    put("teachDeparts", getTeachDeparts)
    put("semester", putSemester(getProject))
    put("teachDepartSize", getTeachDeparts.size)
    put("courseCategories", baseCodeService.getCodes(classOf[CourseCategory]))
    put("campuses", getProject.getCampuses)
    forward()
  }

  def batchArrangeSetting(): String = {
    val lessonIds = getLongIds("lesson")
    if (ArrayUtils.isEmpty(lessonIds)) {
      return forwardError("error.model.id.needed")
    }
    val query = OqlBuilder.from(classOf[ArrangeSuggest], "suggest")
    query.where("suggest.lesson.id in (:lessonIds)", lessonIds)
    val suggests = entityDao.search(query)
    val gson = ArrangeSuggestGsonBuilder.build()
    val departments = getDeparts
    var optionalRooms = Collections.emptyList()
    if (CollectUtils.isNotEmpty(departments)) {
      optionalRooms = entityDao.search(OqlBuilder.from(classOf[Classroom], "room").where("exists(from room.departments department where department in (:departments))", 
        departments)
        .where("room.effectiveAt <= current_time() and (room.invalidAt is null or room.invalidAt >= current_time())")
        .where("exists(from " + classOf[Lesson].getName + 
        " l where l.campus.id=room.campus.id and l.id in (:lessonIds))", lessonIds)
        .where("exists(from " + classOf[Lesson].getName + 
        " l where l.teachClass.limitCount <= room.capacity and l.id in (:lessonIds2))", lessonIds)
        .orderBy("room.campus.name, room.building.name, room.name, room.capacity"))
    }
    put("lessonIds", lessonIds)
    put("optionalRoomsJSON", gson.toJson(optionalRooms))
    put("suggests", suggests)
    put("lessonId2SuggestRoomsJSON", lessonId2SuggestRoomsJSON(suggests))
    put("lessonId2Digests", makeSuggestDigest(entityDao.get(classOf[Lesson], lessonIds)))
    forward()
  }

  private def lessonId2SuggestRoomsJSON(suggests: Collection[ArrangeSuggest]): Map[String, String] = {
    val lessonId2SuggestRoomsJSON = CollectUtils.newHashMap()
    val gson = ArrangeSuggestGsonBuilder.build()
    for (suggest <- suggests) {
      lessonId2SuggestRoomsJSON.put(suggest.getLesson.getId.toString, gson.toJson(suggest.getRooms))
    }
    lessonId2SuggestRoomsJSON
  }

  private def makeArrangeDigest(lessons: Collection[Lesson]): Map[String, String] = {
    val arrangeInfo = CollectUtils.newHashMap()
    val digestor = CourseActivityDigestor.getInstance
    for (oneTask <- lessons) {
      arrangeInfo.put(oneTask.getId.toString, digestor.digest(getTextResource, oneTask, ":teacher+ :day :units :weeks :room"))
    }
    arrangeInfo
  }

  private def makeSuggestDigest(lessons: Collection[Lesson]): Map[String, String] = {
    val arrangeInfo = CollectUtils.newHashMap()
    val digestor = SuggestActivityDigestor.getInstance
    if (CollectUtils.isNotEmpty(lessons)) {
      val suggestQuery = OqlBuilder.from(classOf[ArrangeSuggest], "suggest")
      suggestQuery.where("suggest.lesson in (:lessons)", lessons)
      val suggests = entityDao.search(suggestQuery)
      for (suggest <- suggests) {
        val oneTask = suggest.getLesson
        arrangeInfo.put(oneTask.getId.toString, digestor.digest(getTextResource, suggest, ":teacher+ :day :units :weeks")
          .replaceAll(",", "<br>"))
      }
    }
    arrangeInfo
  }

  def batchArrange(): String = {
    val lessonIds = getLongIds("lesson")
    if (ArrayUtils.isEmpty(lessonIds)) {
      return forwardError("error.model.id.needed")
    }
    val lessonId2roomIdsJSON = get("lessonId2roomIdsJSON")
    val gson = new GsonBuilder().create()
    val raw_lessonIdString2roomIdList = gson.fromJson(lessonId2roomIdsJSON, classOf[AnyRef]).asInstanceOf[Map[String, Any]]
    val lessonId2roomIdArray = CollectUtils.newHashMap()
    for (key <- raw_lessonIdString2roomIdList.keySet) {
      val objs = raw_lessonIdString2roomIdList.get(key).asInstanceOf[List[_]]
      val longs = Array.ofDim[Integer](objs.size)
      for (i <- 0 until objs.size) {
        longs(i) = objs.get(i).asInstanceOf[Number].intValue()
      }
      lessonId2roomIdArray.put(java.lang.Long.valueOf(key.asInstanceOf[String]), longs)
    }
    val query = OqlBuilder.from(classOf[ArrangeSuggest], "suggest")
    query.where("suggest.lesson.id in (:lessonIds)", lessonIds)
    val suggests = entityDao.search(query)
    val failedContexts = CollectUtils.newArrayList()
    for (suggest <- suggests) {
      val courseActivities = CollectUtils.newArrayList()
      for (suggestActivity <- suggest.getActivities) {
        courseActivities.add(suggestActivity.toCourseActivity())
      }
      val mergedCourseActivities = CourseActivityBean.mergeActivites(courseActivities)
      val lesson = suggest.getLesson
      val canUseRooms = entityDao.get(classOf[Classroom], lessonId2roomIdArray.get(lesson.getId))
      val context = new BruteForceArrangeContext(lesson, mergedCourseActivities)
      bruteForceArrangeService.bruteForceArrange(context, canUseRooms)
      if (context.isFailed) {
        failedContexts.add(context)
      }
    }
    if (CollectUtils.isNotEmpty(failedContexts)) {
      val failedLessonIds = CollectUtils.newHashSet()
      for (context <- failedContexts) {
        failedLessonIds.add(context.getLesson.getId)
      }
      put("failedContexts", failedContexts)
      put("lessonId2Digests", makeSuggestDigest(entityDao.get(classOf[Lesson], failedLessonIds)))
      return "batchArrangeFailedList"
    }
    redirect("search", "info.action.success")
  }

  def unschedule(): String = {
    val lessonIds = getLongIds("lesson")
    val semester = putSemester(null)
    if (ArrayUtils.isEmpty(lessonIds)) {
      return forwardError("error.model.id.needed")
    }
    try {
      courseActivityService.removeActivities(lessonIds, semester)
      for (lessonId <- lessonIds) {
        scheduleLogHelper.log(ScheduleLogBuilder.delete(entityDao.get(classOf[Lesson], lessonId), "批量排课"))
      }
    } catch {
      case e: Exception => return redirect("search", "info.delete.failure")
    }
    redirect("search", "info.delete.success")
  }

  protected override def getQueryBuilder(): OqlBuilder[Lesson] = lessonSearchHelper.buildQuery()

  def setLessonSearchHelper(lessonSearchHelper: LessonSearchHelper) {
    this.lessonSearchHelper = lessonSearchHelper
  }

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }

  def setBruteForceArrangeService(bruteForceArrangeService: BruteForceArrangeService) {
    this.bruteForceArrangeService = bruteForceArrangeService
  }

  def setCourseActivityService(courseActivityService: CourseActivityService) {
    this.courseActivityService = courseActivityService
  }
}
