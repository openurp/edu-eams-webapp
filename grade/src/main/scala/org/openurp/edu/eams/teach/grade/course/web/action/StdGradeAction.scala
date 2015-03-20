package org.openurp.edu.eams.teach.grade.course.web.action

import org.openurp.edu.eams.teach.Grade.Status.CONFIRMED
import org.openurp.edu.eams.teach.Grade.Status.PUBLISHED
import java.io.IOException
import java.util.Date



import javax.servlet.http.HttpServletResponse
import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.grade.course.service.MarkStyleHelper
import org.openurp.edu.eams.teach.grade.course.service.StdGradeService
import org.openurp.edu.eams.teach.grade.course.web.helper.CourseGradeHelper
import org.openurp.edu.eams.teach.grade.course.web.helper.StringBuilderHelper
import org.openurp.edu.eams.teach.grade.model.GradeRateConfig
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.GradeCourseTypeProvider
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson



class StdGradeAction extends StdGradeSearchAction {

  protected var calculator: CourseGradeCalculator = _

  protected var courseGradeHelper: CourseGradeHelper = _

  protected var gradeRateService: GradeRateService = _

  protected var markStyleHelper: MarkStyleHelper = _

  private var stdGradeService: StdGradeService = _

  private var stringBuilderHelper: StringBuilderHelper = _

  protected var gradeCourseTypeProvider: GradeCourseTypeProvider = _

  def batchAdd(): String = {
    putSemester(getProject)
    put("markStyles", gradeRateService.getMarkStyles(getProject))
    put("stdTypeList", getStdTypes)
    put("gradeTypes", entityDao.search(OqlBuilder.from(classOf[GradeType], "gt").where("gt.id!=" + GradeTypeConstants.FINAL_ID)
      .cacheable()))
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("courseTakeTypes", baseCodeService.getCodes(classOf[CourseTakeType]))
    put("configs", entityDao.getAll(classOf[GradeRateConfig]))
    put("calendars", semesterService.getCalendars(getProjects))
    put("batchAddSemesterId", getLong("courseGrade.semester.id"))
    forward()
  }

  def getCourseInfo() {
    val lessonNo = get("lessonNo")
    val semesterId = getInt("semesterId")
    val resMap = CollectUtils.newHashMap()
    if (!Strings.isEmpty(lessonNo) && null != semesterId) {
      val builder = OqlBuilder.from(classOf[Lesson], "lesson")
      builder.where("lesson.no = :lessonNo", lessonNo)
      builder.where("lesson.semester.id = :semesterId", semesterId)
      builder.where("lesson.project =:project", getProject)
      val lessons = entityDao.search(builder)
      if (lessons.size >= 1) {
        val lesson = lessons.get(0)
        val gradeState = courseGradeService.getState(lesson)
        resMap.put("lessonId", lesson.id.toString)
        resMap.put("courseCode", lesson.getCourse.getCode.trim())
        resMap.put("courseName", lesson.getCourse.getName.trim())
        var markStyleId: java.lang.Integer = null
        var markStyleName = ""
        if (gradeState == null) {
          if (null != lesson.getCourse.getExamMode && 
            lesson.getCourse.getExamMode.id != ExamMode.NORMAL) {
            markStyleId = ScoreMarkStyle.RANK_EN
            markStyleName = "英文等级制"
          } else {
            markStyleId = ScoreMarkStyle.PERCENT
            markStyleName = "百分制"
          }
        } else {
          markStyleId = gradeState.getScoreMarkStyle.id
          markStyleName = gradeState.getScoreMarkStyle.getName
        }
        resMap.put("markStyleId", markStyleId.toString)
        resMap.put("markStyleName", markStyleName)
      } else if (lessons.size > 1) {
        resMap.put("responseStr", "dataExeption")
      } else {
        resMap.put("responseStr", "noData")
      }
    } else {
      resMap.put("responseStr", "reqIsNull")
    }
    resMap.put("semesterId", semesterId.toString)
    val response = getResponse
    response.setCharacterEncoding("UTF-8")
    response.getWriter.print(stringBuilderHelper.getResponseJSON(resMap))
  }

