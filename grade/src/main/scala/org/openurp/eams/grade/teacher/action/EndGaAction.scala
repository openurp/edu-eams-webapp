package org.openurp.eams.grade.teacher.action

import org.openurp.teach.core.model.ProjectBean
import org.openurp.teach.core.Project
import org.openurp.teach.code.ExamStatus
import org.openurp.teach.code.CourseTakeType
import org.openurp.eams.grade.model.ExamGradeStateBean
import java.lang.{ Short => JShort }
import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.eams.grade.ExamGradeState
import org.openurp.teach.code.model.ScoreMarkStyleBean
import java.util.HashSet
import scala.collection.mutable.ListBuffer
import collection.mutable
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.model.GradeTypeBean
import org.openurp.eams.grade.CourseGradeState
import org.openurp.eams.grade.GradeInputSwitch
import org.beangle.webmvc.api.view.View

/**
 * 期末总评录入
 */
class EndGaAction extends AbstractTeacherAction {

  protected override def getGradeTypes(gradeState: CourseGradeState): List[GradeType] = {
    val gradeTypes = Option(getAttribute("gradeTypes").asInstanceOf[List[GradeType]]).getOrElse({
      val gradeTypes = new ListBuffer[GradeType]
      val gis = getAttribute("gradeInputSwitch").asInstanceOf[GradeInputSwitch]
      val eles = settings.getSetting(getProject).endGaElements
      for (`type` <- eles) {
        val gradeType = baseCodeService.getCode(classOf[GradeType], `type`.id).asInstanceOf[GradeType]
        val egs = gradeState.getState(gradeType)
        //        if (null != egs && (null == egs.getPercent || egs.getPercent <= 0)) //continue
        //          if (null != gis && gis.getTypes.contains(gradeType)) gradeTypes.add(gradeType)
        gradeTypes.append(`type`)
      }
//      gradeTypes.append(entityDao.get(classOf[GradeType], GradeType.EndGa))
      gradeTypes.toList
    })
    put("gradeTypes", gradeTypes)
    gradeTypes
  }

  /**
   * 总评成绩录入
   *
   * @return
   */
  def input(): String = {
    val result = checkState()
    if (null != result) {
//      return result
    }
    val gradeState = getGradeState
    val project = getProject
    val gradeTypes = settings.getSetting(project).endGaElements
    var updatePercent = false
    for (gradeType <- gradeTypes) {
      val prefix = "examGradeState" + gradeType.id
      val percent = getInt(prefix + ".percent")
      val egs = entityDao.get(classOf[ExamGradeStateBean], new java.lang.Long(1))
      if (percent.isDefined &&
        (null == egs.percent || percent.get * 1 == egs.percent)) {
        egs.percent = percent.get.shortValue
        updatePercent = true
      }
      val examMarkStyleId = getInt(prefix + ".scoreMarkStyle.id")
      if (examMarkStyleId.isDefined)
        egs.scoreMarkStyle = entityDao.get(classOf[ScoreMarkStyleBean], new Integer(examMarkStyleId.get))
    }
    val msg = checkLessonPermission(gradeState.lesson)
    if (null != msg) {
      return forward("", msg)
    }
    entityDao.saveOrUpdate(gradeState)
    if (updatePercent) courseGradeService.recalculate(getGradeState)
    val lesson = gradeState.lesson
    val o = lesson.teachClass.courseTakes
    putGradeMap(lesson, getCourseTakes(lesson).toList)
    buildGradeConfig(lesson, getGradeTypes(gradeState))
    val putSomeParams = new ListBuffer[String]
    putSomeParams += "isTeacher"
    putSomeParams += "GA"
    putSomeParams += "NEW"
    putSomeParams += "CONFIRMED"
    putSomeParams += "gradeConverterConfig"
    putSomeParams += "examStatuses"
    putSomeParams += "USUAL"
    putSomeParams += "VIOLATION"
    putSomeParams += "CHEAT"
    putSomeParams += "ABSENT"
    putSomeParams += "DELAY"
    putSomeParams += "gradeRateConfigs"
    put("setting", settings.getSetting(project))
    buildSomeParams(lesson, putSomeParams.toSet)
    put("NormalTakeType", baseCodeService.getCode(classOf[CourseTakeType], CourseTakeType.NORMAL))
    put("NormalExamStatus", baseCodeService.getCode(classOf[ExamStatus], ExamStatus.Normal))
    put("lesson", lesson)
    put("gradeTypePolicy", gradeTypePolicy)
    forward()
  }

  override def save(): View = {
    null
  }

  def personPercent(): String = null

  def inputTask(): String = null
}
