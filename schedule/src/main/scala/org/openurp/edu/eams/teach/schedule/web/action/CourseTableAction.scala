package org.openurp.edu.eams.teach.schedule.web.action

import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Collections
import java.util.Date
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Set
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Transformer
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.bean.comparators.MultiPropertyComparator
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.eams.base.Building
import org.openurp.edu.eams.base.Campus
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.base.Semester
import org.openurp.code.person.Gender
import org.openurp.edu.eams.base.code.school.ClassroomType
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.core.service.StudentService
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.Textbook
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.eams.teach.lesson.CourseMaterial
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.model.LessonMaterialBean
import org.openurp.edu.eams.teach.lesson.service.CourseLimitUtils
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategyFactory
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.TimeUnitUtil
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.base.Program
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeSwitch
import org.openurp.edu.eams.teach.schedule.model.CourseTableSetting
import org.openurp.edu.eams.teach.schedule.util.CourseTable
import org.openurp.edu.eams.teach.schedule.util.MultiCourseTable
import org.openurp.edu.eams.teach.service.TeachResourceService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.helper.BaseInfoSearchHelper
import org.openurp.edu.eams.web.helper.StdSearchHelper

import scala.collection.JavaConversions._

class CourseTableAction extends SemesterSupportAction {

  protected var lessonFilterStrategyFactory: LessonFilterStrategyFactory = _

  protected var lessonService: LessonService = _

  protected var baseInfoSearchHelper: BaseInfoSearchHelper = _

  protected var studentService: StudentService = _

  protected var teachResourceService: TeachResourceService = _

  protected var majorPlanService: MajorPlanService = _

  protected var stdSearchHelper: StdSearchHelper = _

  protected var timeSettingService: TimeSettingService = _

  protected var adminclassCourseGroups: Map[Adminclass, Map[CourseType, Float]] = CollectUtils.newHashMap()

  protected var adminclassLessonGroups: Map[Adminclass, Map[CourseType, Set[Lesson]]] = CollectUtils.newHashMap()

