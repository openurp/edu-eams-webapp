package org.openurp.edu.eams.teach.grade.course.web.helper



import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.ems.dictionary.service.BaseCodeService
import org.beangle.security.blueprint.User
import org.beangle.struts2.helper.ContextHelper
import org.beangle.struts2.helper.Params
import org.openurp.base.Semester
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.service.CourseGradeComparator
import org.openurp.edu.eams.teach.grade.lesson.service.CourseSegStat
import org.openurp.edu.eams.teach.grade.lesson.service.GradeSegStats
import org.openurp.edu.eams.teach.grade.lesson.service.LessonGradeService
import org.openurp.edu.eams.teach.grade.lesson.service.LessonSegStat
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.util.stat.FloatSegment
import org.openurp.edu.eams.web.action.BaseAction



class TeachClassGradeHelper extends BaseAction {

  protected var baseCodeService: BaseCodeService = _

  protected var courseGradeService: CourseGradeService = _

  protected var lessonGradeService: LessonGradeService = _

  protected var calculator: CourseGradeCalculator = _

  protected var settings: CourseGradeSettings = _

  def stat(lesson: Lesson) {
    val gradeTypeIds = Array(GradeTypeConstants.USUAL_ID, GradeTypeConstants.MIDDLE_ID, GradeTypeConstants.END_ID, GradeTypeConstants.GA_ID)
    if (Objects.!=(get("kind"), GradeSegStats.COURSE)) {
      statLesson(CollectUtils.newArrayList(lesson), gradeTypeIds)
      put("kind", GradeSegStats.LESSON)
    } else {
      val lessonIdSeq = get("lesson.id")
      var courses: List[Course] = null
      var semester: Semester = null
      if (Strings.isNotEmpty(lessonIdSeq)) {
        val query1 = OqlBuilder.from(classOf[Course], "course")
        var hql = "exists (from " + classOf[Lesson].getName + 
          " lesson where lesson.course = course and lesson.id in (:lessonIds))"
        query1.where(hql, Strings.splitToLong(lessonIdSeq))
        courses = entityDao.search(query1)
        val query2 = OqlBuilder.from(classOf[Semester], "semester")
        hql = "exists (from " + classOf[Lesson].getName + 
          " lesson where lesson.semester = semester and lesson.id in (:lessonIds))"
        query2.where(hql, Strings.splitToLong(lessonIdSeq))
        semester = entityDao.search(query2).iterator().next()
      }
      statCourse(courses, semester, gradeTypeIds)
      put("kind", GradeSegStats.COURSE)
    }
  }

  def statLesson(lessons: List[Lesson], gradeTypeIds: Array[Integer]) {
    val scoreSegmentsLength = Params.getInt("scoreSegmentsLength")
    val segStat = new LessonSegStat(scoreSegmentsLength)
    var i = 0
    var it = segStat.getScoreSegments.iterator()
    while (it.hasNext) {
      val scoreSegment = it.next()
      scoreSegment.setMin(getFloat("segStat.scoreSegments[" + i + "].min").floatValue())
      scoreSegment.setMax(getFloat("segStat.scoreSegments[" + i + "].max").floatValue())
      i += 1
    }
    segStat.buildScoreSegments()
    ContextHelper.put("segStat", segStat)
    val stats = CollectUtils.newArrayList()
    val gradeTypes = entityDao.get(classOf[GradeType], gradeTypeIds)
    for (lesson <- lessons) {
      val grades = getCourseGrades(lesson)
      val stat = new LessonSegStat(lesson, null, grades)
      stat.setScoreSegments(segStat.getScoreSegments)
      stat.stat(gradeTypes)
      stats.add(stat)
    }
    put("courseStats", stats)
  }

  def statCourse(courses: List[Course], semester: Semester, gradeTypeIds: Array[Integer]) {
    val scoreSegmentsLength = Params.getInt("scoreSegmentsLength").intValue()
    val segStat = new CourseSegStat(scoreSegmentsLength)
    var i = 0
    var it = segStat.getScoreSegments.iterator()
    while (it.hasNext) {
      val scoreSegment = it.next()
      scoreSegment.setMin(getFloat("segStat.scoreSegments[" + i + "].min").floatValue())
      scoreSegment.setMax(getFloat("segStat.scoreSegments[" + i + "].max").floatValue())
      i += 1
    }
    segStat.buildScoreSegments()
    ContextHelper.put("segStat", segStat)
    val stats = CollectUtils.newArrayList()
    val gradeTypes = entityDao.get(classOf[GradeType], gradeTypeIds)
    for (course <- courses) {
      val grades = getCourseGrades(course, semester)
      val stat = new CourseSegStat(course, semester, grades)
      stat.setScoreSegments(segStat.getScoreSegments)
      stat.stat(gradeTypes)
      stats.add(stat)
    }
    ContextHelper.put("courseStats", stats)
  }

