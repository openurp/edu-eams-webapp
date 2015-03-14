package org.openurp.edu.eams.teach.grade.lesson.service.impl

import java.util.Collections
import java.util.Date
import java.util.List
import java.util.Set
import org.apache.commons.beanutils.BeanComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.ems.dictionary.service.BaseCodeService
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.course.model.GradeViewScope
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch
import org.openurp.edu.eams.teach.grade.lesson.service.LessonGradeService
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class LessonGradeServiceImpl extends BaseServiceImpl with LessonGradeService {

  protected var baseCodeService: BaseCodeService = _

  def getGradeTypes(state: CourseGradeState, userCategoryId: java.lang.Long): List[GradeType] = {
    val examGradeStates = state.getStates
    var canInputTypes: List[GradeType] = null
    if (!examGradeStates.isEmpty) {
      canInputTypes = CollectUtils.newArrayList()
      for (gradeTypeState <- examGradeStates) {
        canInputTypes.add(gradeTypeState.gradeType)
      }
      Collections.sort(canInputTypes, new BeanComparator("code"))
    } else {
      if (null != userCategoryId && userCategoryId.longValue() == 3L) {
        val query = OqlBuilder.from(classOf[GradeType], "gradeType")
        query.where("gradeType.enabled = true")
        query.where("gradeType.id not in (:gradeTypeIds)", Array(GradeTypeConstants.FINAL_ID, GradeTypeConstants.GA_ID))
        return entityDao.search(query)
      }
      val query = OqlBuilder.from(classOf[GradeInputSwitch], "inputSwitch")
      query.where("inputSwitch.isOpen = true")
      query.where("inputSwitch.semester = :semester", state.getLesson.getSemester)
      query.join("inputSwitch.gradeTypes", "gradeType")
      query.select("distinct gradeType")
      canInputTypes = entityDao.search(query).asInstanceOf[List[GradeType]]
    }
    canInputTypes
  }

  def getGradeTypes(lesson: Lesson): List[GradeType] = {
    val states = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    if (null == lesson || null == states) {
      return null
    }
    val gradeTypes = CollectUtils.newArrayList()
    if (CollectUtils.isNotEmpty(states)) {
      for (state <- states.get(0).getStates if state.getStatus > Grade.Status.NEW if state.gradeType.getId == GradeTypeConstants.GA_ID || 
        (state.getPercent != null && state.getPercent > 0)) {
        gradeTypes.add(entityDao.get(classOf[GradeType], state.gradeType.getId))
      }
    }
    gradeTypes
  }

  def getCanInputGradeTypes(isOnlyCanInput: Boolean): List[GradeType] = {
    val query = OqlBuilder.from(classOf[GradeType], "gradeType")
    if (isOnlyCanInput) {
      query.where("gradeType.id != :gradeTypeId", GradeTypeConstants.FINAL_ID)
    }
    query.where("gradeType.effectiveAt <= :now and (gradeType.invalidAt is null or gradeType.invalidAt >= :now)", 
      new java.util.Date())
    entityDao.search(query)
  }

  def isCheckEvaluation(std: Student): Boolean = {
    val query = OqlBuilder.from(classOf[GradeViewScope], "scope")
    query.where("scope.checkEvaluation=true")
    query.where("exists (from scope.stdTypes ss where ss = :stdType)", std.getType)
    query.where("exists (from scope.projects ss where ss = :project)", std.getProject)
    query.where("exists (from scope.educations ss where ss = :education)", std.education)
    query.join("left", "scope.stdTypes", "stdType")
    query.where("instr(',' || scope.enrollYears || ',', ',' || :enrollYear || ',') > 0", std.grade)
    CollectUtils.isNotEmpty(entityDao.search(query))
  }

  def getState(gradeType: GradeType, gradeState: CourseGradeState, precision: java.lang.Integer): ExamGradeState = {
    var gradeTypeState = gradeState.getState(gradeType)
    if (null == precision) {
      precision = new java.lang.Integer(0)
    }
    if (null == gradeTypeState) {
      gradeTypeState = Model.newInstance(classOf[ExamGradeState])
      gradeTypeState.setGradeType(gradeType)
      gradeTypeState.setStatus(Grade.Status.NEW)
      gradeTypeState.setGradeState(gradeState)
      gradeTypeState.setPrecision(precision)
      gradeTypeState.setInputedAt(new Date())
      gradeTypeState.setScoreMarkStyle(Model.newInstance(classOf[ScoreMarkStyle], gradeState.getScoreMarkStyle.getId))
      gradeState.getStates.add(gradeTypeState)
    }
    gradeTypeState
  }

  def setBaseCodeService(baseCodeService: BaseCodeService) {
    this.baseCodeService = baseCodeService
  }
}