  override def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    put("classroomConfigTypeList", baseCodeService.getCodes(classOf[ClassroomType]))
    addBaseInfo("campusList", classOf[Campus])
    addBaseInfo("buildings", classOf[Building])
    val project = getProject
    put("campuses", project.getCampuses)
    put("teacherDeparts", departmentService.getTeachDeparts)
    put("teachDeparts", lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      getAttribute("semester").asInstanceOf[Semester]))
    put("courseTableType", get("courseTableType"))
    put("genders", baseCodeService.getCodes(classOf[Gender]))
    forward()
  }

  def courseTake(): String = {
    val lessonId = getLongId("lesson")
    val adminclassId = getIntId("adminclass")
    if (null == lessonId) {
      return forwardError("error.model.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    put("lesson", lesson)
    val courseTakes = lesson.getTeachClass.getCourseTakes
    val targetCourseTakes = CollectUtils.newArrayList()
    if (null != adminclassId) {
      for (courseTake <- courseTakes if courseTake.getStd.getAdminclass.getId == adminclassId) {
        targetCourseTakes.add(courseTake)
      }
      put("courseTakes", targetCourseTakes)
    } else {
      put("courseTakes", courseTakes)
    }
    forward()
  }

  def publicHome(): String = {
    put("departmentList", departmentService.getColleges)
    put("stdTypeList", baseCodeService.getCodes(classOf[StdType]))
    putSemester(null)
    put("classroomConfigTypeList", baseCodeService.getCodes(classOf[ClassroomType]))
    put("districtList", baseInfoService.getBaseInfos(classOf[Campus]))
    put("teacherDeparts", this.entityDao.getAll(classOf[Department]))
    forward("index")
  }

  def taskTable(): String = {
    val lessonId = getLongId("lesson")
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    put("startWeek", new java.lang.Integer(1))
    put("endWeek", new java.lang.Integer(lesson.getSemester.getWeeks))
    put("weekList", WeekDays.All)
    put("activityList", lesson.getCourseSchedule.getActivities)
    put("lesson", lesson)
    put("semester", lesson.getSemester)
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, lesson.getSemester, null))
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    forward()
  }

  def courseTableOfTask(): String = {
    val adminClassIds = Strings.splitToInt(get("adminClassIds"))
    val semesterId = getInt("semester.id")
    val semester = semesterService.getSemester(semesterId)
    val courseTables = CollectUtils.newHashMap()
    var adminclasses = CollectUtils.newArrayList()
    if (ArrayUtils.isNotEmpty(adminClassIds)) {
      adminclasses = entityDao.get(classOf[Adminclass], adminClassIds)
    }
    val adminclassForNoMajors = CollectUtils.newArrayList()
    val time = new CourseTime()
    for (adminclass <- adminclasses) {
      val courseActivities = teachResourceService.getAdminclassActivities(adminclass, time, semester)
      courseTables.put(adminclass.getId.toString, courseActivities)
      putActivityId2ArrangeWeek(semester, courseActivities)
      if (null == adminclass.major) adminclassForNoMajors.add(adminclass)
    }
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, semester, null))
    put("semester", semester)
    put("courseTables", courseTables)
    adminclasses.removeAll(adminclassForNoMajors)
    Collections.sort(adminclasses, new MultiPropertyComparator("department.code,major.code,code"))
    adminclasses.addAll(adminclasses.size, adminclassForNoMajors)
    put("adminClasses", adminclasses)
    put("weeks", WeekDays.All)
    put("project", getProject)
    forward()
  }

  def courseTableOfTeacher(): String = {
    val teacherIds = Strings.splitToLong(get("teacherIds"))
    val semesterId = getInt("semester.id")
    val semester = semesterService.getSemester(semesterId)
    val courseTables = CollectUtils.newHashMap()
    var teachers = CollectUtils.newArrayList()
    if (ArrayUtils.isNotEmpty(teacherIds)) {
      teachers = entityDao.get(classOf[Teacher], teacherIds)
    }
    val time = new CourseTime()
    for (teacher <- teachers) {
      val courseActivities = teachResourceService.getTeacherActivities(teacher, time, semester)
      courseTables.put(teacher.getId.toString, courseActivities)
      putActivityId2ArrangeWeek(semester, courseActivities)
    }
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, semester, null))
    put("semester", semester)
    put("courseTables", courseTables)
    Collections.sort(teachers, new PropertyComparator("code"))
    put("teachers", teachers)
    put("weeks", WeekDays.All)
    forward()
  }

  def courseTableOfClassroom(): String = {
    val roomIds = Strings.splitToInt(get("roomIds"))
    val semesterId = getInt("semester.id")
    val semester = semesterService.getSemester(semesterId)
    val courseTables = CollectUtils.newHashMap()
    var classrooms = CollectUtils.newArrayList()
    if (ArrayUtils.isNotEmpty(roomIds)) {
      classrooms = entityDao.get(classOf[Classroom], roomIds)
    }
    val time = new CourseTime()
    for (classroom <- classrooms) {
      val courseActivities = teachResourceService.getRoomActivities(classroom, time, semester)
      courseTables.put(classroom.getId.toString, courseActivities)
      putActivityId2ArrangeWeek(semester, courseActivities)
    }
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, semester, null))
    put("semester", semester)
    put("courseTables", courseTables)
    Collections.sort(classrooms, new PropertyComparator("code"))
    put("classrooms", classrooms)
    put("weeks", WeekDays.All)
    forward()
  }

  private def putActivityId2ArrangeWeek(semester: Semester, courseActivities: Collection[CourseActivity]) {
    val activityId2ArrangeWeek = CollectUtils.newHashMap()
    val digestor = CourseActivityDigestor.getInstance
    for (courseActivity <- courseActivities) {
      activityId2ArrangeWeek.put(courseActivity.getId, digestor.digest(getTextResource, Collections.singleton(courseActivity), 
        CourseActivityDigestor.weeks))
    }
    put("activityId2ArrangeWeek", activityId2ArrangeWeek)
  }

  protected def buildStdQuery(): OqlBuilder[Student] = {
    val query = OqlBuilder.from(classOf[Student], "std")
    populateConditions(query)
    val stdActive = getBoolean("stdActive")
    if (null != stdActive) {
      if (true == stdActive) {
        query.where("std.registOn <= :now and std.graduateOn >= :now and std.registed = true", new Date())
      } else {
        query.where("std.registOn > :now or std.graduateOn < :now or std.registed=false", new Date())
      }
    }
    val educations = getEducations
    if (!educations.isEmpty) {
      query.where("std.education in (:educations)", educations)
    }
    val departments = getDeparts
    if (!departments.isEmpty) {
      query.where("std.department in (:departments)", departments)
    }
    query.where("std.project = :project", getProject)
    val stdTypes = getStdTypes
    if (!stdTypes.isEmpty) {
      query.where("std.type in (:stdTypes)", stdTypes)
    }
    query.limit(QueryHelper.getPageLimit)
    query.orderBy(Order.parse(Params.get("orderBy")))
    val adminclassName = Params.get("adminclassName")
    if (Strings.isNotEmpty(Strings.trim(adminclassName))) {
      query.where(new Condition("std.adminclass.name like :adminclassName", adminclassName))
    }
    query
  }

  def search(): String = {
    val kind = get("courseTableType")
    if (Strings.isEmpty(kind)) {
      return forwardError("error.courseTable.unknown")
    }
    put("semester", semesterService.getSemester(getInt("semester.id")))
    val project = getProject
    if (CourseTable.CLASS == kind) {
      val q = baseInfoSearchHelper.buildAdminclassQuery()
      q.where("adminclass.project = :project", project)
      put("adminclasses", entityDao.search(q))
      forward("adminClassList")
    } else if (CourseTable.STD == kind) {
      put("students", entityDao.search(buildStdQuery().where("std.project = :project", project)))
      forward("stdList")
    } else if (CourseTable.ROOM == kind) {
      put("classrooms", entityDao.search(baseInfoSearchHelper.buildClassroomQuery()))
      forward("classroomList")
    } else if (CourseTable.TEACHER == kind) {
      val teachers = entityDao.search(baseInfoSearchHelper.buildTeacherQuery())
      put("teachers", teachers)
      forward("teacherList")
    } else if (CourseTable.PROGRAM == kind) {
      val programs = entityDao.search(buildProgramQuery().where("program.major.project = :project", project))
      put("programs", programs)
      forward("programList")
    } else {
      put("weeks", WeekDays.All)
      put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, getSemester, null))
      put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
      forward("lessonList")
    }
  }

  protected def buildProgramQuery(): OqlBuilder[Program] = {
    val builder = OqlBuilder.from(classOf[Program], "program")
    QueryHelper.populateConditions(builder)
    val departments = restrictionHelper.getDeparts.asInstanceOf[List[Department]]
    if (!departments.isEmpty) {
      builder.where("program.department in (:departments)", departments)
    } else {
      builder.where("1=2")
    }
    val educations = restrictionHelper.educations.asInstanceOf[List[Education]]
    if (!educations.isEmpty) {
      builder.where("program.education in (:educations)", educations)
    } else {
      builder.where("1=2")
    }
    builder.limit(QueryHelper.getPageLimit)
    var orderByPras = Params.get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "program.name"
    }
    builder.orderBy(orderByPras)
    builder
  }

  def courseTable(): String = {
    val semester = getSemester
    if (semester == null) {
      return forwardError("error.semester.id.notExists")
    }
    val setting = populate(classOf[CourseTableSetting], "setting")
    setting.setSemester(semester)
    setting.setTimes(getTimesFormPage(semester))
    if (Strings.isEmpty(setting.getKind)) {
      return forwardError("error.courseTable.unknown")
    }
    val ids = get("ids")
    if (Strings.isEmpty(ids)) {
      put("prompt", "common.lessOneSelectPlease")
      return forward("prompt")
    }
    val clazz = CourseTable.getResourceClass(setting.getKind)
    val idClazz = Model.getType(clazz).getIdType
    val rsList = CollectUtils.newArrayList()
    for (a <- Strings.split(ids)) {
      rsList.add(DefaultConversion.Instance.convert(a, idClazz))
    }
    val entityQuery = OqlBuilder.from(clazz, "resource").where("resource.id in (:ids)", rsList)
    val resources = entityDao.search(entityQuery)
    var orders = Order.parse(get("setting.orderBy"))
    if (orders.isEmpty) {
      orders.add(new Order("code asc"))
    }
    if ("program" == setting.getKind) {
      orders = CollectUtils.newArrayList(new Order("name asc"))
    }
    val order = orders.get(0).asInstanceOf[Order]
    Collections.sort(resources, new PropertyComparator(getLastSubString(order.getProperty), order.isAscending))
    val courseTableList = CollectUtils.newArrayList()
    if (setting.getTablePerPage == 1) {
      for (resource <- resources) {
        courseTableList.add(buildCourseTable(setting, resource))
      }
    } else {
      var i = 0
      var multiTable: MultiCourseTable = null
      for (resource <- resources) {
        if (i % setting.getTablePerPage == 0) {
          multiTable = new MultiCourseTable()
          courseTableList.add(multiTable)
        }
        multiTable.getResources.add(resource)
        multiTable.getTables.add(buildCourseTable(setting, resource))
        i += 1
      }
    }
    setting.setWeekdays(Arrays.asList(WeekDays.All:_*))
    setting.setDisplaySemesterTime(true)
    put("courseTableList", courseTableList)
    if (setting.getTablePerPage == 1 && !setting.getIgnoreTask) {
      val textbookMap = CollectUtils.newHashMap()
      for (`object` <- courseTableList) {
        val table = `object`.asInstanceOf[CourseTable]
        for (lesson <- table.getLessons) {
          val lessonMaterials = entityDao.get(classOf[LessonMaterialBean], "lesson", lesson)
          if (!lessonMaterials.isEmpty) {
            val lessonMaterial = lessonMaterials.get(0)
            if (lessonMaterial.getPassed != null && true == lessonMaterial.getPassed) {
              textbookMap.put(lesson, CollectUtils.newHashSet(lessonMaterials.get(0).getBooks))
            }
          } else {
            val courseMaterials = entityDao.search(OqlBuilder.from(classOf[CourseMaterial], "courseMaterial")
              .where("courseMaterial.course = :course", lesson.getCourse)
              .where("courseMaterial.department = :department", lesson.getTeachDepart)
              .where("courseMaterial.semester = :semester", lesson.getSemester)
              .where("courseMaterial.passed is true"))
            if (!courseMaterials.isEmpty) {
              textbookMap.put(lesson, CollectUtils.newHashSet(courseMaterials.get(0).getBooks))
            }
          }
        }
      }
      put("textbookMap", textbookMap)
    }
    put("setting", setting)
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, setting.getSemester, null))
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    if (CourseTable.CLASS == setting.getKind) {
      put("adminclassLessonGroups", adminclassLessonGroups)
      put("adminclassCourseGroups", adminclassCourseGroups)
    }
    if (1 == setting.getTablePerPage) {
      forward()
    } else {
      forward("courseTable_" + setting.getStyle)
    }
  }

  def peCourseTable(): String = {
    val peCourseCodes = get("pe.courseCodes").trim().split("\\s*,\\s*")
    val semester = getSemester
    val weekIds = getDistinctWeekdays(peCourseCodes, semester)
    val unitRanges = getDistinctUnitRanges(peCourseCodes, semester)
    val arrangeQuery = new StringBuilder()
    arrangeQuery.append("select distinct \n").append("    activity.time.weekday,\n")
      .append("    activity.time.startUnit,\n")
      .append("    activity.time.endUnit,\n")
      .append("    lesson.course.code, \n")
      .append("    (select count(student.id) from adminClass.students student where student.gender.id=1), \n")
      .append("    (select count(student.id) from adminClass.students student where student.gender.id=2) \n")
      .append("from\n")
      .append("	org.openurp.edu.teach.lesson.Lesson lesson \n")
      .append("    join lesson.courseSchedule.activities activity\n")
      .append("where\n")
      .append("lesson.semester=:semester\n")
      .append("and lesson.course.code in (:peCourseCodes)\n")
      .append("and lesson.courseSchedule.status=:status\n")
      .append("order by\n")
      .append("    activity.time.weekId,\n")
      .append("    activity.time.startUnit,\n")
      .append("    activity.time.endUnit,\n")
      .append("    lesson.course.code,\n")
    val arrangeQueryParams = CollectUtils.newHashMap()
    arrangeQueryParams.put("peCourseCodes", peCourseCodes)
    arrangeQueryParams.put("semester", semester)
    arrangeQueryParams.put("status", CourseStatusEnum.ARRANGED)
    val arranges = entityDao.search(arrangeQuery.toString, arrangeQueryParams)
    val courseQuery = new StringBuilder()
    courseQuery.append("select distinct course from Course course where course.code in(:courseCodes)\n")
    val courseQueryParams = CollectUtils.newHashMap()
    courseQueryParams.put("courseCodes", peCourseCodes)
    val courses = entityDao.search(courseQuery.toString, courseQueryParams)
    val courseCodeNameMap = new HashMap[String, String]()
    for (i <- 0 until courses.size) {
      courseCodeNameMap.put(courses.get(i).getCode, courses.get(i).getName)
    }
    put("unitRanges", unitRanges)
    put("weekIds", weekIds)
    put("arranges", arranges)
    put("courseCodeNameMap", courseCodeNameMap)
    forward()
  }

  private def getDistinctWeekdays(courseCodes: Array[String], semester: Semester): List[Integer] = {
    val weekDayQuery = OqlBuilder.from(classOf[CourseActivity], "activity")
      .select("select distinct activity.time.weekday")
      .where("activity.lesson.course.code in (:peCourseCodes)", courseCodes)
      .where("activity.lesson.semester=:semester", semester)
      .orderBy("activity.time.weekday")
    entityDao.search(weekDayQuery).asInstanceOf[List[Integer]]
  }

  private def getDistinctUnitRanges(courseCodes: Array[String], semester: Semester): List[Array[Integer]] = {
    val unitRangeQuery = OqlBuilder.from(classOf[CourseActivity], "activity")
      .select("select distinct activity.time.startUnit, activity.time.endUnit")
      .where("activity.lesson.course.code in (:peCourseCodes)", courseCodes)
      .where("activity.lesson.courseSchedule.status=:status", CourseStatusEnum.ARRANGED)
      .where("activity.lesson.semester=:semester", semester)
    val orders = CollectUtils.newArrayList(new Order("activity.time.startUnit"), new Order("activity.time.endUnit"))
    unitRangeQuery.orderBy(orders)
    val res = entityDao.search(unitRangeQuery)
    CollectionUtils.transform(res, new Transformer() {

      def transform(input: AnyRef): AnyRef = {
        val arr = input.asInstanceOf[Array[Any]]
        val ret = Array.ofDim[Integer](arr.length)
        for (i <- 0 until arr.length) {
          ret(i) = arr(i).asInstanceOf[java.lang.Integer]
        }
        return ret
      }
    })
    res.asInstanceOf[List[Array[Integer]]]
  }

  protected def getLessons[T <: Entity[_]](semester: Semester, entity: T): List[Lesson] = {
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.project = :project", getProject)
    builder.where("lesson.semester =:semester", semester)
    val con = CourseLimitUtils.build(entity, "lgi")
    val params = con.getParams
    builder.where("exists(from lesson.teachClass.limitGroups lg join lg.items as lgi where (lgi.operator='" + 
      Operator.EQUAL.name() + 
      "' or lgi.operator='" + 
      Operator.IN.name() + 
      "') and " + 
      con.getContent + 
      ")", params.get(0), params.get(1), params.get(2))
    entityDao.search(builder)
  }

  protected def buildCourseTable(setting: CourseTableSetting, resource: Entity[_]): CourseTable = {
    val table = new CourseTable(resource, setting.getKind)
    var taskList: List[Lesson] = null
    if (CourseTable.CLASS == setting.getKind) {
      for (j <- 0 until setting.getTimes.length) {
        table.getActivities.addAll(teachResourceService.getAdminclassActivities(resource.asInstanceOf[Adminclass], 
          setting.getTimes()(j), if (setting.getForSemester) setting.getSemester else null))
      }
      if (setting.getIgnoreTask) return table
      val adminClass = resource.asInstanceOf[Adminclass]
      taskList = getLessons(setting.getSemester, resource)
      val plan = majorPlanService.majorPlanByAdminClass(adminClass)
      val courseGroups = CollectUtils.newHashSet()
      val newCourseGroups = CollectUtils.newHashMap()
      val courses = CollectUtils.newHashSet()
      val newLessonGroups = CollectUtils.newHashMap()
      for (lesson <- taskList) {
        val courseType = lesson.getCourseType
        if (newLessonGroups.containsKey(courseType)) {
          newLessonGroups.get(courseType).add(lesson)
        } else {
          val courseGroup = if (plan == null) null else plan.getGroup(lesson.getCourseType)
          newLessonGroups.put(courseType, CollectUtils.newHashSet(lesson))
          newCourseGroups.put(courseType, if ((null == courseGroup)) 0 else courseGroup.getCredits)
          if (null != courseGroup) courseGroups.add(courseGroup)
        }
        if (!courses.contains(lesson.getCourse)) {
          courses.add(lesson.getCourse)
        }
      }
      adminclassCourseGroups.put(adminClass, newCourseGroups)
      adminclassLessonGroups.put(adminClass, newLessonGroups)
    } else if (CourseTable.TEACHER == setting.getKind) {
      val teacher = resource.asInstanceOf[Teacher]
      put("teacher", resource)
      for (j <- 0 until setting.getTimes.length) {
        table.getActivities.addAll(teachResourceService.getTeacherActivities(teacher, setting.getTimes()(j), 
          if (setting.getForSemester) setting.getSemester else null))
      }
      if (setting.getIgnoreTask) {
        return table
      }
      taskList = if (setting.getForSemester) lessonService.getLessonByCategory(resource.getId, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.TEACHER), 
        setting.getSemester) else lessonService.getLessonByCategory(resource.getId, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.TEACHER), 
        semesterService.getSemestersOfOverlapped(setting.getSemester))
    } else if (CourseTable.STD == setting.getKind) {
      if (getLoginStudent != null) {
        val query = OqlBuilder.from(classOf[CourseArrangeSwitch], "switch")
        query.where("switch.semester = :semester", getSemester)
        query.where("switch.project = :project", getLoginStudent.getProject)
        query.where("switch.published is true")
        if (CollectUtils.isEmpty(entityDao.search(query))) {
          return table
        }
      }
      val student = resource.asInstanceOf[Student]
      for (j <- 0 until setting.getTimes.length) {
        table.getActivities.addAll(teachResourceService.getStdActivities(student, setting.getTimes()(j), 
          if (setting.getForSemester) setting.getSemester else null))
      }
      if (setting.getIgnoreTask) {
        return table
      }
      taskList = if (setting.getForSemester) lessonService.getLessonByCategory(resource.getId, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.STD), 
        setting.getSemester) else lessonService.getLessonByCategory(resource.getId, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.STD), 
        semesterService.getSemestersOfOverlapped(setting.getSemester))
    } else if (CourseTable.ROOM == setting.getKind) {
      val classroom = resource.asInstanceOf[Classroom]
      val notShowAll = getBool("notShowAll")
      for (j <- 0 until setting.getTimes.length) {
        if (notShowAll) {
          table.getActivities.addAll(teachResourceService.getRoomActivities(classroom, setting.getTimes()(j), 
            if (setting.getForSemester) setting.getSemester else null, getDeparts, getProject))
        } else {
          table.getActivities.addAll(teachResourceService.getRoomActivities(classroom, setting.getTimes()(j), 
            if (setting.getForSemester) setting.getSemester else null))
        }
      }
      if (setting.getIgnoreTask) {
        return table
      }
      table.extractTaskFromActivity()
    } else if (CourseTable.PROGRAM == setting.getKind) {
      val program = resource.asInstanceOf[Program]
      put("program", resource)
      for (j <- 0 until setting.getTimes.length) {
        table.getActivities.addAll(teachResourceService.getProgramActivities(program, setting.getTimes()(j), 
          if (setting.getForSemester) setting.getSemester else null))
      }
      if (setting.getIgnoreTask) {
        return table
      }
      taskList = if (setting.getForSemester) getLessons(setting.getSemester, program) else getLessons(putSemester(null), 
        program)
    }
    if (null == table.getLessons) table.setLessons(taskList)
    table
  }

  protected def getTimesFormPage(semester: Semester): Array[CourseTime] = {
    var startWeek = getInt("startWeek")
    var endWeek = getInt("endWeek")
    if (null == startWeek) startWeek = new java.lang.Integer(1)
    if (null == endWeek) endWeek = new java.lang.Integer(semester.getWeeks)
    if (startWeek.intValue() < 1) startWeek = new java.lang.Integer(1)
    if (endWeek.intValue() > semester.getWeeks) endWeek = new java.lang.Integer(semester.getWeeks)
    put("startWeek", startWeek)
    put("endWeek", endWeek)
    val courseTime = TimeUnitUtil.buildTimeUnits(2, startWeek.intValue(), endWeek.intValue(), CourseTime.CONTINUELY)
    Array(courseTime)
  }

  protected def getSemester(): Semester = {
    var semester = populate(classOf[Semester], "semester").asInstanceOf[Semester]
    val semesterId = getInt("semester.id")
    if (null != semesterId) {
      semester = entityDao.get(classOf[Semester], semesterId)
    }
    put("semester", semester)
    semester
  }

  private def getLastSubString(str: String): String = {
    if (null == str) {
      return null
    }
    val subStrArr = Strings.split(str, ".")
    if (subStrArr.length > 0) {
      subStrArr(subStrArr.length - 1)
    } else {
      null
    }
  }

  def getArrangedLessons(): String = {
    val lessonNo = get("lessonNo")
    val courseNo = get("courseNo")
    val courseName = get("courseName")
    val teachDepartId = getInt("teachDepartId")
    val semesterId = getInt("semesterId")
    if (Strings.isEmpty(courseName) && Strings.isEmpty(lessonNo) && 
      Strings.isEmpty(courseNo) && 
      teachDepartId == null || 
      semesterId == null) {
      put("lessons", Collections.emptyList())
    } else {
      val builder = OqlBuilder.from(classOf[Lesson], "lesson")
      if (Strings.isNotEmpty(lessonNo)) {
        builder.where(Condition.like("lesson.no", lessonNo))
      }
      if (Strings.isNotEmpty(courseNo)) {
        builder.where(Condition.like("lesson.course.code", courseNo))
      }
      if (Strings.isNotEmpty(courseName)) {
        builder.where(Condition.like("lesson.course.name", courseName))
      }
      if (null != teachDepartId) {
        builder.where("lesson.teachDepart.id = :teachDepartId", teachDepartId)
      }
      builder.where("lesson.semester.id = :semesterId", semesterId)
      builder.where("lesson.courseSchedule.status = :status", CourseStatusEnum.ARRANGED)
      builder.where("lesson.project.id=:projectid1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
      put("lessons", entityDao.search(builder))
    }
    forward("arrangedLessons")
  }

  def getLessonActivities(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    var lessons = CollectUtils.newArrayList()
    val activities = CollectUtils.newArrayList()
    val semesterId = getInt("semesterId")
    if (null == semesterId) {
      return forwardError("没有找到学期")
    }
    val semester = entityDao.get(classOf[Semester], semesterId)
    if (ArrayUtils.isNotEmpty(lessonIds)) {
      lessons = entityDao.get(classOf[Lesson], lessonIds)
      for (lesson <- lessons) {
        activities.addAll(lesson.getCourseSchedule.getActivities)
      }
    } else {
      return forwardError("没有找到教学任务")
    }
    put("startWeek", new java.lang.Integer(1))
    put("endWeek", new java.lang.Integer(semester.getWeeks))
    put("weekList", WeekDays.All)
    put("activityList", activities)
    put("lessons", lessons)
    put("lessonIds", get("lessonIds"))
    put("semester", semester)
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, semester, null))
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    if (getBool("print")) {
      forward("courseTableLessonForPrint")
    } else {
      forward("courseTableLesson")
    }
  }

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }

  def setStudentService(studentService: StudentService) {
    this.studentService = studentService
  }

  def setTeachResourceService(teachResourceService: TeachResourceService) {
    this.teachResourceService = teachResourceService
  }

  def setMajorPlanService(majorPlanService: MajorPlanService) {
    this.majorPlanService = majorPlanService
  }

  def setBaseInfoSearchHelper(baseInfoSearchHelper: BaseInfoSearchHelper) {
    this.baseInfoSearchHelper = baseInfoSearchHelper
  }

  def setStdSearchHelper(stdSearchHelper: StdSearchHelper) {
    this.stdSearchHelper = stdSearchHelper
  }

  def setTimeSettingService(timeSettingService: TimeSettingService) {
    this.timeSettingService = timeSettingService
  }

  def setLessonFilterStrategyFactory(lessonFilterStrategyFactory: LessonFilterStrategyFactory) {
    this.lessonFilterStrategyFactory = lessonFilterStrategyFactory
  }
}
