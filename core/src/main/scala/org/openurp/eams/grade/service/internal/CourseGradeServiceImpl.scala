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
import org.openurp.teach.grade.model.CourseGradeBean
import org.openurp.teach.grade.model.ExamGradeBean
import org.openurp.eams.grade.model.CourseGradeStateBean
import scala.collection.mutable.Buffer
import org.openurp.teach.grade.ExamGrade
import org.openurp.teach.grade.GaGrade
import org.openurp.eams.grade.ExamGradeState
import org.openurp.eams.grade.GaGradeState

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
    val published = new collection.mutable.ListBuffer[GradeType]
    for (egs <- gradeState.gaStates if egs.status == Published) published += egs.gradeType
    val grades = getGrades(gradeState.lesson)
    for (grade <- grades) {
      updateGradeState(grade, gradeState)
      for (state <- gradeState.examStates) {
        val gradeType = state.gradeType
        val examGrade = grade.getGrade(gradeType).asInstanceOf[ExamGradeBean]
        val examState = gradeState.getState(gradeType).asInstanceOf[ExamGradeState]
        examGrade.percent = examState.percent
        updateGradeState(examGrade, examState)
      }
      for (state <- gradeState.gaStates) {
        val gradeType = state.gradeType
        val gaGrade = grade.getGrade(gradeType)
        val gaState = gradeState.getState(gradeType)
        updateGradeState(gaGrade, gaState)
      }
      calculator.calc(grade)
    }
    entityDao.saveOrUpdate(grades)
    if (!published.isEmpty) publish(Array(gradeState.lesson.id), published.toArray, true)
  }

  def remove(lesson: Lesson, gradeType: GradeType) {
    val state = getState(lesson).asInstanceOf[CourseGradeStateBean]
    val courseGrades = entityDao.findBy(classOf[CourseGrade], "lesson", List(lesson))
    val gradeSetting = settings.getSetting(lesson.project)
    val save = new collection.mutable.ListBuffer[Any]
    val remove = new collection.mutable.ListBuffer[Any]
    for (courseGrade <- courseGrades) {
      val examGrades = courseGrade.examGrades.asInstanceOf[Buffer[ExamGrade]]
      if (GradeType.Final == gradeType.id) {
        if (New == courseGrade.status) remove += courseGrade
      } else if (gradeType.isGa) {
        for (`type` <- gradeSetting.getRemovableElements(gradeType)) {
          val exam = courseGrade.getGrade(`type`).asInstanceOf[ExamGrade]
          if (null != exam && New == exam.status) examGrades -= exam
        }
        val ga = courseGrade.getGrade(gradeType).asInstanceOf[GaGrade]
        if (null != ga && New == ga.status) courseGrade.gaGrades.asInstanceOf[Buffer[GaGrade]] -= ga
        if (!examGrades.isEmpty || !courseGrade.gaGrades.isEmpty) {
          calculator.calc(courseGrade)
          save += courseGrade
        } else {
          remove += courseGrade
        }
      } else {
        val examGrade = courseGrade.getGrade(gradeType).asInstanceOf[ExamGrade]
        if (null != examGrade && New == examGrade.status) {
          examGrades -= examGrade
          if (!examGrades.isEmpty || !courseGrade.gaGrades.isEmpty) {
            calculator.calc(courseGrade)
            save += courseGrades
          } else {
            remove += courseGrade
          }
        }
      }
    }
    if (null != state) {
      if (GradeType.Final == gradeType.id) {
        state.status = New
        state.gaStates.clear()
        state.examStates.clear()
      } else {
        if (gradeType.isGa) {
          state.status = New
          for (`type` <- gradeSetting.getRemovableElements(gradeType)) {
            state.examStates.remove(state.getState(`type`).asInstanceOf[ExamGradeState])
          }
          state.gaStates.remove(state.getState(gradeType).asInstanceOf[GaGradeState])
        } else {
          state.examStates.remove(state.getState(gradeType).asInstanceOf[ExamGradeState])
        }
      }
      if (state.gaStates.isEmpty && state.examStates.isEmpty) {
        remove += state
      } else {
        save += state
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
      grade.markStyle = state.scoreMarkStyle
      grade.status = state.status
    }
  }

  private def updateState(lesson: Lesson, gradeTypes: Array[GradeType], status: Int) {
    val courseGradeStates = entityDao.findBy(classOf[CourseGradeStateBean], "lesson", List(lesson))
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
        val examGrade = grade.getGrade(gradeType)
        if (null != examGrade) {
          examGrade.status = status
          published.add(grade)
        }
      }
    }
    if (status == Grade.Status.Published) toBeSaved ++= publishStack.onPublish(published, gradeState, gradeTypes)
    toBeSaved ++= Operation.saveOrUpdate(lesson, gradeState).saveOrUpdate(published).build()
    entityDao.execute(toBeSaved.toArray: _*)
  }

}