  def getCourseByCode() {
    val courseCode = get("courseCode")
    val courseMap = CollectUtils.newHashMap()
    if (!Strings.isEmpty(courseCode)) {
      val courses = entityDao.get(classOf[Course], "code", courseCode.trim())
      if (CollectUtils.isEmpty(courses)) {
        courseMap.put("responseStr", "noData")
      } else if (courses.size > 1) {
        courseMap.put("responseStr", "dataException")
      } else {
        val course = courses.get(0)
        courseMap.put("courseName", course.getName)
        courseMap.put("credit", course.getCredits + "")
        courseMap.put("courseId", course.id.toString)
      }
    } else {
      courseMap.put("responseStr", "reqIsNull")
    }
    val response = getResponse
    response.setCharacterEncoding("UTF-8")
    response.getWriter.print(stringBuilderHelper.getResponseJSON(courseMap))
  }

  def getStuInfo() {
    val lessonId = getLong("lessonId")
    val stdCode = get("stdCode")
    val stdMap = CollectUtils.newHashMap()
    if (null != lessonId && "" != stdCode) {
      val stus = entityDao.get(classOf[Student], "code", stdCode)
      if (CollectUtils.isEmpty(stus)) {
        stdMap.put("responseStr", "noStu")
      } else if (stus.size > 1) {
        stdMap.put("responseStr", "dataException")
      } else {
        val std = stus.get(0)
        val takes = getCourseTakeInfo(std, lessonId)
        if (CollectUtils.isEmpty(takes)) {
          stdMap.put("dataException", "noTakes")
        } else if (takes.size > 1) {
          stdMap.put("dataException", "takeDataException")
        } else {
          stdMap.put("stdId", std.id.toString)
          stdMap.put("stdName", std.getName)
        }
      }
    } else {
      stdMap.put("responseStr", "reqIsNull")
    }
    val response = getResponse
    response.setCharacterEncoding("UTF-8")
    response.getWriter.print(stringBuilderHelper.getResponseJSON(stdMap))
  }

  def getStudentByCode() {
    val semesterId = getInt("semesterId")
    val stuCode = get("stdCode")
    val courseCode = get("courseCode")
    val gradeTypeId = getInt("gradeTypeId")
    val resMap = CollectUtils.newHashMap()
    if (semesterId != null && Strings.isNotEmpty(stuCode) && Strings.isNotEmpty(courseCode)) {
      val semester = entityDao.get(classOf[Semester], semesterId)
      val grade = getCourseGrade(stuCode.trim(), semester, courseCode, gradeTypeId)
      resMap.put("gradeId", if (grade.id == null) "" else grade.id.toString)
      if (null != grade.getStd) {
        resMap.put("stdName", grade.getStd.getName)
        val examGrade = grade.getExamGrade(Model.newInstance(classOf[GradeType], gradeTypeId))
        resMap.put("examGradeScore", if (examGrade == null) "" else if (examGrade.getScore == null) "" else examGrade.getScore.toString)
        resMap.put("examGradeStatus", if (examGrade == null) "" else if (examGrade.getExamStatus == null) "" else examGrade.getExamStatus.getName)
        resMap.put("lessonNo", if (grade.getLessonNo == null) "" else grade.getLessonNo)
        val courseType = grade.getCourseType
        resMap.put("courseTypeId", if (courseType == null) "" else courseType.id.toString)
        val courseTakeType = grade.getCourseTakeType
        resMap.put("courseTakeTypeId", if (courseTakeType == null) "" else courseTakeType.id.toString)
        resMap.put("flag", "true")
      } else {
        resMap.put("flag", "false")
      }
    }
    val response = getResponse
    response.setCharacterEncoding("UTF-8")
    response.getWriter.print(stringBuilderHelper.getResponseJSON(resMap))
  }

