package org.openurp.edu.eams.teach.grade.course.service.impl




import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.service.CourseGradePublishListener
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.ExamTakeBean
import ExamTakeGeneratePublishListener._



object ExamTakeGeneratePublishListener {

  private val Makeup = new ExamType(ExamType.MAKEUP)

  private val Delay = new ExamType(ExamType.DELAY)
}

class ExamTakeGeneratePublishListener extends BaseServiceImpl with CourseGradePublishListener {

  private var settings: CourseGradeSettings = _

  private var forbiddenCourseNames: Array[String] = new Array[String](0)

  private var forbiddenCourseTypeNames: Array[String] = new Array[String](0)

  private var forbiddenCourseTakeTypeNames: Array[String] = new Array[String](0)

  def onPublish(grades: Iterable[CourseGrade], gradeState: CourseGradeState, gradeTypes: Array[GradeType]): List[Operation] = {
    val operations = Collections.newBuffer[Any]
    var hasGa = false
    for (gradeType <- gradeTypes if gradeType.id == GradeTypeConstants.GA_ID) {
      hasGa = true
      //break
    }
    if (!hasGa) return operations
    if (isLessonForbidden(gradeState.getLesson)) return operations
    val setting = settings.getSetting(gradeState.getLesson.getProject)
    val examTakes = getExamTakes(gradeState.getLesson)
    for (grade <- grades) operations.addAll(publishOneGrade(grade, setting, gradeTypes, examTakes))
    operations
  }

  def onPublish(grade: CourseGrade, gradeTypes: Array[GradeType]): List[Operation] = {
    val operations = Collections.newBuffer[Any]
    var hasGa = false
    for (gradeType <- gradeTypes if gradeType.id == GradeTypeConstants.GA_ID) {
      hasGa = true
      //break
    }
    if (!hasGa) return operations
    val lesson = grade.getLesson
    if (isLessonForbidden(lesson)) return operations
    val setting = settings.getSetting(lesson.getProject)
    val examTakes = getExamTakes(lesson, grade.getStd)
    operations.addAll(publishOneGrade(grade, setting, gradeTypes, examTakes))
    operations
  }

  protected def isLessonForbidden(lesson: Lesson): Boolean = {
    if (null != lesson) {
      for (courseName <- forbiddenCourseNames if lesson.getCourse.getName.contains(courseName)) return true
      for (courseTypeName <- forbiddenCourseTypeNames if lesson.getCourseType.getName.contains(courseTypeName)) return true
    }
    false
  }

  protected def isCourseTakeTypeForbidden(grade: CourseGrade): Boolean = {
    forbiddenCourseTakeTypeNames.find(grade.getCourseTakeType.getName.contains(_))
      .map(_ => true)
      .getOrElse(false)
  }

  protected def getMakeupOrDelayExamTypeId(setting: CourseGradeSetting, examGrade: ExamGrade): java.lang.Integer = {
    if (isCourseTakeTypeForbidden(examGrade.getCourseGrade)) return null
    val examStatus = examGrade.getExamStatus
    if (examStatus.id == ExamStatus.DELAY) {
      ExamType.DELAY
    } else {
      if (setting.getAllowExamStatuses.contains(examStatus)) ExamType.MAKEUP else {
        null
      }
    }
  }

  private def getExamTakes(lesson: Lesson): Map[Student, ExamTake] = {
    val builder = OqlBuilder.from(classOf[ExamTake], "examTake")
    builder.where("examTake.lesson=:lesson and examTake.examType in (:examTypes) and examTake.activity is null", 
      lesson, Array(Makeup, Delay))
    val examTakes = entityDao.search(builder)
    val takes = Collections.newMap[Any]
    for (examTake <- examTakes) {
      takes.put(examTake.getStd, examTake)
    }
    takes
  }

  private def getExamTakes(lesson: Lesson, std: Student): Map[Student, ExamTake] = {
    val builder = OqlBuilder.from(classOf[ExamTake], "examTake")
    builder.where("examTake.std=:std and examTake.lesson=:lesson and examTake.examType in (:examTypes) and examTake.activity is null", 
      std, lesson, Array(Makeup, Delay))
    val examTakes = entityDao.search(builder)
    val takes = Collections.newMap[Any]
    for (examTake <- examTakes) {
      takes.put(examTake.getStd, examTake)
    }
    takes
  }

  def publishOneGrade(grade: CourseGrade, 
      setting: CourseGradeSetting, 
      gradeTypes: Array[GradeType], 
      examTakes: Map[Student, ExamTake]): List[Operation] = {
    val operations = Collections.newBuffer[Any]
    val examGrade = grade.getExamGrade(new GradeType(GradeTypeConstants.END_ID))
    if (null == examGrade) return operations
    val lesson = grade.getLesson
    val std = grade.getStd
    var take: ExamTake = null
    if (!grade.isPassed && !examGrade.isPassed) {
      val examTypeId = getMakeupOrDelayExamTypeId(setting, examGrade)
      if (null != examTypeId) take = getOrCreateExamTake(std, lesson, new ExamType(examTypeId), examTakes)
      if (null == take) {
        addRemoveExamTakes(operations, std, examTakes, Makeup, Delay)
      } else {
        operations.addAll(Operation.saveOrUpdate(take).build())
        if (take.getExamType == Makeup) addRemoveExamTakes(operations, std, examTakes, Delay)
        if (take.getExamType == Delay) addRemoveExamTakes(operations, std, examTakes, Makeup)
      }
    } else {
      if (null != 
        grade.getExamGrade(new GradeType(GradeTypeConstants.DELAY_ID))) addRemoveExamTakes(operations, 
        std, examTakes, Makeup)
      if (null != 
        grade.getExamGrade(new GradeType(GradeTypeConstants.MAKEUP_ID))) addRemoveExamTakes(operations, 
        std, examTakes, Delay)
    }
    operations
  }

  private def addRemoveExamTakes(operations: List[Operation], 
      std: Student, 
      examTakes: Map[Student, ExamTake], 
      examTypes: ExamType*) {
    val take = examTakes.get(std)
    if (null != take) {
      for (examType <- examTypes if take.getExamType == examType) operations.addAll(Operation.remove(take).build())
    }
  }

  private def getOrCreateExamTake(std: Student, 
      lesson: Lesson, 
      examType: ExamType, 
      examTakes: Map[Student, ExamTake]): ExamTake = {
    var take = examTakes.get(std)
    if (null == take) {
      take = new ExamTakeBean()
      take.setStd(std)
      take.setLesson(lesson)
      take.setSemester(lesson.getSemester)
      take.setExamType(examType)
      take.setExamStatus(new ExamStatus(ExamStatus.NORMAL))
    }
    take
  }

  def setForbiddenCourseNames(names: String) {
    forbiddenCourseNames = Strings.split(names, ",")
    if (null == forbiddenCourseNames) forbiddenCourseNames = Array.ofDim[String](0)
  }

  def setForbiddenCourseTypeNames(names: String) {
    forbiddenCourseTypeNames = Strings.split(names, ",")
    if (null == forbiddenCourseTypeNames) forbiddenCourseTypeNames = Array.ofDim[String](0)
  }

  def setForbiddenCourseTakeTypeNames(names: String) {
    forbiddenCourseTakeTypeNames = Strings.split(names, ",")
    if (null == forbiddenCourseTakeTypeNames) forbiddenCourseTakeTypeNames = Array.ofDim[String](0)
  }

  def setSettings(settings: CourseGradeSettings) {
    this.settings = settings
  }
}
