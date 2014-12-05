package org.openurp.eams.grade.teacher.action

import scala.collection.mutable.ListBuffer
import org.beangle.data.model.annotation.code
import org.beangle.webmvc.api.annotation.ignore
import org.openurp.eams.grade.CourseGradeState
import org.openurp.eams.grade.GradeInputSwitch
import org.openurp.eams.grade.model.ExamGradeStateBean
import org.openurp.teach.code.CourseTakeType
import org.openurp.teach.code.ExamStatus
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.model.ScoreMarkStyleBean
import org.beangle.commons.lang.Strings
import org.openurp.teach.grade.model.CourseGradeBean
import org.beangle.webmvc.entity.helper.PopulateHelper
import org.openurp.teach.grade.model.ExamGradeBean
import org.openurp.teach.code.model.GradeTypeBean

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
      gradeTypes.append(entityDao.get(classOf[GradeType], GradeType.EndGa))
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
    checkState()
    val gradeState = getGradeState
    checkLessonPermission(gradeState.lesson)
    val project = getProject
    val gradeTypes = settings.getSetting(project).endGaElements
    var updatePercent = false
    for (gradeType <- gradeTypes) {
      val egs = getState(gradeType).asInstanceOf[ExamGradeStateBean]
      val prefix = "examGradeState" + gradeType.id
      val percent = getInt(prefix + ".percent")
      if (percent.isDefined &&
        (null == egs.percent || percent.get * 1 != egs.percent)) {
        egs.percent = percent.get.shortValue
        updatePercent = true
      }
      val examMarkStyleId = getInt(prefix + ".scoreMarkStyle.id")
      if (examMarkStyleId.isDefined)
        egs.scoreMarkStyle = entityDao.get(classOf[ScoreMarkStyleBean], new Integer(examMarkStyleId.get))
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

  def personPercent(): String = null

  def inputTask(): String = null

  def calcGa(): String = {
    val gradeStateId = getLong("gradeStateId")
    val gradeContent = get("gradeContent")
    if (gradeContent.isEmpty || Strings.isBlank(gradeContent.get)) {
      return null
    }
    val grade = new CourseGradeBean
    val paramMaps = getParams(Strings.split(gradeContent.get, "&"))
    PopulateHelper.populate(grade, paramMaps.get("grade").get)
    paramMaps.remove("grade")
    paramMaps.remove("state")
    for (key <- paramMaps.keys) {
      val examGrade = new ExamGradeBean
      PopulateHelper.populate(examGrade, paramMaps.get(key).get)
      grade.examGrades += examGrade
    }
    val state = entityDao.get(classOf[CourseGradeState], new java.lang.Long(gradeStateId.get))
    val ga = calculator.calcEndGa(grade)
    val gaState = state.getState(new GradeTypeBean(GradeType.EndGa))
    val gaStyle = Option(gaState).getOrElse(state).scoreMarkStyle
    val passed = gradeRateService.isPassed(ga, gaStyle, grade.project)
    val result = gradeRateService.convert(ga, gaStyle, grade.project) + "," + (if (passed) 1 else 0)
    put("result", result)
    forward()
  }

  private def getParams(contents: Array[String]): collection.mutable.HashMap[String, collection.mutable.HashMap[String, Object]] = {
    val paramMaps = new collection.mutable.HashMap[String, collection.mutable.HashMap[String, Object]]
    for (content <- contents) {
      val prefix = Strings.substringBefore(content, ".")
      val params = paramMaps.get(prefix).getOrElse({
        val params = new collection.mutable.HashMap[String, Object]
        paramMaps.put(prefix, params)
        params
      }).asInstanceOf[collection.mutable.HashMap[String, Object]]
      params.put(Strings.substringBetween(content, prefix + ".", "="),
        Strings.substringAfter(content, "="))
    }
    paramMaps
  }

}