  private def getCourseGrade(stdCode: String, 
      semester: Semester, 
      courseCode: String, 
      gradeTypeId: java.lang.Integer): CourseGrade = {
    var grade = Model.newInstance(classOf[CourseGrade])
    val gradeType = entityDao.get(classOf[GradeType], gradeTypeId)
    grade.setStatus(PUBLISHED)
    grade.setSemester(semester)
    val courses = entityDao.get(classOf[Course], "code", courseCode)
    var course: Course = null
    if (courses.size == 1) {
      course = courses.get(0)
      grade.setCourse(course)
    }
    if (null == course) {
      return grade
    }
    val stds = entityDao.get(classOf[Student], "code", stdCode)
    if (stds.isEmpty || stds.size != 1) {
      return grade
    }
    val std = stds.get(0)
    grade.setStd(std)
    grade.setProject(std.getProject)
    val gradeQuery = OqlBuilder.from(classOf[CourseGrade], "grade")
    gradeQuery.where("grade.semester = :semester", semester)
    gradeQuery.where("grade.std = :std", grade.getStd)
    gradeQuery.where("grade.course.code = :courseCode", courseCode)
    val grades = entityDao.search(gradeQuery)
    if (grades.size > 0) {
      grade = grades.get(0).asInstanceOf[CourseGrade]
    }
    if (null != grade.getExamGrade(gradeType)) {
      return grade
    } else {
      var takes = CollectUtils.newArrayList()
      val takeQuery = OqlBuilder.from(classOf[CourseTake], "take").where("take.lesson.course.code = :courseCode", 
        courseCode)
        .where("take.std = :std", grade.getStd)
        .where("take.lesson.semester = :semester", semester)
      takes = entityDao.search(takeQuery)
      if (takes.size == 1) {
        val take = takes.get(0).asInstanceOf[CourseTake]
        if (grade.isTransient) {
          grade.setCourseTakeType(take.getCourseTakeType)
          grade.setLesson(take.getLesson)
          grade.setCourse(take.getLesson.getCourse)
          grade.setLessonNo(take.getLesson.getNo)
          grade.setCourseType(take.getLesson.getCourseType)
          grade.setProject(course.getProject)
        }
      } else {
        grade.setCourse(course)
        grade.setCourseTakeType(entityDao.get(classOf[CourseTakeType], CourseTakeType.NORMAL))
      }
      if (grade.isTransient) {
        grade.setCourseType(gradeCourseTypeProvider.getCourseType(grade.getStd, grade.getCourse, grade.getCourseType))
      }
      val examGrade = Model.newInstance(classOf[ExamGrade])
      examGrade.setGradeType(gradeType)
      examGrade.setStatus(CONFIRMED)
      examGrade.setExamStatus(entityDao.get(classOf[ExamStatus], ExamStatus.NORMAL))
      if (null != grade.getLesson) {
        val examTakeQuery = OqlBuilder.from(classOf[ExamTake], "examTake")
        examTakeQuery.where("examTake.semester = :semeter", semester)
        examTakeQuery.where("examTake.std = :std", grade.getStd)
        examTakeQuery.where("examTake.examType = :examTypeId", gradeType.getExamType)
        examTakeQuery.where("examTake.lesson = :lesson", grade.getLesson)
        val examTakes = entityDao.search(examTakeQuery)
        if (CollectUtils.isNotEmpty(examTakes)) {
          examGrade.setExamStatus(examTakes.get(0).getExamStatus)
        }
      }
      grade.addExamGrade(examGrade)
    }
    grade
  }

  private def getCourseTakeInfo(std: Student, lessonId: java.lang.Long): List[CourseTake] = {
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    builder.where("courseTake.lesson.id = :lessonId", lessonId)
    builder.where("courseTake.std =:std", std)
    val takes = entityDao.search(builder)
    takes
  }

