package org.openurp.eams.grade.service.internal

class CourseGradeServiceImpl extends BaseServiceImpl with CourseGradeService {

  protected var calculator: CourseGradeCalculator = _

  protected var gradeCourseTypeProvider: GradeCourseTypeProvider = _

  protected var publishStack: CourseGradePublishStack = _

  protected var settings: CourseGradeSettings = _

  private def getGrades(lesson: Lesson): List[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    query.where("courseGrade.lesson = :lesson", lesson)
    entityDao.search(query)
  }

  def getState(lesson: Lesson): CourseGradeState = {
    val list = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    if (list.isEmpty) {
      return null
    }
    list.get(0)
  }

  /**
   * 发布学生成绩
   */
  def publish(lessonIdSeq: String, gradeTypes: Array[GradeType], isPublished: Boolean) {
    val lessons = entityDao.get(classOf[Lesson], Strings.transformToLong(lessonIdSeq.split(",")))
    if (CollectUtils.isNotEmpty(lessons)) {
      val setting = settings.getSetting(lessons.get(0).getProject)
      if (setting.isSubmitIsPublish) {
        for (lesson <- lessons) {
          updateState(lesson, gradeTypes, if (isPublished) PUBLISHED else NEW)
        }
      } else {
        for (lesson <- lessons) {
          updateState(lesson, gradeTypes, if (isPublished) PUBLISHED else CONFIRMED)
        }
      }
    }
  }

  /**
   依据状态调整成绩
   */
  def recalculate(gradeState: CourseGradeState) {
    if (null == gradeState) {
      return
    }
    val published = CollectUtils.newArrayList()
    for (egs <- gradeState.getStates if egs.getStatus == PUBLISHED) published.add(egs.getGradeType)
    val grades = getGrades(gradeState.getLesson)
    for (grade <- grades) {
      updateGradeState(grade, gradeState)
      for (state <- gradeState.getStates) {
        val gradeType = state.getGradeType
        val examGrade = grade.getExamGrade(gradeType)
        val examState = gradeState.getState(gradeType)
        updateGradeState(examGrade, examState)
      }
      calculator.calc(grade, gradeState)
    }
    entityDao.saveOrUpdate(grades)
    if (!published.isEmpty) publish(gradeState.getLesson.getId.toString, published.toArray(Array.ofDim[GradeType](published.size)), 
      true)
  }

  def remove(lesson: Lesson, gradeType: GradeType) {
    val state = getState(lesson)
    val courseGrades = entityDao.get(classOf[CourseGrade], "lesson", lesson)
    val gradeSetting = settings.getSetting(lesson.getProject)
    val save = CollectUtils.newArrayList()
    val remove = CollectUtils.newArrayList()
    for (courseGrade <- courseGrades) {
      if (GradeTypeConstants.FINAL_ID == gradeType.getId) {
        if (NEW == courseGrade.getStatus) remove.add(courseGrade)
      } else if (GradeTypeConstants.GA_ID == gradeType.getId) {
        for (`type` <- gradeSetting.getGaElementTypes) {
          val exam = courseGrade.getExamGrade(`type`)
          if (null != exam && NEW == exam.getStatus) courseGrade.getExamGrades.remove(exam)
        }
        val ga = courseGrade.getExamGrade(gradeType)
        if (null != ga && NEW == ga.getStatus) courseGrade.getExamGrades.remove(ga)
        if (CollectUtils.isNotEmpty(courseGrade.getExamGrades)) {
          calculator.calc(courseGrade, state)
          save.add(courseGrade)
        } else {
          remove.add(courseGrade)
        }
      } else {
        val examGrade = courseGrade.getExamGrade(gradeType)
        if (null == examGrade || NEW != examGrade.getStatus) //continue
        courseGrade.getExamGrades.remove(examGrade)
        if (CollectUtils.isNotEmpty(courseGrade.getExamGrades)) {
          calculator.calc(courseGrade, state)
          save.add(courseGrade)
        } else {
          remove.add(courseGrade)
        }
      }
    }
    if (null != state) {
      state.setAuditReason(null)
      state.setAuditStatus(null)
      if (GradeTypeConstants.FINAL_ID == gradeType.getId) {
        state.setStatus(NEW)
        state.getStates.clear()
      } else {
        if (GradeTypeConstants.GA_ID == gradeType.getId) {
          state.setStatus(NEW)
          for (`type` <- gradeSetting.getGaElementTypes) {
            state.getStates.remove(state.getState(`type`))
          }
        }
        state.getStates.remove(state.getState(gradeType))
      }
      if (state.getStates.isEmpty) {
        remove.add(state)
      } else {
        save.add(state)
      }
    }
    entityDao.execute(Operation.saveOrUpdate(save).remove(remove))
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }

  def setCourseGradePublishStack(courseGradePublishStack: CourseGradePublishStack) {
    this.publishStack = courseGradePublishStack
  }

  def setGradeCourseTypeProvider(gradeCourseTypeProvider: GradeCourseTypeProvider) {
    this.gradeCourseTypeProvider = gradeCourseTypeProvider
  }

  /**
   * 依据状态信息更新成绩的状态和记录方式
   *
   * @param grade
   * @param state
   */
  private def updateGradeState(grade: Grade, state: GradeState) {
    if (null != grade && null != state) {
      grade.setMarkStyle(state.scoreMarkStyle)
      grade.setStatus(state.getStatus)
    }
  }

  private def updateState(lesson: Lesson, gradeTypes: Array[GradeType], status: Int) {
    val courseGradeStates = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    var gradeState: CourseGradeState = null
    for (gradeType <- gradeTypes) {
      gradeState = if (courseGradeStates.isEmpty) Model.newInstance(classOf[CourseGradeState]) else courseGradeStates.get(0)
      if (gradeType.getId == GradeTypeConstants.FINAL_ID) {
        gradeState.setStatus(status)
      } else {
        gradeState.updateStatus(gradeType, status)
      }
    }
    val grades = entityDao.get(classOf[CourseGrade], "lesson", lesson)
    val toBeSaved = CollectUtils.newArrayList()
    val published = CollectUtils.newHashSet()
    for (grade <- grades; gradeType <- gradeTypes) {
      if (gradeType.getId == GradeTypeConstants.FINAL_ID) {
        grade.setStatus(status)
      } else {
        val examGrade = grade.getExamGrade(gradeType)
        if (null != examGrade) {
          examGrade.setStatus(status)
          published.add(grade)
        }
      }
    }
    if (status == PUBLISHED) toBeSaved.addAll(publishStack.onPublish(published, gradeState, gradeTypes))
    toBeSaved.addAll(Operation.saveOrUpdate(lesson, gradeState).saveOrUpdate(published)
      .build())
    entityDao.execute(toBeSaved.toArray(Array.ofDim[Operation](toBeSaved.size)))
  }

  def setSettings(settings: CourseGradeSettings) {
    this.settings = settings
  }
}