  def report(lessons: List[Lesson], gradeTypeIds: Array[Integer]) {
    var gradeTypes = CollectUtils.newArrayList()
    if (ArrayUtils.isNotEmpty(gradeTypeIds)) {
      gradeTypes = entityDao.get(classOf[GradeType], gradeTypeIds)
    }
    val makeupType = baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.MAKEUP_ID).asInstanceOf[GradeType]
    val reports = CollectUtils.newArrayList()
    for (lesson <- lessons) {
      if (CollectUtils.isEmpty(gradeTypes) || 
        gradeTypes.size == 1 && 
        gradeTypes.contains(new GradeType(GradeTypeConstants.GA_ID))) {
        val setting = settings.getSetting(lesson.getProject)
        val gaElGradeTypes = setting.getGaElementTypes
        for (gradeType <- gaElGradeTypes if !gradeTypes.contains(gradeType)) {
          gradeTypes.add(entityDao.get(classOf[GradeType], gradeType.id))
        }
        val gaGradeType = entityDao.get(classOf[GradeType], GradeTypeConstants.GA_ID)
        if (!gradeTypes.contains(gaGradeType)) {
          gradeTypes.add(gaGradeType)
        }
      }
      Collections.sort(gradeTypes, new PropertyComparator("code"))
      val query = OqlBuilder.from(classOf[CourseGrade], "grade")
      query.where("grade.lesson=:task", lesson)
      query.where("exists (from grade.examGrades examGrade where examGrade.gradeType in (:gradeTypes))", 
        gradeTypes)
      query.orderBy("grade.std.code")
      val courseGradeState = courseGradeService.getState(lesson)
      val usercode = courseGradeState.getOperator
      var username: String = null
      if (Strings.isNotBlank(usercode)) {
        val users = entityDao.get(classOf[User], "name", usercode)
        if (users.size == 1) {
          username = users.get(0).getFullname
        }
      }
      reports.add(new TeachClassGrade(gradeTypes, lesson, entityDao.search(query), courseGradeService.getState(lesson), 
        username))
    }
    ContextHelper.put("USUAL_ID", GradeTypeConstants.USUAL_ID)
    ContextHelper.put("END", new GradeType(GradeTypeConstants.END_ID))
    ContextHelper.put("FINAL", new GradeType(GradeTypeConstants.FINAL_ID))
    ContextHelper.put("GA", new GradeType(GradeTypeConstants.GA_ID))
    ContextHelper.put("MAKEUP", makeupType)
    ContextHelper.put("REEXAM", CourseTakeType.REEXAM)
    ContextHelper.put("PUBLISH_STATUS", Grade.Status.PUBLISHED)
    ContextHelper.put("reports", reports)
  }

  def info(lesson: Lesson) {
    val grades = entityDao.search(OqlBuilder.from(classOf[CourseGrade], "cg").where("cg.lesson=:lesson", 
      lesson))
    var gradeTypeSet = CollectUtils.newHashSet()
    val gradeTypeIds = getIntIds("gradeType")
    if (null != gradeTypeIds && gradeTypeIds.length > 0) {
      gradeTypeSet = CollectUtils.newHashSet(baseCodeService.getCodes(classOf[GradeType], gradeTypeIds))
    }
    val existed = CollectUtils.newHashSet()
    for (grade <- grades; eg <- grade.getExamGrades) existed.add(eg.gradeType)
    if (gradeTypeSet.isEmpty) gradeTypeSet.addAll(existed) else gradeTypeSet.retainAll(existed)
    val gradeTypes = CollectUtils.newArrayList(gradeTypeSet)
    var orderBy = get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "std.code"
    } else {
      if (orderBy.startsWith("courseGrade.")) {
        orderBy = Strings.substringAfter(orderBy, "courseGrade.")
        put("orderModify", true)
      }
    }
    val orders = Order.parse(orderBy)
    if (CollectUtils.isNotEmpty(orders)) {
      val order = orders.get(0)
      Collections.sort(grades, new CourseGradeComparator(order.getProperty, order.isAscending, gradeTypes))
    }
    ContextHelper.put("gradeTypes", gradeTypes)
    ContextHelper.put("grades", grades)
    ContextHelper.put("NORMAL", baseCodeService.getCode(classOf[ExamStatus], ExamStatus.NORMAL))
    ContextHelper.put("FINAL", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.FINAL_ID))
    ContextHelper.put("lesson", lesson)
    val courseGradeStates = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    var gradeState: CourseGradeState = null
    if (CollectUtils.isNotEmpty(courseGradeStates)) {
      gradeState = courseGradeStates.get(0)
    }
    ContextHelper.put("gradeState", gradeState)
  }

  def setBaseCodeService(baseCodeService: BaseCodeService) {
    this.baseCodeService = baseCodeService
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }

  def setLessonGradeService(lessonGradeService: LessonGradeService) {
    this.lessonGradeService = lessonGradeService
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }

  private def getCourseGrades(course: Course, semester: Semester): List[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    query.where("courseGrade.course = :course", course)
    query.where("courseGrade.semester = :semester", semester)
    entityDao.search(query)
  }

  private def getCourseGrades(lesson: Lesson): List[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    query.where("courseGrade.lesson = :lesson and courseGrade.status<>:input", lesson, Grade.Status.NEW)
    entityDao.search(query)
  }

  def setSettings(settings: CourseGradeSettings) {
    this.settings = settings
  }
}