  def addGrade(): String = {
    putSemester(getProject)
    put("markStyles", gradeRateService.getMarkStyles(getProject))
    put("examStatuses", baseCodeService.getCodes(classOf[ExamStatus]))
    put("gradeTypes", entityDao.search(stdGradeService.buildGradeTypeQuery()))
    forward()
  }

  def addGradeByStd(): String = {
    setSemesterDataRealm(hasStdType)
    put("markStyles", gradeRateService.getMarkStyles(getProject))
    val stdCode = get("courseGrade.std.code")
    if (stdCode == "") {
      return forward(new Action("search"), "请再查询条件中用学号查询")
    }
    put("std", stdGradeService.getStdByCode(stdCode, getProject, getDeparts, entityDao))
    put("examStatuses", baseCodeService.getCodes(classOf[ExamStatus]))
    put("stdTypeList", getStdTypes)
    put("gradeTypes", entityDao.search(stdGradeService.buildGradeTypeQuery()))
    forward()
  }

  def batchEdit(): String = {
    var courseGradeIdSeq = get("courseGradeIds")
    if (Strings.isBlank(courseGradeIdSeq)) {
      courseGradeIdSeq = get("courseGrade.ids")
    }
    if (Strings.isEmpty(courseGradeIdSeq)) {
      return forwardError("error.parameters.needed")
    }
    put("grades", entityDao.get(classOf[CourseGrade], Strings.splitToLong(courseGradeIdSeq)))
    put("courseTypeList", baseCodeService.getCodes(classOf[CourseType]))
    put("courseGradeIds", courseGradeIdSeq)
    forward()
  }

