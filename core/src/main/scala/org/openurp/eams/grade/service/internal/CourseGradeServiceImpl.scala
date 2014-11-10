package org.openurp.eams.grade.service.internal

import org.openurp.eams.grade.domain.CourseGradePublishStack
import org.openurp.eams.grade.service.CourseGradeSettings
import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.openurp.eams.grade.service.CourseGradeService
import org.springframework.ui.Model
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.eams.grade.CourseGradeState
import org.openurp.eams.grade.GradeState
import org.openurp.teach.lesson.Lesson
import org.openurp.teach.grade.domain.GradeCourseTypeProvider
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.grade.Grade
import org.openurp.teach.grade.Grade.Status._
import org.openurp.teach.code.GradeType
import org.beangle.commons.lang.Strings
import org.beangle.data.model.dao.Operation

class CourseGradeServiceImpl extends CourseGradeService {

  var entityDao: EntityDao = _

  var calculator: CourseGradeCalculator = _

  var gradeCourseTypeProvider: GradeCourseTypeProvider = _

  var publishStack: CourseGradePublishStack = _

  var settings: CourseGradeSettings = _

  private def getGrades(lesson: Lesson): Seq[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    query.where("courseGrade.lesson = :lesson", lesson)
    entityDao.search(query)
  }

  def getState(lesson: Lesson): CourseGradeState = {
    val list = entityDao.findBy(classOf[CourseGradeState], "lesson", List(lesson))
    if (list.isEmpty) null
    else list(0)
  }

  /**
   * 发布学生成绩
   */
  def publish(lessonIds: Array[Integer], gradeTypes: Array[GradeType], isPublished: Boolean) {
    val lessons = entityDao.find(classOf[Lesson], lessonIds)
    if (!lessons.isEmpty) {
      val setting = settings.getSetting(lessons(0).project)
      if (setting.submitIsPublish) {
        for (lesson <- lessons) {
          updateState(lesson, gradeTypes, if (isPublished) Grade.Status.Published else Grade.Status.New)
        }
      } else {
        for (lesson <- lessons) {
          updateState(lesson, gradeTypes, if (isPublished) Grade.Status.Published else Grade.Status.Confirmed)
        }
      }
    }
  }

  /**
   * 依据状态调整成绩
   */
  def recalculate(gradeState: CourseGradeState) {
    if (null == gradeState) {
      return
    }
    val published = CollectUtils.newArrayList()
    for (egs <- gradeState.states if egs.status == Published) published.add(egs.gradeType)
    val grades = getGrades(gradeState.lesson)
    for (grade <- grades) {
      updateGradeState(grade, gradeState)
      for (state <- gradeState.states) {
        val gradeType = state.gradeType
        val examGrade = grade.getExamGrade(gradeType)
        val examState = gradeState.getState(gradeType)
        updateGradeState(examGrade, examState)
      }
      calculator.calc(grade, gradeState)
    }
    entityDao.saveOrUpdate(grades)
    if (!published.isEmpty) publish(gradeState.lesson.id.toString, published.toArray(Array.ofDim[GradeType](published.size)),
      true)
  }

  def remove(lesson: Lesson, gradeType: GradeType) {
    val state = getState(lesson)
    val courseGrades = entityDao.findBy(classOf[CourseGrade], "lesson", List(lesson))
    val gradeSetting = settings.getSetting(lesson.project)
    val save = CollectUtils.newArrayList()
    val remove = CollectUtils.newArrayList()
    for (courseGrade <- courseGrades) {
      if (GradeType.Final == gradeType.id) {
        if (New == courseGrade.status) remove.add(courseGrade)
      } else if (GradeType.GA_ID == gradeType.id) {
        for (`type` <- gradeSetting.getGaElementTypes) {
          val exam = courseGrade.getExamGrade(`type`)
          if (null != exam && New == exam.status) courseGrade.getExamGrades.remove(exam)
        }
        val ga = courseGrade.getExamGrade(gradeType)
        if (null != ga && New == ga.status) courseGrade.getExamGrades.remove(ga)
        if (CollectUtils.isNotEmpty(courseGrade.getExamGrades)) {
          calculator.calc(courseGrade, state)
          save.add(courseGrade)
        } else {
          remove.add(courseGrade)
        }
      } else {
        val examGrade = courseGrade.getExamGrade(gradeType)
        if (null == examGrade || New != examGrade.status) //continue
          courseGrade.examGrades -= examGrade
        if (CollectUtils.isNotEmpty(courseGrade.getExamGrades)) {
          calculator.calc(courseGrade, state)
          save.add(courseGrade)
        } else {
          remove.add(courseGrade)
        }
      }
    }
    if (null != state) {
      if (GradeType.Final == gradeType.id) {
        state.status = New
        state.states.clear()
      } else {
        if (GradeType.GA_ID == gradeType.id) {
          state.setStatus(New)
          for (`type` <- gradeSetting.getGaElementTypes) {
            state.states.remove(state.getState(`type`))
          }
        }
        state.states.remove(state.getState(gradeType))
      }
      if (state.states.isEmpty) {
        remove.add(state)
      } else {
        save.add(state)
      }
    }
    entityDao.execute(Operation.saveOrUpdate(save).remove(remove))
  }

  /**
   * 依据状态信息更新成绩的状态和记录方式
   *
   * @param grade
   * @param state
   */
  private def updateGradeState(grade: Grade, state: GradeState) {
    if (null != grade && null != state) {
      grade.asInstanceOf[CourseGradeBean].markStyle = state.scoreMarkStyle
      grade.asInstanceOf[CourseGradeBean].status = state.status
    }
  }

  private def updateState(lesson: Lesson, gradeTypes: Array[GradeType], status: Int) {
    val courseGradeStates = entityDao.findBy(classOf[CourseGradeState], "lesson", List(lesson))
    var gradeState: CourseGradeStateBean = null
    for (gradeType <- gradeTypes) {
      gradeState = if (courseGradeStates.isEmpty) new CourseGradeStateBean else courseGradeStates(0)
      if (gradeType.id == GradeType.Final) {
        gradeState.status = status
      } else {
        gradeState.updateStatus(gradeType, status)
      }
    }
    val grades = entityDao.findBy(classOf[CourseGrade], "lesson", List(lesson))
    val toBeSaved = new collection.mutable.HashSet[Operation]
    val published = new collection.mutable.HashSet[CourseGrade]
    for (grade <- grades; gradeType <- gradeTypes) {
      if (gradeType.id == GradeType.Final) {
        grade.asInstanceOf[CourseGradeBean].status = status
      } else {
        val examGrade = grade.getExamGrade(gradeType).asInstanceOf[ExamGradeBean]
        if (null != examGrade) {
          examGrade.status = status
          published.add(grade)
        }
      }
    }
    if (status == Grade.Status.Published) toBeSaved ++= publishStack.onPublish(published, gradeState, gradeTypes)
    toBeSaved ++= Operation.saveOrUpdate(lesson, gradeState).saveOrUpdate(published).build()
    entityDao.execute(toBeSaved.toArray(Array.ofDim[Operation](toBeSaved.size)))
  }

}