package org.openurp.eams.grade.teacher.action

class EndGaAction extends TeacherAction {
  
//  protected override def getGradeTypes(gradeState: CourseGradeState): List[GradeType] = {
//    var gradeTypes = getAttribute("gradeTypes").asInstanceOf[List[GradeType]]
//    if (null == gradeTypes) {
//      gradeTypes = CollectUtils.newArrayList()
//      val gis = getAttribute("gradeInputSwitch").asInstanceOf[GradeInputSwitch]
//      val eles = settings.getSetting(getProject).getGaElementTypes
//      for (`type` <- eles) {
//        val gradeType = baseCodeService.getCode(classOf[GradeType], `type`.getId).asInstanceOf[GradeType]
//        val egs = gradeState.getState(gradeType)
//        if (null != egs && (null == egs.getPercent || egs.getPercent <= 0)) //continue
//        if (null != gis && gis.getTypes.contains(gradeType)) gradeTypes.add(gradeType)
//      }
//      gradeTypes.add(entityDao.get(classOf[GradeType], GradeTypeConstants.GA_ID))
//      put("gradeTypes", gradeTypes)
//    }
//    gradeTypes
//  }

//  /**
//   * 总评成绩录入
//   *
//   * @return
//   */
//  def input(): String = {
//    val result = checkState()
//    if (null != result) {
//      return result
//    }
//    val gradeState = getGradeState
//    val gradeTypes = settings.getSetting(getProject).getGaElementTypes
//    var updatePercent = false
//    for (gradeType <- gradeTypes) {
//      val prefix = "examGradeState" + gradeType.getId
//      val percent = getFloat(prefix + ".percent")
//      val egs = getState(gradeType)
//      if (null != percent && 
//        (null == egs.getPercent || 0 != Float.compare(percent / 100F, egs.getPercent))) {
//        egs.setPercent(percent / 100F)
//        updatePercent = true
//      }
//      val examMarkStyleId = getInt(prefix + ".scoreMarkStyle.id")
//      if (null != examMarkStyleId) egs.setScoreMarkStyle(entityDao.get(classOf[ScoreMarkStyle], examMarkStyleId))
//    }
//    val msg = checkLessonPermission(gradeState.getLesson)
//    if (null != msg) {
//      return forwardError(msg)
//    }
//    entityDao.saveOrUpdate(gradeState)
//    if (updatePercent) courseGradeService.recalculate(getGradeState)
//    val lesson = gradeState.getLesson
//    putGradeMap(lesson, getCourseTakes(lesson))
//    buildGradeConfig(lesson, getGradeTypes(gradeState))
//    val putSomeParams = CollectUtils.newHashSet()
//    putSomeParams.add("isTeacher")
//    putSomeParams.add("GA")
//    putSomeParams.add("NEW")
//    putSomeParams.add("CONFIRMED")
//    putSomeParams.add("gradeConverterConfig")
//    putSomeParams.add("examStatuses")
//    putSomeParams.add("USUAL")
//    putSomeParams.add("VIOLATION")
//    putSomeParams.add("CHEAT")
//    putSomeParams.add("ABSENT")
//    putSomeParams.add("DELAY")
//    putSomeParams.add("gradeRateConfigs")
//    put("setting", settings.getSetting(getProject))
//    buildSomeParams(lesson, putSomeParams)
//    put("NormalTakeType", baseCodeService.getCode(classOf[CourseTakeType], CourseTakeType.NORMAL))
//    put("NormalExamStatus", baseCodeService.getCode(classOf[ExamStatus], ExamStatus.NORMAL))
//    put("lesson", lesson)
//    forward()
//  }
}
