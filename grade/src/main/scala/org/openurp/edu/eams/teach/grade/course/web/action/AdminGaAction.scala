package org.openurp.edu.eams.teach.grade.course.web.action



import org.beangle.commons.collection.Collections
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson



class AdminGaAction extends AdminAction {

  protected override def getGradeTypes(gradeState: CourseGradeState): List[GradeType] = {
    var gradeTypes = getAttribute("gradeTypes").asInstanceOf[List[GradeType]]
    if (null == gradeTypes) {
      gradeTypes = Collections.newBuffer[Any]
      val gis = getAttribute("gradeInputSwitch").asInstanceOf[GradeInputSwitch]
      val eles = settings.getSetting(getProject).getGaElementTypes
      for (`type` <- eles) {
        val gradeType = baseCodeService.getCode(classOf[GradeType], `type`.id).asInstanceOf[GradeType]
        val egs = gradeState.getState(gradeType)
        if (null != egs && (null == egs.getPercent || egs.getPercent <= 0)) //continue
        if (null != gis && gis.getTypes.contains(gradeType)) gradeTypes.add(gradeType)
      }
      gradeTypes.add(entityDao.get(classOf[GradeType], GradeTypeConstants.GA_ID))
      put("gradeTypes", gradeTypes)
    }
    gradeTypes
  }

  def input(): String = {
    val result = checkState()
    if (null != result) {
      return result
    }
    val gradeState = getGradeState
    val gradeTypes = settings.getSetting(getProject).getGaElementTypes
    var updatePercent = false
    for (gradeType <- gradeTypes) {
      val prefix = "examGradeState" + gradeType.id
      val percent = getFloat(prefix + ".percent")
      val egs = getState(gradeType)
      if (null != percent && 
        (null == egs.getPercent || 
        0 != 
        java.lang.Float.compare(percent / 100F, egs.getPercent))) {
        egs.setPercent(percent / 100F)
        updatePercent = true
      }
      val examMarkStyleId = getInt(prefix + ".scoreMarkStyle.id")
      if (null != examMarkStyleId) egs.setScoreMarkStyle(entityDao.get(classOf[ScoreMarkStyle], examMarkStyleId))
    }
    entityDao.saveOrUpdate(gradeState)
    if (updatePercent) courseGradeService.recalculate(getGradeState)
    val lesson = gradeState.getLesson
    putGradeMap(lesson, getCourseTakes(lesson))
    buildGradeConfig(lesson, getGradeTypes(gradeState))
    val putSomeParams = Collections.newSet[Any]
    putSomeParams.add("isTeacher")
    putSomeParams.add("GA")
    putSomeParams.add("NEW")
    putSomeParams.add("CONFIRMED")
    putSomeParams.add("gradeConverterConfig")
    putSomeParams.add("examStatuses")
    putSomeParams.add("USUAL")
    putSomeParams.add("VIOLATION")
    putSomeParams.add("CHEAT")
    putSomeParams.add("ABSENT")
    putSomeParams.add("DELAY")
    putSomeParams.add("gradeRateConfigs")
    put("setting", settings.getSetting(getProject))
    buildSomeParams(lesson, putSomeParams)
    put("NormalTakeType", baseCodeService.getCode(classOf[CourseTakeType], CourseTakeType.NORMAL))
    put("NormalExamStatus", baseCodeService.getCode(classOf[ExamStatus], ExamStatus.NORMAL))
    put("lesson", lesson)
    forward()
  }
}
