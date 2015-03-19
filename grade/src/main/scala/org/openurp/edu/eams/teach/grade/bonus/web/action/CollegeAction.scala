package org.openurp.edu.eams.teach.grade.bonus.web.action

import java.util.Date


import org.beangle.commons.lang.Strings
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.security.blueprint.User
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.event.CourseGradeModifyEvent
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseGradeBean
import org.openurp.edu.eams.teach.lesson.model.CourseGradeStateBean
import org.openurp.edu.eams.teach.lesson.model.ExamGradeBean
import org.openurp.edu.eams.teach.lesson.model.ExamGradeStateBean
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class CollegeAction extends SemesterSupportAction {

  private var courseGradeService: CourseGradeService = _

  private var calculator: CourseGradeCalculator = _

  override def getEntityName(): String = classOf[ExamGrade].getName

  protected override def editSetting(entity: Entity[_]) {
    put("courseTakes", CollectUtils.newArrayList())
  }

  def search(): String = {
    put(getShortName + "s", search(getQueryBuilder))
    val gradeInputSwitch = getGradeInputSwitch(getProject, putSemester(null))
    if (gradeInputSwitch.checkOpen() && 
      gradeInputSwitch.getTypes.contains(Model.newInstance(classOf[GradeType], GradeTypeConstants.BONUS_ID))) {
      put("inputOpen", true)
    }
    forward()
  }

  def report(): String = {
    val builder = getQueryBuilder.asInstanceOf[OqlBuilder[ExamGrade]]
    builder.limit(null)
    val examGradeIds = getLongIds(getShortName)
    if (ArrayUtils.isNotEmpty(examGradeIds)) {
      builder.where(getShortName + ".id in (:examGradeIds)", examGradeIds)
    }
    builder.orderBy(getShortName + ".courseGrade.lesson.no," + getShortName + 
      ".courseGrade.std.code")
    val examGrades = entityDao.search(builder)
    val examGradeMap = CollectUtils.newHashMap()
    for (examGrade <- examGrades) {
      val department = examGrade.getCourseGrade.getLesson.getTeachDepart
      if (examGradeMap.containsKey(department)) {
        if (!examGradeMap.get(department).contains(examGrade)) {
          examGradeMap.get(department).add(examGrade)
        }
      } else {
        examGradeMap.put(department, CollectUtils.newArrayList(examGrade))
      }
    }
    put("examGradeMap", examGradeMap)
    put("semester", putSemester(null))
    put("sysDate", new Date())
    put("emptyExamGrade", new ExamGradeBean())
    forward()
  }

  private def getGradeInputSwitch(project: Project, semester: Semester): GradeInputSwitch = {
    val query = OqlBuilder.from(classOf[GradeInputSwitch], "switch")
    query.where("switch.project=:project", project)
    query.where("switch.semester=:semester", semester)
    query.where("switch.opened = true")
    val rs = entityDao.search(query)
    var gradeInputSwitch: GradeInputSwitch = null
    if (CollectUtils.isNotEmpty(rs)) {
      gradeInputSwitch = rs.get(0)
    } else {
      gradeInputSwitch = Model.newInstance(classOf[GradeInputSwitch])
      gradeInputSwitch.setProject(project)
      gradeInputSwitch.setSemester(semester)
      gradeInputSwitch.setTypes(CollectUtils.newHashSet(baseCodeService.getCodes(classOf[GradeType])))
    }
    gradeInputSwitch
  }

  protected override def indexSetting() {
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("stdTypes", getStdTypes)
    put("departments", getDeparts)
  }

  def searchCourseTakes(): String = {
    val stdCode = get("stdCode")
    if (Strings.isBlank(stdCode)) {
      put("courseTakes", CollectUtils.newArrayList())
      return forward()
    }
    val students = entityDao.get(classOf[Student], "code", stdCode)
    if (students.isEmpty) {
      put("courseTakes", CollectUtils.newArrayList())
      return forward()
    }
    val student = students.get(0)
    val courseTakes = entityDao.get(classOf[CourseTake], Array("std", "lesson.semester", "lesson.project"), 
      student, putSemester(null), getProject)
    put("student", student)
    put("courseTakes", courseTakes)
    put("bonus", Model.newInstance(classOf[GradeType], GradeTypeConstants.BONUS_ID))
    val lessonGrades = CollectUtils.newHashMap()
    for (courseTake <- courseTakes) {
      val grades = entityDao.get(classOf[CourseGrade], Array("std", "lesson"), courseTake.getStd, courseTake.getLesson)
      if (grades.isEmpty) {
        //continue
      }
      lessonGrades.put(courseTake.getLesson, grades.get(0))
    }
    put("lessonGrades", lessonGrades)
    forward()
  }

  override def save(): String = {
    val studentId = getLong("student.id")
    val params = get("params")
    if (studentId == null) {
      return redirect("search", params, "error.model.id.needed")
    }
    val bonusGradeType = Model.newInstance(classOf[GradeType], GradeTypeConstants.BONUS_ID)
    val student = entityDao.get(classOf[Student], studentId)
    val lessons = getModels(classOf[Lesson], getLongIds("lesson"))
    val percentStyle = Model.newInstance(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT)
    val normalStatus = Model.newInstance(classOf[ExamStatus], ExamStatus.NORMAL)
    val toSaves = CollectUtils.newArrayList()
    val modifyGrades = CollectUtils.newArrayList()
    val date = new Date()
    val user = entityDao.get(classOf[User], getUserId)
    for (lesson <- lessons) {
      val courseTakes = entityDao.get(classOf[CourseTake], Array("std", "lesson"), student, lesson)
      if (courseTakes.isEmpty) {
        //continue
      }
      val courseTake = courseTakes.get(0)
      var gradeState = courseGradeService.getState(lesson)
      val bonusScore = getFloat("bonus_score_" + lesson.id)
      if (null == gradeState) {
        gradeState = new CourseGradeStateBean(lesson)
      }
      val courseGrades = entityDao.get(classOf[CourseGrade], Array("lesson", "std"), lesson, student)
      var courseGrade: CourseGrade = null
      if (courseGrades.isEmpty) {
        if (bonusScore != null) {
          courseGrade = new CourseGradeBean(courseTakes.get(0))
          courseGrade.setProject(courseTake.getLesson.getProject)
          courseGrade.setExamMode(courseTake.getLesson.getExamMode)
          courseGrade.setMarkStyle(percentStyle)
          courseGrade.setCreatedAt(new Date())
          courseGrade.setUpdatedAt(courseGrade.getCreatedAt)
        } else {
          //continue
        }
      } else {
        courseGrade = courseGrades.get(0)
      }
      var bonusGradeState = gradeState.getState(bonusGradeType)
      var bonusGrade = courseGrade.getExamGrade(bonusGradeType)
      if (null != bonusScore) {
        if (null == bonusGradeState) {
          bonusGradeState = Model.newInstance(classOf[ExamGradeStateBean])
          bonusGradeState.setGradeState(gradeState)
          bonusGradeState.setGradeType(bonusGradeType)
          bonusGradeState.setScoreMarkStyle(percentStyle)
          bonusGradeState.setStatus(Grade.Status.PUBLISHED)
          bonusGradeState.setOperator(user.getFullname + "(" + user.getName + ")")
          gradeState.getStates.add(bonusGradeState)
        }
        if (null == bonusGrade) {
          bonusGrade = new ExamGradeBean(bonusGradeType, bonusScore)
          bonusGrade.setScoreText(bonusScore + "")
          bonusGrade.setPassed(true)
          bonusGrade.setMarkStyle(percentStyle)
          bonusGrade.setCreatedAt(new Date())
          bonusGrade.setUpdatedAt(bonusGrade.getCreatedAt)
          bonusGrade.setCourseGrade(courseGrade)
          bonusGrade.setExamStatus(normalStatus)
          bonusGrade.setStatus(Grade.Status.PUBLISHED)
          courseGrade.getExamGrades.add(bonusGrade)
          courseGrade.setUpdatedAt(date)
        } else {
          bonusGrade.setScore(bonusScore)
          bonusGrade.setScoreText(bonusScore + "")
          bonusGrade.setUpdatedAt(new Date())
          courseGrade.setUpdatedAt(date)
        }
        bonusGrade.setOperator(user.getFullname + "(" + user.getName + ")")
      } else {
        if (null != bonusGradeState) {
          gradeState.getStates.remove(bonusGradeState)
        }
        if (null != bonusGrade) {
          courseGrade.getExamGrades.remove(bonusGrade)
          courseGrade.setUpdatedAt(date)
        }
      }
      calculator.calc(courseGrade, gradeState)
      if (null != bonusGrade) {
        bonusGrade.setPassed(true)
      }
      toSaves.add(courseGrade)
      toSaves.add(gradeState)
      if (courseGrade.getStatus == Grade.Status.PUBLISHED) {
        modifyGrades.add(courseGrade)
      }
    }
    try {
      entityDao.saveOrUpdate(toSaves)
    } catch {
      case e: Exception => return redirect("search", params, "info.save.failure")
    }
    publish(new CourseGradeModifyEvent(modifyGrades))
    redirect("search", params, "info.save.success")
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(classOf[ExamGrade], "examGrade")
    populateConditions(builder)
    builder.where("examGrade.gradeType.id =:bonusId", GradeTypeConstants.BONUS_ID)
    builder.where("examGrade.courseGrade.semester = :semester", putSemester(null))
    builder.where("examGrade.courseGrade.project = :project", getProject)
    builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    builder
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }

  def setCourseGradeCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }
}
