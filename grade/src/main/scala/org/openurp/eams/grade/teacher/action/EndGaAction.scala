package org.openurp.eams.grade.teacher.action

import org.openurp.teach.core.model.ProjectBean
import org.openurp.teach.core.Project
import org.openurp.teach.code.ExamStatus
import org.openurp.teach.code.CourseTakeType
import org.openurp.eams.grade.model.ExamGradeStateBean
import java.lang.{Short => JShort}
import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.eams.grade.ExamGradeState

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

  /**
   * 总评成绩录入
   *
   * @return
   */
  def input(): String = {
//    val result = checkState()
//    if (null != result) {
//      return result
//    }
    val gradeState = getGradeState
    val project = entityDao.get(classOf[Project], new Integer(1))
    val gradeTypes = settings.getSetting(project).endGaElements 
    var updatePercent = false
    for (gradeType <- gradeTypes) {
      val prefix = "examGradeState" + gradeType.id
      val percent = getInt(prefix + ".percent").get
      val egs = entityDao .get(classOf[ExamGradeState], new java.lang.Long(1))
      if (null != percent && 
        (null == egs.percent  || percent * 1 == egs.percent)) {
        egs.percent = percent.shortValue
        updatePercent = true
      }
      val examMarkStyleId = getInt(prefix + ".scoreMarkStyle.id")
      if (null != examMarkStyleId) egs.scoreMarkStyle=entityDao .get(classOf[ScoreMarkStyle], examMarkStyleId)
    }
    val msg = checkLessonPermission(gradeState.lesson)
    if (null != msg) {
      return forwardError(msg)
    }
    entityDao.saveOrUpdate(gradeState)
    if (updatePercent) courseGradeService.recalculate(getGradeState)
    val lesson = gradeState.lesson
    //putGradeMap(lesson, getCourseTakes(lesson))
    //buildGradeConfig(lesson, getGradeTypes(gradeState))
    val putSomeParams = new HashSet()
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
    put("setting", settings.getSetting(project))
    buildSomeParams(lesson, putSomeParams)
    put("NormalTakeType", baseCodeService.getCode(classOf[CourseTakeType], CourseTakeType.NORMAL))
    put("NormalExamStatus", baseCodeService.getCode(classOf[ExamStatus], ExamStatus.Normal))
    put("lesson", lesson)
    forward()
  }
}
