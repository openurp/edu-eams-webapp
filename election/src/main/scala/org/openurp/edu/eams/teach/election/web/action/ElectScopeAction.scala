package org.openurp.edu.eams.teach.election.web.action





import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Predicate
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.openurp.base.Campus
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.service.CourseLimitExtractorService
import org.openurp.edu.eams.teach.lesson.service.CourseLimitGroupBuilder
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.task.util.ProjectUtils
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class ElectScopeAction extends SemesterSupportAction {

  protected var courseLimitExtractorService: CourseLimitExtractorService = _

  protected var lessonSearchHelper: LessonSearchHelper = _

  protected var courseLimitService: CourseLimitService = _

  def index(): String = {
    val semester = putSemester(null)
    val teachDepartBuilder = OqlBuilder.from(classOf[Lesson].getName, "lesson")
    teachDepartBuilder.where("lesson.project =:project", getProject)
    teachDepartBuilder.where("lesson.semester =:semester", semester)
    teachDepartBuilder.groupBy("lesson.teachDepart.id")
    teachDepartBuilder.select("lesson.teachDepart.id")
    val courseTypeBuilder = OqlBuilder.from(classOf[Lesson].getName, "lesson")
    courseTypeBuilder.where("lesson.project =:project", getProject)
    courseTypeBuilder.where("lesson.semester =:semester", semester)
    courseTypeBuilder.groupBy("lesson.courseType.id")
    courseTypeBuilder.select("lesson.courseType.id")
    put("teachDeparts", entityDao.get(classOf[Department], entityDao.search(teachDepartBuilder)))
    put("teachClassDeparts", getDeparts)
    put("courseTypes", entityDao.get(classOf[CourseType], entityDao.search(courseTypeBuilder)))
    addBaseInfo("campuses", classOf[Campus])
    put("stdTypes", getStdTypes)
    put("weeks", WeekDays.All)
    put("lessonAuditStates", CommonAuditState.values)
    forward()
  }

  def search(): String = {
    val builder = lessonSearchHelper.buildQuery()
    val isElectable = getBoolean("isElectable")
    if (null != isElectable) {
      builder.where((if (isElectable) "" else "not ") + "exists (from " + 
        classOf[ElectionProfile].getName + 
        " electionProfile join electionProfile.electableLessons electableLesson " + 
        "where electableLesson.id=lesson.id and electionProfile.semester=lesson.semester)")
    }
    put("lessons", entityDao.search(builder))
    put("guaPaiTag", Model.newInstance(classOf[LessonTag], LessonTag.PredefinedTags.GUAPAI.id))
    forward()
  }

  def info(): String = {
    val lessonId = getLongId("lesson")
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val scopes = new ArrayList[Map[String, Any]]()
    for (limitGroup <- lesson.getTeachClass.getLimitGroups) {
      val scopeMap = new HashMap[String, Any]()
      scopeMap.put("grades", courseLimitExtractorService.extractGrade(limitGroup))
      scopeMap.put("educations", courseLimitExtractorService.extractEducations(limitGroup))
      scopeMap.put("stdTypes", courseLimitExtractorService.extractStdTypes(limitGroup))
      scopeMap.put("attendDeparts", courseLimitExtractorService.extractAttendDeparts(limitGroup))
      scopeMap.put("majors", courseLimitExtractorService.extractMajors(limitGroup))
      scopeMap.put("directions", courseLimitExtractorService.extractDirections(limitGroup))
      scopeMap.put("adminclasses", courseLimitExtractorService.extractAdminclasses(limitGroup))
      scopes.add(scopeMap)
    }
    put("lesson", lesson)
    put("scopes", scopes)
    forward()
  }

  def configureScope(): String = {
    val lessonId = getLongId("lesson")
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    put("lesson", lesson)
    put("educations", getEducations)
    put("stdTypes", getStdTypes)
    put("examModes", baseCodeService.getCodes(classOf[ExamMode]))
    put("departments", ProjectUtils.getColleges(getProject))
    val scopes = new ArrayList[Map[String, Any]]()
    for (scope <- lesson.getTeachClass.getLimitGroups) {
      val scopeMap = new HashMap[String, Any]()
      scopeMap.put("grades", courseLimitExtractorService.extractGrade(scope))
      scopeMap.put("stdTypes", courseLimitExtractorService.extractStdTypes(scope))
      scopeMap.put("attendDeparts", courseLimitExtractorService.extractAttendDeparts(scope))
      scopeMap.put("majors", courseLimitExtractorService.extractMajors(scope))
      scopeMap.put("directions", courseLimitExtractorService.extractDirections(scope))
      scopeMap.put("adminclasses", courseLimitExtractorService.extractAdminclasses(scope))
      scopes.add(scopeMap)
    }
    put("scopes", scopes)
    forward()
  }

  def batchConfigureScope(): String = {
    val lessonIds = getLongIds("lesson")
    put("lessonIds", Strings.join(lessonIds, ','))
    forward()
  }

  def saveBatchConfigureScope(): String = {
    val lessons = entityDao.get(classOf[Lesson], getLongIds("lesson"))
    clearLimitGroup(lessons)
    addLimitGroup(lessons)
    setLimitCount(lessons)
    entityDao.saveOrUpdate(lessons)
    redirect("search", "info.action.success", get("params"))
  }

  private def addLimitGroup(lessons: List[Lesson]) {
    val addLimitGroup = getBool("option.scope.op_add")
    if (!addLimitGroup) {
      return
    }
    val grades = get("grades")
    val educationIds = getIntIds("education")
    val stdTypeIds = getIntIds("stdType")
    val attendDepartIds = getIntIds("attendDepart")
    val majorIds = getIntIds("major")
    val directionIds = getIntIds("direction")
    val adminclassIds = getIntIds("adminclass")
    val limitGroupBuilder = courseLimitService.builder()
    if (Strings.isNotEmpty(grades)) {
      limitGroupBuilder.inGrades(Strings.split(grades))
    }
    if (ArrayUtils.isNotEmpty(educationIds)) {
      val op = get("education.op")
      if ("IN" == op) {
        limitGroupBuilder.in(entityDao.get(classOf[Education], educationIds).toArray(Array()))
      } else if ("NOT_IN" == op) {
        limitGroupBuilder.notIn(entityDao.get(classOf[Education], educationIds).toArray(Array()))
      }
    }
    if (ArrayUtils.isNotEmpty(stdTypeIds)) {
      val op = get("stdType.op")
      if ("IN" == op) {
        limitGroupBuilder.in(entityDao.get(classOf[StdType], stdTypeIds).toArray(Array()))
      } else if ("NOT_IN" == op) {
        limitGroupBuilder.notIn(entityDao.get(classOf[StdType], stdTypeIds).toArray(Array()))
      }
    }
    if (ArrayUtils.isNotEmpty(attendDepartIds)) {
      val op = get("attendDepart.op")
      if ("IN" == op) {
        limitGroupBuilder.in(entityDao.get(classOf[Department], attendDepartIds)
          .toArray(Array()))
      } else if ("NOT_IN" == op) {
        limitGroupBuilder.notIn(entityDao.get(classOf[Department], attendDepartIds)
          .toArray(Array()))
      }
    }
    if (ArrayUtils.isNotEmpty(majorIds)) {
      val op = get("major.op")
      if ("IN" == op) {
        limitGroupBuilder.in(entityDao.get(classOf[Major], majorIds).toArray(Array()))
      } else if ("NOT_IN" == op) {
        limitGroupBuilder.notIn(entityDao.get(classOf[Major], majorIds).toArray(Array()))
      }
    }
    if (ArrayUtils.isNotEmpty(directionIds)) {
      val op = get("direction.op")
      if ("IN" == op) {
        limitGroupBuilder.in(entityDao.get(classOf[Direction], directionIds).toArray(Array()))
      } else if ("NOT_IN" == op) {
        limitGroupBuilder.notIn(entityDao.get(classOf[Direction], directionIds).toArray(Array()))
      }
    }
    if (ArrayUtils.isNotEmpty(adminclassIds)) {
      val op = get("adminclass.op")
      if ("IN" == op) {
        limitGroupBuilder.in(entityDao.get(classOf[Adminclass], adminclassIds).toArray(Array()))
      } else if ("NOT_IN" == op) {
        limitGroupBuilder.notIn(entityDao.get(classOf[Adminclass], adminclassIds).toArray(Array()))
      }
    }
    val limitGroup = limitGroupBuilder.build()
    limitGroup.setForClass(false)
    for (lesson <- lessons) {
      lesson.getTeachClass.addLimitGroups(limitGroup.clone().asInstanceOf[CourseLimitGroup])
    }
  }

  private def clearLimitGroup(lessons: List[Lesson]) {
    val clearLimitGroup = getBool("option.scope.op_clear")
    if (!clearLimitGroup) {
      return
    }
    for (lesson <- lessons) {
      CollectionUtils.filter(lesson.getTeachClass.getLimitGroups, new Predicate() {

        def evaluate(`object`: AnyRef): Boolean = {
          `object`.asInstanceOf[CourseLimitGroup].isForClass
        }
      })
    }
  }

  private def setLimitCount(lessons: List[Lesson]) {
    val option = get("option.limitCount.setting")
    if ("min_room_capacity" == option) {
      for (lesson <- lessons) {
        var limitCount = 0
        var minCapacity = java.lang.Integer.MAX_VALUE
        for (activity <- lesson.getCourseSchedule.getActivities; room <- activity.getRooms if room.getCapacity < minCapacity) {
          minCapacity = room.getCapacity
        }
        limitCount = if (minCapacity == java.lang.Integer.MAX_VALUE) 0 else minCapacity
        lesson.getTeachClass.setLimitCount(limitCount)
      }
    } else if ("customize" == option) {
      val limitCount = getInt("option.limitCount")
      for (lesson <- lessons) {
        lesson.getTeachClass.setLimitCount(limitCount)
      }
    }
  }

  def showSelect(): String = {
    val idsMap = CollectUtils.newHashMap()
    val grades = Strings.split(get("grades"))
    val educationIds = Strings.splitToInt(get("educationIds"))
    val stdTypeIds = Strings.splitToInt(get("stdTypeIds"))
    val attendDepartIds = Strings.splitToInt(get("attendDepartIds"))
    val majorIds = Strings.splitToInt(get("majorIds"))
    val directionIds = Strings.splitToInt(get("directionIds"))
    idsMap.put(classOf[Education], get("educationIds"))
    idsMap.put(classOf[StdType], get("stdTypeIds"))
    idsMap.put(classOf[Department], get("departIds"))
    idsMap.put(classOf[Major], get("majorIds"))
    idsMap.put(classOf[Direction], get("directionIds"))
    val `type` = get("type")
    var datas = new ArrayList()
    if ("educations" == `type`) {
      datas = getEducations
    } else if ("stdTypes" == `type`) {
      datas = getStdTypes
    } else if ("attendDeparts" == `type`) {
      datas = getCollegeOfDeparts
    } else if ("majors" == `type`) {
      var warnings = ""
      if (ArrayUtils.isEmpty(educationIds)) {
        warnings += "请先选择学历层次\n"
      }
      if (ArrayUtils.isEmpty(attendDepartIds)) {
        warnings += "请先选择听课院系"
      }
      if (Strings.isNotBlank(warnings)) {
        put("warnings", warnings)
      } else {
        val query = OqlBuilder.from(classOf[Major], "major")
        query.where("major.project.id = :projectId", getProject.id)
          .where("exists(from major.educations e where e.id in (:educationIds))", educationIds)
          .where("exists(from major.journals md where md.depart.id in (:departIds))", attendDepartIds)
          .orderBy("major.code, major.name")
        datas = entityDao.search(query)
      }
    } else if ("directions" == `type`) {
      var warnings = ""
      if (ArrayUtils.isEmpty(majorIds)) {
        warnings += "请先选择专业"
      }
      if (Strings.isNotBlank(warnings)) {
        put("warnings", warnings)
      } else {
        val query = OqlBuilder.from(classOf[Direction], "direction")
        query.where("direction.major.project.id = :projectId", getProject.id)
          .where("direction.major.id in (:majorIds)", majorIds)
          .orderBy("direction.code, direction.name")
        if (ArrayUtils.isNotEmpty(attendDepartIds)) {
          query.where("exists(from direction.departs dd where dd.depart.id in (:departIds))", attendDepartIds)
        }
        datas = entityDao.search(query)
      }
    } else if ("adminclasses" == `type`) {
      var warnings = ""
      if (ArrayUtils.isEmpty(attendDepartIds)) {
        warnings += "请先选择听课院系\n"
      }
      if (ArrayUtils.isEmpty(majorIds)) {
        warnings += "请先选择专业"
      }
      if (Strings.isNotBlank(warnings)) {
        put("warnings", warnings)
      } else {
        val query = OqlBuilder.from(classOf[Adminclass], "adminclass")
        query.where("adminclass.major.project.id = :projectId", getProject.id)
          .where("adminclass.department.id in (:departIds)", attendDepartIds)
          .where("adminclass.major.id in (:majorIds)", majorIds)
        if (ArrayUtils.isNotEmpty(grades)) {
          query.where("adminclass.grade in (:grades)", grades)
        }
        if (ArrayUtils.isNotEmpty(educationIds)) {
          query.where("adminclass.education.id in (:educationIds)", educationIds)
        }
        if (ArrayUtils.isNotEmpty(stdTypeIds)) {
          query.where("adminclass.stdType.id in (:stdTypeIds)", stdTypeIds)
        }
        if (ArrayUtils.isNotEmpty(directionIds)) {
          query.where("adminclass.direction.id in (:directionIds)", directionIds)
        }
        datas = entityDao.search(query)
      }
    }
    put("datas", datas)
    put("type", `type`)
    "batch/showSelect"
  }

  def setCourseLimitExtractorService(courseLimitExtractorService: CourseLimitExtractorService) {
    this.courseLimitExtractorService = courseLimitExtractorService
  }

  def setLessonSearchHelper(lessonSearchHelper: LessonSearchHelper) {
    this.lessonSearchHelper = lessonSearchHelper
  }

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }
}