  def saveBatchEdit(): String = {
    val courseGradeIdSeq = get("courseGradeIds")
    if (Strings.isEmpty(courseGradeIdSeq)) {
      return forwardError("error.parameters.needed")
    }
    val courseNo = get("course.code")
    val courses = entityDao.get(classOf[Course], "code", courseNo)
    var course: Course = null
    if (courses.size == 1) {
      course = courses.get(0)
    } else if (!Strings.isEmpty(courseNo) && courses.isEmpty) {
      return redirect("search", "找不到课程代码为" + courseNo + "的课程")
    }
    val courseTypeId = getInt("courseType.id")
    val grades = entityDao.get(classOf[CourseGrade], Strings.splitToLong(courseGradeIdSeq))
    for (grade <- grades) {
      if (null != course) {
        grade.setCourse(course)
      }
      if (null != courseTypeId) {
        grade.setCourseType(new CourseType(courseTypeId))
      }
    }
    try {
      entityDao.saveOrUpdate(grades)
      logger.info("batch update grade")
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
      }
    }
  }

  def edit(): String = {
    courseGradeHelper.editGrade()
    forward()
  }

  def save(): String = {
    courseGradeHelper.saveGrade(entityDao.get(classOf[User], getUserId))
    redirect("search", "info.save.success")
  }

  protected def getCourseTake(lesson: Lesson, std: Student): CourseTake = {
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.std = :std", std)
    query.where("take.lesson = :takeLesson", lesson)
    val takes = entityDao.search(query)
    if (CollectUtils.isEmpty(takes)) {
      return null
    }
    if (takes.size == 1) {
      takes.get(0)
    } else {
      throw new RuntimeException("数据异常")
    }
  }

  def saveAddGrade(): String = {
    val grade = populateEntity(classOf[CourseGrade], "courseGrade")
    val take = getCourseTake(grade.getLesson, grade.getStd)
    val project = getProject
    val lesson = entityDao.get(classOf[Lesson], grade.getLesson.id)
    if (stdGradeService.checkStdGradeExists(grade.getStd, grade.getSemester, lesson.getCourse, getProject)) {
      return redirect("search", "该课程成绩已存在!")
    }
    grade.setProject(project)
    grade.setUpdatedAt(new Date())
    grade.setCreatedAt(new Date())
    grade.setCourse(lesson.getCourse)
    grade.setLessonNo(lesson.getNo)
    val markStyle = grade.getMarkStyle
    if (markStyle == null) {
      grade.setMarkStyle(Model.newInstance(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT))
    }
    grade.setStatus(Grade.Status.PUBLISHED)
    grade.setCourseType(lesson.getCourseType)
    if (null != take) {
      grade.setCourseTakeType(take.getCourseTakeType)
    } else {
      grade.setCourseTakeType(Model.newInstance(classOf[CourseTakeType], CourseTakeType.NORMAL))
    }
    val gradeTypes = baseCodeService.getCodes(classOf[GradeType])
    var it = gradeTypes.iterator()
    while (it.hasNext) {
      val gradeType = it.next()
      val gradeTypeId = gradeType.id
      val score = get("examGrade" + gradeTypeId + ".score")
      val statusId = getInt("examGrade" + gradeTypeId + ".examStatus.id")
      if (Strings.isNotBlank(score) || null != statusId && statusId != ExamStatus.NORMAL) {
        val examGrade = buildGrade(grade, gradeType, markStyle)
        examGrade.setScore(calculator.gradeRateService.convert(score, markStyle, grade.getProject))
        examGrade.setExamStatus(Model.newInstance(classOf[ExamStatus], statusId))
        examGrade.setScoreText(calculator.gradeRateService.convert(examGrade.getScore, examGrade.getMarkStyle, 
          project))
        grade.addExamGrade(examGrade)
      }
    }
    val builder = OqlBuilder.from(classOf[CourseGradeState], "courseGradeStat")
    builder.where("courseGradeStat.lesson = :statLesson", lesson)
    val gradeStats = entityDao.search(builder)
    calculator.calc(grade, if ((CollectUtils.isNotEmpty(gradeStats))) gradeStats.get(0) else null)
    grade.setScoreText(calculator.gradeRateService.convert(grade.getScore, grade.getMarkStyle, project))
    try {
      entityDao.saveOrUpdate(grade)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => redirect("search", "error.occurred")
    }
  }

  private def buildGrade(grade: CourseGrade, gradeType: GradeType, markStyle: ScoreMarkStyle): ExamGrade = {
    var examGrade = grade.getExamGrade(gradeType)
    if (null != examGrade) return examGrade
    examGrade = Model.newInstance(classOf[ExamGrade])
    examGrade.setMarkStyle(markStyle)
    examGrade.setExamStatus(new ExamStatus(ExamStatus.NORMAL))
    examGrade.setCourseGrade(grade)
    examGrade.setGradeType(gradeType)
    examGrade.setCreatedAt(new Date())
    examGrade.setUpdatedAt(new Date())
    examGrade.setStatus(grade.getStatus)
    grade.addExamGrade(examGrade)
    examGrade
  }

  def saveAddGradeByStd(): String = {
    val grade = buildGrade()
    saveCourseGrade(grade)
    redirect("search", "info.save.success", "&stdId=" + grade.getStd.id)
  }

  def saveAndAddNext(): String = {
    val grade = buildGrade()
    saveCourseGrade(grade)
    redirect("addGradeByStd", "info.save.success")
  }

  private def buildGrade(): CourseGrade = {
    val grade = populateEntity(classOf[CourseGrade], "courseGrade")
    val take = getCourseTake(grade.getLesson, grade.getStd)
    val lesson = entityDao.get(classOf[Lesson], grade.getLesson.id)
    val std = entityDao.get(classOf[Student], grade.getStd.id)
    grade.setLesson(lesson)
    grade.setStd(std)
    grade.setCreatedAt(new Date())
    grade.setUpdatedAt(grade.getCreatedAt)
    grade.setSemester(lesson.getSemester)
    grade.setCourse(lesson.getCourse)
    val markStyle = grade.getMarkStyle
    if (null != grade.getMarkStyle) {
      grade.setMarkStyle(entityDao.get(classOf[ScoreMarkStyle], markStyle.id))
    }
    grade.setStatus(Grade.Status.PUBLISHED)
    grade.setCourseType(lesson.getCourseType)
    if (null != take) {
      grade.setCourseTakeType(take.getCourseTakeType)
    } else {
      grade.setCourseTakeType(Model.newInstance(classOf[CourseTakeType], CourseTakeType.NORMAL))
    }
    grade
  }

  private def saveCourseGrade(grade: CourseGrade) {
    markStyleHelper.init(ScoreMarkStyle.PERCENT)
    val gradeTypes = entityDao.search(stdGradeService.buildGradeTypeQuery())
    for (gradeType <- gradeTypes) {
      val gradeTypeId = gradeType.id
      val score = get("examGrade" + gradeTypeId + ".score")
      val statusId = getInt("examGrade" + gradeTypeId + ".examStatus.id")
      if (Strings.isNotEmpty(score) || null != statusId && statusId != ExamStatus.NORMAL) {
        val examMarkStyle = markStyleHelper.styleForScore(score)
        val examGrade = stdGradeService.buildGrade(grade, gradeType, examMarkStyle)
        examGrade.setScore(calculator.gradeRateService.convert(score, examMarkStyle, grade.getProject))
        examGrade.setUpdatedAt(grade.getUpdatedAt)
        examGrade.setExamStatus(new ExamStatus(statusId))
      }
    }
    calculator.calc(grade, (entityDao.get(classOf[CourseGradeState], "lesson", grade.getLesson))
      .get(0))
    entityDao.saveOrUpdate(grade)
  }

  def removeGrade(): String = {
    var courseGradeIdSeq = get("courseGradeIds")
    if (Strings.isBlank(courseGradeIdSeq)) courseGradeIdSeq = get("courseGrade.ids")
    if (Strings.isEmpty(courseGradeIdSeq)) return forwardError("error.parameters.needed")
    val courseGrades = entityDao.get(classOf[CourseGrade], Strings.splitToLong(courseGradeIdSeq))
    try {
      if (CollectUtils.isNotEmpty(courseGrades)) entityDao.remove(courseGrades)
    } catch {
      case e: Exception => return redirect("search", "info.delete.failure")
    }
    redirect("search", "info.delete.success")
  }

  def getCourseGradeInfo(stdCode: String, 
      projectId: java.lang.Integer, 
      semesterYear: String, 
      semesterTerm: String, 
      courseCode: String, 
      gradeTypeId: java.lang.Integer): Array[Any] = {
    val gradeInfo = Array.ofDim[Any](9)
    val semester = semesterService.getSemester(entityDao.get(classOf[Project], projectId).asInstanceOf[Project], 
      semesterYear, semesterTerm)
    if (null == semester) return gradeInfo
    val grade = getCourseGrade(stdCode.trim(), semester, courseCode, gradeTypeId)
    gradeInfo(0) = grade.id
    if (null != grade.getStd) {
      gradeInfo(1) = grade.getStd.getName
      val examGrade = grade.getExamGrade(new GradeType(gradeTypeId))
      if (null != examGrade) {
        gradeInfo(2) = examGrade.getScore
        gradeInfo(3) = examGrade.getExamStatus.getName
      }
      gradeInfo(4) = grade.getCourse.getCredits
      gradeInfo(5) = grade.getLessonNo
      val courseType = grade.getCourseType
      if (courseType != null) {
        gradeInfo(6) = courseType.id
      }
      val courseTakeType = grade.getCourseTakeType
      if (courseTakeType != null) {
        gradeInfo(7) = courseTakeType.id
      }
      if (null != grade.getCourseTakeType) {
        gradeInfo(8) = grade.getCourseTakeType.id
      }
    }
    gradeInfo
  }

  def batchSaveCourseGrade(): String = {
    val stdCount = getInt("stdCount")
    if (null == stdCount || stdCount.intValue() < 1) {
      return forwardError("error.parameters.illegal")
    }
    val project = getProject
    val semester = entityDao.get(classOf[Semester], getInt("semesterId"))
    val courseCode = get("courseCode")
    val gradeTypeId = getInt("gradeTypeId")
    val markStyleId = getInt("markStyleId")
    var courseGradeStatus = getInt("courseGradeStatus")
    if (null == courseGradeStatus) {
      courseGradeStatus = new java.lang.Integer(Grade.Status.CONFIRMED)
    }
    val markStyle = baseCodeService.getCode(classOf[ScoreMarkStyle], markStyleId).asInstanceOf[ScoreMarkStyle]
    val gradeType = baseCodeService.getCode(classOf[GradeType], gradeTypeId).asInstanceOf[GradeType]
    val grades = CollectUtils.newArrayList()
    for (i <- 0 until stdCount.intValue()) {
      val stdCode = get("stdCode" + i)
      val courseTypeId = getInt("courseType" + i)
      val courseTakeTypeId = getInt("courseTakeType" + i)
      if (Strings.isEmpty(stdCode)) {
        //continue
      }
      if (null == courseTypeId) {
        //continue
      }
      val courseType = baseCodeService.getCode(classOf[CourseType], courseTypeId).asInstanceOf[CourseType]
      var courseTakeType: CourseTakeType = null
      if (null != courseTakeTypeId) {
        courseTakeType = baseCodeService.getCode(classOf[CourseTakeType], courseTakeTypeId).asInstanceOf[CourseTakeType]
      }
      val grade = getCourseGrade(stdCode, semester, courseCode, gradeTypeId)
      if (null == grade.getStd) {
        //continue
      }
      grade.setProject(project)
      grade.setCourseType(courseType)
      if (null != courseTakeType) {
        grade.setCourseTakeType(courseTakeType)
      }
      val examGrade = grade.getExamGrade(gradeType)
      val score = getFloat("score" + i)
      if (null != score) {
        if (examGrade.isPersisted) {
          examGrade.setScore(score)
        } else {
          examGrade.setScore(score)
        }
      } else {
        val optionScore = getFloat("optionScore" + i)
        if (examGrade.isPersisted) {
          examGrade.setScore(optionScore)
        } else {
          examGrade.setScore(optionScore)
        }
      }
      if (!grade.isPersisted) {
        grade.setMarkStyle(markStyle)
      }
      grade.setCreatedAt(new Date())
      grade.setUpdatedAt(new Date())
      examGrade.setStatus(courseGradeStatus.intValue())
      examGrade.setMarkStyle(markStyle)
      if (null == grade.getLesson) {
        calculator.calc(grade, null)
      } else {
        val gradeState = courseGradeService.getState(grade.getLesson)
        calculator.calc(grade, gradeState)
      }
      entityDao.saveOrUpdate(grade)
      grades.add(grade)
    }
    val addAnother = getBoolean("addAnother")
    logger.info("Batch insert or update grade")
    if (true == addAnother) {
      redirect("batchAdd", "info.save.success")
    } else {
      put("grades", grades)
      put("gradeType", gradeType)
      put("semester", semester)
      forward("batchAddResult")
    }
  }

  def setCourseGradeHelper(courseGradeHelper: CourseGradeHelper) {
    this.courseGradeHelper = courseGradeHelper
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }

  def setMarkStyleHelper(markStyleHelper: MarkStyleHelper) {
    this.markStyleHelper = markStyleHelper
  }

  def setStringBuilderHelper(stringBuilderHelper: StringBuilderHelper) {
    this.stringBuilderHelper = stringBuilderHelper
  }

  def setStdGradeService(stdGradeService: StdGradeService) {
    this.stdGradeService = stdGradeService
  }

  def setGradeRateService(gradeRateService: GradeRateService) {
    this.gradeRateService = gradeRateService
  }

  def setGradeCourseTypeProvider(gradeCourseTypeProvider: GradeCourseTypeProvider) {
    this.gradeCourseTypeProvider = gradeCourseTypeProvider
  }
}
