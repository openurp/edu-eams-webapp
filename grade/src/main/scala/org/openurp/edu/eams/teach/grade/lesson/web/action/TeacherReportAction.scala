package org.openurp.edu.eams.teach.grade.lesson.web.action




import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.struts2.convention.route.Action
import org.openurp.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.service.CourseGradeComparator
import org.openurp.edu.eams.teach.grade.course.service.MakeupStdStrategy
import org.openurp.edu.eams.teach.grade.course.web.action.TeacherAction
import org.openurp.edu.eams.teach.grade.course.web.helper.TeachClassGradeHelper
import org.openurp.edu.eams.teach.grade.lesson.service.GradeSegStats
import org.openurp.edu.eams.teach.grade.lesson.service.LessonGradeService
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.model.GradeRateConfig
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class TeacherReportAction extends SemesterSupportAction {

  var lessonService: LessonService = _

  var courseGradeService: CourseGradeService = _

  var lessonGradeService: LessonGradeService = _

  var teachClassGradeHelper: TeachClassGradeHelper = _

  var lessonSearchHelper: LessonSearchHelper = _

  var settings: CourseGradeSettings = _

  var makeupStdStrategy: MakeupStdStrategy = _

  protected def checkLessonPermission(lesson: Lesson, teacher: Teacher): String = {
    if (null == teacher) {
      return "只有教师才可以录入成绩"
    }
    if (!lesson.getTeachers.contains(teacher)) {
      return "没有权限"
    }
    null
  }

  def index(): String = {
    setSemesterDataRealm(hasStdTypeDepart)
    put("courseTypes", lessonService.courseTypesOfSemester(getProjects, getDeparts, getAttribute("semester").asInstanceOf[Semester]))
    put("teachDepartList", lessonService.teachDepartsOfSemester(getProjects, getDeparts, getAttribute("semester").asInstanceOf[Semester]))
    put("departmentList", lessonService.teachDepartsOfSemester(getProjects, getDeparts, getAttribute("semester").asInstanceOf[Semester]))
    forward()
  }

  def search(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("对不起，该功能只开放给教师用户！")
    }
    val builder = lessonSearchHelper.buildQuery()
    builder.where("exists (select cgs.lesson.id from " + classOf[CourseGradeState].getName + 
      " cgs where lesson.id = cgs.lesson.id and cgs.status = :status)", Grade.Status.PUBLISHED)
    builder.where("exists(from " + builder.getAlias + ".teachers teacher where teacher=:teacher)", teacher)
    val lessons = entityDao.search(builder)
    val unpassedMap = Collections.newMap[Any]
    if (!lessons.isEmpty) {
      val builder2 = OqlBuilder.from(classOf[CourseGrade], "cg")
      builder2.where("cg.lesson in(:lessons) and cg.passed=false", lessons)
      builder2.select("cg.lesson.id,count(*)").groupBy("cg.lesson.id")
      val rs = entityDao.search(builder2)
      for (data <- rs) {
        val datas = data.asInstanceOf[Array[Any]]
        unpassedMap.put(datas(0).asInstanceOf[java.lang.Long], datas(1).asInstanceOf[Number])
      }
    }
    put("unpassedMap", unpassedMap)
    put("lessons", lessons)
    put("FINAL", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.FINAL_ID))
    forward()
  }

  def unpassed(): String = {
    val lesson = getEntity(classOf[Lesson], "lesson")
    val msg = checkLessonPermission(lesson, getLoginTeacher)
    if (null == msg) {
      return forwardError(msg)
    }
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    query.where("courseGrade.lesson = :lesson and courseGrade.passed=false", lesson)
    val grades = entityDao.search(query)
    val gradeTypes = Collections.newBuffer[Any]
    val exited = Collections.newSet[Any]
    for (grade <- grades; eg <- grade.getExamGrades) exited.add(eg.gradeType)
    gradeTypes.addAll(exited)
    var orderBy = get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "std.code"
    } else {
      if (orderBy.startsWith("courseGrade.")) orderBy = Strings.substringAfter(orderBy, "courseGrade.")
    }
    val orders = Order.parse(orderBy)
    if (Collections.isNotEmpty(orders)) {
      val order = orders.get(0)
      Collections.sort(grades, new CourseGradeComparator(order.getProperty, order.isAscending, gradeTypes))
    }
    put("gradeTypes", gradeTypes)
    put("grades", grades)
    put("NORMAL", baseCodeService.getCode(classOf[ExamStatus], ExamStatus.NORMAL))
    put("FINAL", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.FINAL_ID))
    put("lesson", lesson)
    forward()
  }

  def report(): String = {
    val lessonIds = getLongIds("lesson")
    if (null == lessonIds || lessonIds.length == 0) {
      return forwardError("error.parameters.needed")
    }
    var gradeTypeIdArray = getIntIds("gradeType")
    if (null == gradeTypeIdArray) {
      gradeTypeIdArray = Array(GradeTypeConstants.USUAL_ID, GradeTypeConstants.MIDDLE_ID, GradeTypeConstants.END_ID, GradeTypeConstants.GA_ID)
    }
    val gradeTypeIds = Collections.newHashSet(gradeTypeIdArray)
    val makeupIds = Collections.newHashSet(GradeTypeConstants.MAKEUP_ID, GradeTypeConstants.DELAY_ID)
    val isMakeup = CollectionUtils.containsAny(gradeTypeIds, makeupIds)
    if (isMakeup) gradeTypeIds.addAll(makeupIds)
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    val ownLessons = Collections.newBuffer[Any]
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("对不起,该功能只开放给教师用户!")
    }
    for (lesson <- lessons if null == checkLessonPermission(lesson, teacher)) {
      ownLessons.add(lesson)
    }
    if (ownLessons.isEmpty) return forwardError("没有权限")
    teachClassGradeHelper.report(ownLessons, gradeTypeIds.toArray(Array.ofDim[Integer](gradeTypeIds.size)))
    val query = OqlBuilder.from(classOf[GradeRateConfig], "config")
      .where("config.project=:project", getProject)
    val gradeConfigMap = Collections.newMap[Any]
    for (config <- entityDao.search(query)) {
      gradeConfigMap.put(String.valueOf(config.getScoreMarkStyle.id), config)
    }
    put("MIDDLE_ID", GradeTypeConstants.MIDDLE_ID)
    put("NORMAL", ExamStatus.NORMAL)
    put("GA_ID", GradeTypeConstants.GA_ID)
    put("gradeConfigMap", gradeConfigMap)
    if (isMakeup) "reportMakeup" else "reportGa"
  }

  def blank(): String = {
    val lessonIds = getLongIds("lesson")
    val lessonList = entityDao.get(classOf[Lesson], lessonIds)
    val lessons = Collections.newBuffer[Any]
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("对不起,该功能只开放给教师用户!")
    }
    for (lesson <- lessonList if null == checkLessonPermission(lesson, teacher)) {
      lessons.add(lesson)
    }
    if (lessons.isEmpty) return forwardError("没有权限")
    put("lessons", lessons)
    var gradeTypes = Collections.newBuffer[Any]
    val courseTakes = Collections.newMap[Any]
    var makeup = getBool("makeup")
    val gradeTypeId = getInt("gradeType.id")
    if (null != gradeTypeId && 
      (gradeTypeId == GradeTypeConstants.MAKEUP_ID || gradeTypeId == GradeTypeConstants.DELAY_ID)) {
      makeup = true
    }
    if (makeup) {
      gradeTypes = baseCodeService.getCodes(classOf[GradeType], GradeTypeConstants.DELAY_ID, GradeTypeConstants.MAKEUP_ID)
      for (lesson <- lessons) courseTakes.put(lesson, makeupStdStrategy.getCourseTakes(lesson))
      val examTypes = Collections.newSet[Any]
      for (`type` <- gradeTypes) examTypes.add(`type`.getExamType)
      put("stdExamTakeMap", getStdExamTakeMap(lessons, examTypes))
    } else {
      for (lesson <- lessons) {
        val takes = Collections.newBuffer[Any](lesson.getTeachClass.getCourseTakes)
        courseTakes.put(lesson, takes)
      }
      val setting = settings.getSetting(getProject)
      for (gradeType <- setting.getGaElementTypes) {
        val freshedGradeType = entityDao.get(classOf[GradeType], gradeType.id)
        if (null != freshedGradeType) gradeTypes.add(freshedGradeType)
      }
      val ga = entityDao.get(classOf[GradeType], GradeTypeConstants.GA_ID)
      put("GA", ga)
      gradeTypes.add(ga)
    }
    put("courseTakeMap", courseTakes)
    Collections.sort(gradeTypes, new PropertyComparator("code"))
    put("gradeTypes", gradeTypes)
    if (makeup) "blankMakeuptable" else "blankGatable"
  }

  protected def getStdExamTakeMap(lessons: List[Lesson], examTypes: Set[ExamType]): Map[String, ExamTake] = {
    if (examTypes.isEmpty) {
      return Collections.newMap[Any]
    }
    val query = OqlBuilder.from(classOf[ExamTake], "examTake").where("examTake.lesson  in(:lessons)", 
      lessons)
    if (Collections.isNotEmpty(examTypes)) query.where("examTake.examType in (:examTypes)", examTypes)
    val stdExamTypeMap = Collections.newMap[Any]
    val examTakes = entityDao.search(query)
    for (examTake <- examTakes) {
      stdExamTypeMap.put(examTake.getLesson.id + "_" + examTake.getStd.id, examTake)
    }
    stdExamTypeMap
  }

  def stat(): String = {
    val gradeTypeIds = Array(GradeTypeConstants.END_ID, GradeTypeConstants.GA_ID)
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("对不起,该功能只开放给教师用户!")
    }
    if (Objects.!=(get("kind"), GradeSegStats.COURSE)) {
      val lessonIdSeq = getLongIds("lesson")
      if (null == lessonIdSeq || lessonIdSeq.length == 0) {
        return forwardError("error.parameters.needed")
      }
      val lessons = entityDao.get(classOf[Lesson], lessonIdSeq)
      val ownLessons = Collections.newBuffer[Any]
      for (lesson <- lessons if null == checkLessonPermission(lesson, teacher)) {
        ownLessons.add(lesson)
      }
      if (ownLessons.isEmpty) return forwardError("没有权限")
      teachClassGradeHelper.statLesson(ownLessons, gradeTypeIds)
      put("kind", GradeSegStats.LESSON)
    } else {
      val lessonIdSeq = get("lesson.ids")
      var courses: List[Course] = null
      var semester: Semester = null
      if (Strings.isNotEmpty(lessonIdSeq)) {
        val lessonIds = Strings.splitToLong(lessonIdSeq)
        var query1 = OqlBuilder.from(classOf[Lesson], "lesson")
        query1.where("lesson.id in (:lessonIds)", lessonIds)
        query1.where("exists(from lesson.teachers teacher where teacher=:teacher)", teacher)
        query1.select("distinct lesson.course")
        courses = entityDao.search(query1)
        if (courses.isEmpty) {
          return forwardError("没有权限")
        }
        query1 = OqlBuilder.from(classOf[Lesson], "lesson")
        query1.where("lesson.id in (:lessonIds)", Strings.splitToLong(lessonIdSeq))
        query1.where("exists(from lesson.teachers teacher where teacher=:teacher)", teacher)
        query1.select("distinct lesson.semester")
        semester = entityDao.search(query1).iterator().next().asInstanceOf[Semester]
      }
      teachClassGradeHelper.statCourse(courses, semester, gradeTypeIds)
      put("kind", GradeSegStats.COURSE)
    }
    forward()
  }

  def reportForExam(): String = {
    forward(new Action(classOf[TeacherAction], "reportForExam"))
  }

  protected def getExportDatas(): Iterable[_] = {
    val lessonIdSeq = get("lessonIds")
    val teacher = getLoginTeacher
    if (null == teacher) {
      return Collections.emptyList()
    }
    if (Strings.isEmpty(lessonIdSeq)) {
      val builder = lessonSearchHelper.buildQuery()
      builder.where("exists (select cgs.lesson.id from " + classOf[CourseGradeState].getName + 
        " cgs where lesson.id = cgs.lesson.id and cgs.status = :status)", Grade.Status.PUBLISHED)
      builder.where("exists(from " + builder.getAlias + ".teachers teacher where teacher=:teacher)", 
        teacher)
      populateConditions(builder)
      builder.orderBy(get(Order.ORDER_STR))
      entityDao.search(builder)
    } else {
      val lessons = entityDao.get(classOf[Lesson], Strings.splitToLong(lessonIdSeq))
      val result = Collections.newBuffer[Any]
      for (lesson <- lessons if null == checkLessonPermission(lesson, teacher)) {
        result.add(lesson)
      }
      result
    }
  }

  def printStdListForDuty(): String = {
    forward(new Action("teachTask", "printStdListForDuty"))
  }
}
