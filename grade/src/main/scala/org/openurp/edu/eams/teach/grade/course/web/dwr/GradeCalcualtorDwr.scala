package org.openurp.edu.eams.teach.grade.course.web.dwr


import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.entity.metadata.Model
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants



class GradeCalcualtorDwr {

  var entityDao: EntityDao = _

  var gradeRateService: GradeRateService = _

  var courseGradeCalculator: CourseGradeCalculator = _

  private def getParams(contents: Array[String]): Map[String, Map[String, Any]] = {
    val paramMaps = CollectUtils.newHashMap()
    for (content <- contents) {
      val prefix = Strings.substringBefore(content, ".")
      var params = paramMaps.get(prefix)
      if (null == params) {
        params = CollectUtils.newHashMap()
        paramMaps.put(prefix, params)
      }
      params.put(Strings.substringBetween(content, prefix + ".", "="), Strings.substringAfter(content, 
        "="))
    }
    paramMaps
  }

  def calcGa(gradeStateId: java.lang.Long, gradeContent: String): String = {
    if (Strings.isBlank(gradeContent)) {
      return null
    }
    val grade = Model.newInstance(classOf[CourseGrade])
    val paramMaps = getParams(Strings.split(gradeContent, "&"))
    Model.populate(grade, paramMaps.get("grade"))
    paramMaps.remove("grade")
    paramMaps.remove("state")
    for ((key, value) <- paramMaps) {
      val key = key
      val examGrade = Model.newInstance(classOf[ExamGrade])
      Model.populate(examGrade, paramMaps.get(key))
      grade.addExamGrade(examGrade)
    }
    val state = entityDao.get(classOf[CourseGradeState], gradeStateId)
    val ga = courseGradeCalculator.calcGa(grade, state)
    val gaState = state.getState(new GradeType(GradeTypeConstants.GA_ID))
    var gaStyle: ScoreMarkStyle = null
    gaStyle = if (null == gaState) state.getScoreMarkStyle else gaState.getScoreMarkStyle
    val passed = gradeRateService.isPassed(ga, gaStyle, grade.getProject)
    gradeRateService.convert(ga, gaStyle, grade.getProject) + 
      "," + 
      (if (passed) 1 else 0)
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setGradeRateService(gradeRateService: GradeRateService) {
    this.gradeRateService = gradeRateService
  }

  def setCourseGradeCalculator(courseGradeCalculator: CourseGradeCalculator) {
    this.courseGradeCalculator = courseGradeCalculator
  }
}
