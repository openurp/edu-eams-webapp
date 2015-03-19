package org.openurp.edu.eams.teach.grade.lesson.web.action



import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Department
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.ExamGradeBean
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import MakeupAction._



object MakeupAction {

  private val examTypeIds = Array(ExamType.MAKEUP, ExamType.DELAY)
}

class MakeupAction extends SemesterSupportAction {

  protected var courseGradeService: CourseGradeService = _

  protected var lessonService: LessonService = _

  protected var calcualtor: CourseGradeCalculator = _

  protected override def getEntityName(): String = classOf[ExamTake].getName

  protected override def indexSetting() {
    put("teachDepartList", getDeparts)
  }

  def search(): String = {
    val query = OqlBuilder.from(classOf[ExamTake], "examTake")
    populateConditions(query)
    query.select("new org.openurp.edu.eams.teach.grade.course.web.helper.MakeupCourse(examTake.lesson.teachDepart,examTake.lesson.course,count(*))")
    query.groupBy("examTake.lesson.teachDepart,examTake.lesson.course")
    query.where("examTake.examType.id in (:examTypeId)", examTypeIds)
    query.limit(getPageLimit)
    val departId = getLong("examTake.lesson.teachDepart.id")
    if (null == departId) {
      query.where("examTake.lesson.teachDepart in(:departs)", getDeparts)
    }
    put("makeupCourses", entityDao.search(query))
    forward()
  }

  def getExamTakeState(): String = {
    val semesterId = getInt("examTake.semester.id")
    val query = OqlBuilder.from(classOf[ExamActivity], "activity")
    populateConditions(query)
    query.where("activity.examType.id = :examTypeId", examTypeIds)
    query.where("activity.semester.id = :semesterId", semesterId)
    query.limit(getPageLimit)
    query.orderBy(Order.parse(get("orderBy")))
    query.select("select distinct activity.lesson.course,activity.lesson.teachDepart,activity.time")
    put("courseList", entityDao.search(query))
    forward()
  }

  def gradeTable(): String = {
    val makeupCourseIds = get("makeupCourse.ids")
    val semesterId = getInt("semester.id")
    val examTakeId = Strings.split(makeupCourseIds, ",")
    val examTasks = CollectUtils.newArrayList()
    for (i <- 0 until examTakeId.length) {
      val params = Strings.split(examTakeId(i), "@")
      examTasks.add(getMakeupTakes(semesterId, java.lang.Long.valueOf(params(0)), java.lang.Long.valueOf(params(1))))
    }
    put("examTasks", examTasks)
    put("semester", semesterService.getSemester(semesterId))
    forward()
  }

  private def getMakeupTakes(semesterId: java.lang.Integer, courseId: java.lang.Long, departId: java.lang.Long): List[ExamTake] = {
    val query = OqlBuilder.from(classOf[ExamTake], "examTake")
    query.where("examTake.examType.id in (:examTypeId)", examTypeIds)
    query.where("examTake.semester.id=:semesterId", semesterId)
    query.where("examTake.lesson.course.id = :courseId", courseId)
    query.where("examTake.lesson.teachDepart.id=:teachDepartId", departId)
    entityDao.search(query)
  }

  def batchAddGrade(): String = {
    getExamTakeAndMakeupExam
    put("MAKEUP", baseCodeService.getCode(classOf[ExamType], ExamType.MAKEUP))
    forward()
  }

  def gradeInfo(): String = {
    getExamTakeAndMakeupExam
    forward()
  }

  private def getExamTakeAndMakeupExam() {
    val makeupCourseId = get("makeupCourse.id")
    val semesterId = getInt("semester.id")
    val params = Strings.split(makeupCourseId, "@")
    val examTakes = getMakeupTakes(semesterId, java.lang.Long.valueOf(params(0)), java.lang.Long.valueOf(params(1)))
    val examGradeMap = CollectUtils.newHashMap()
    for (examTake <- examTakes) {
      val grade = getCourseGrade(examTake.getLesson, examTake.getStd)
      var gradeType: GradeType = null
      gradeType = if (examTake.getExamType.id == ExamType.DELAY) new GradeType(GradeTypeConstants.DELAY_ID) else new GradeType(GradeTypeConstants.MAKEUP_ID)
      if (null != grade) {
        val examGrade = grade.getExamGrade(gradeType)
        examGradeMap.put(examTake.id.toString, examGrade)
      }
    }
    put("examTakeList", examTakes)
    put("examGradeMap", examGradeMap)
    put("semester", semesterService.getSemester(semesterId))
    put("course", entityDao.get(classOf[Course], java.lang.Long.valueOf(params(0).toString)))
    put("teachDepart", entityDao.get(classOf[Department], java.lang.Integer.valueOf(params(1).toString)))
  }

  private def getCourseGrade(lesson: Lesson, std: Student): CourseGrade = {
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.std = :std and grade.lesson=:lesson", std, lesson)
    val grades = entityDao.search(query)
    if (grades.isEmpty) null else grades.get(0)
  }

  def batchSaveCourseGrade(): String = {
    val courseId = get("makeupCourse.id")
    val gradeState = get("grade.state")
    val semesterId = getInt("semester.id")
    val params = Strings.split(courseId, "@")
    val examTakes = getMakeupTakes(semesterId, java.lang.Long.valueOf(params(0)), java.lang.Long.valueOf(params(1)))
    val grades = CollectUtils.newArrayList()
    for (examTake <- examTakes) {
      val score = getFloat(examTake.id.toString)
      if (null != score) {
        val grade = getCourseGrade(examTake.getLesson, examTake.getStd)
        if (null != grade) {
          val gradeType = new GradeType(if (examTake.getExamType.id == ExamType.DELAY) GradeTypeConstants.DELAY_ID else GradeTypeConstants.MAKEUP_ID)
          var examGrade = grade.getExamGrade(gradeType)
          if (null == examGrade) {
            examGrade = new ExamGradeBean()
            examGrade.setGradeType(gradeType)
            examGrade.setExamStatus(new ExamStatus(ExamStatus.NORMAL))
            var style = grade.getMarkStyle
            if (gradeType.id == GradeTypeConstants.DELAY_ID) {
              val end = grade.getExamGrade(new GradeType(GradeTypeConstants.END_ID))
              if (null != end) style = end.getMarkStyle
            }
            examGrade.setMarkStyle(style)
            examGrade.setScore(score)
            grade.addExamGrade(examGrade)
          } else {
            examGrade.setScore(score)
          }
          examGrade.setStatus(java.lang.Integer.parseInt(gradeState))
        }
        calcualtor.calc(grade, courseGradeService.getState(examTake.getLesson))
        grades.add(grade)
      }
    }
    entityDao.saveOrUpdate(grades)
    redirect("search", "info.save.success")
  }

  def setCourseGradeCalculator(calcualtor: CourseGradeCalculator) {
    this.calcualtor = calcualtor
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }

  def setCalcualtor(calcualtor: CourseGradeCalculator) {
    this.calcualtor = calcualtor
  }
}
