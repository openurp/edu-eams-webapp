package org.openurp.edu.eams.teach.grade.teacher.web.action

import java.util.Date


import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.security.blueprint.User
import org.openurp.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.grade.course.GradeModifyApply
import org.openurp.edu.eams.teach.grade.course.model.GradeModifyApplyBean
import org.openurp.edu.eams.teach.grade.course.model.GradeModifyApplyBean.GradeModifyStatus
import org.openurp.edu.eams.teach.grade.model.GradeRateConfig
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class GradeModifyApplyAction extends SemesterSupportAction {

  private var courseGradeService: CourseGradeService = _

  private var gradeRateService: GradeRateService = _

  override def getEntityName(): String = classOf[CourseGrade].getName

  def myApply(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    forward()
  }

  def applyList(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("field.evaluate.errorsOfSelect")
    }
    val semester = putSemester(null)
    val builder = OqlBuilder.from(classOf[GradeModifyApplyBean], "apply")
    populateConditions(builder)
    val applyStatus = get("applyStatus")
    if (Strings.isNotEmpty(applyStatus)) {
      builder.where("apply.status = :status", GradeModifyStatus.valueOf(applyStatus))
    }
    builder.where("apply.semester = :semester", semester)
    builder.where("apply.project = :project", getProject)
    builder.where("apply.applyer = :applyer", teacher.getName + "(" + teacher.getCode + ")")
    builder.limit(getPageLimit)
    builder.orderBy(get(Order.ORDER_STR))
    put("applys", entityDao.search(builder))
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType]))
    put("examStatuses", baseCodeService.getCodes(classOf[ExamStatus]))
    put("statuses", GradeModifyStatus.values)
    put("GA_ID", GradeTypeConstants.GA_ID)
    put("FINAL_ID", GradeTypeConstants.FINAL_ID)
    forward()
  }

  def search(): String = {
    val teacher = getLoginTeacher
    val user = entityDao.get(classOf[User], getUserId)
    if (null == teacher) {
      return forwardError("field.evaluate.errorsOfSelect")
    }
    val semester = putSemester(null)
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    populateConditions(builder)
    builder.where("courseGrade.semester = :semester", semester)
    builder.where("courseGrade.project = :project", getProject)
    builder.where("exists (from org.openurp.edu.teach.grade.model.CourseGradeState gradeState " + 
      "where gradeState.lesson=courseGrade.lesson and gradeState.extraInputer = :user) " + 
      "or exists (from courseGrade.lesson.teachers teacher where teacher = :teacher)", user, teacher)
    builder.where("courseGrade.status = :status", Grade.Status.PUBLISHED)
    builder.orderBy(get(Order.ORDER_STR))
    builder.limit(getPageLimit)
    val grades = entityDao.search(builder)
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("courseGrades", grades)
    forward()
  }

  protected override def editSetting(entity: Entity[_]) {
    val courseGrade = entity.asInstanceOf[CourseGrade]
    val gradeState = courseGradeService.getState(courseGrade.getLesson)
    val query = OqlBuilder.from(classOf[ExamStatus], "examStatus")
    query.where("examStatus.effectiveAt <= :now and (examStatus.invalidAt is null or examStatus.invalidAt >= :now)", 
      new Date())
    query.orderBy("examStatus.code")
    put("gradeState", gradeState)
    val configs = entityDao.get(classOf[GradeRateConfig], "scoreMarkStyle", gradeState.getScoreMarkStyle)
    if (CollectUtils.isNotEmpty(configs)) {
      put("gradeConverterConfig", configs.get(0))
    }
    var tempGa = true
    val examGradeStates = gradeState.getStates
    for (examGradeState <- examGradeStates if examGradeState.getPercent != null) {
      tempGa = false
      //break
    }
    val examGrades = courseGrade.getExamGrades
    val toUpdates = CollectUtils.newArrayList()
    for (examGrade <- examGrades) {
      if ((!tempGa && 
        GradeTypeConstants.GA_ID == examGrade.gradeType.id) || 
        GradeTypeConstants.FINAL_ID == examGrade.gradeType.id || 
        GradeTypeConstants.BONUS_ID == examGrade.gradeType.id) {
        //continue
      }
      toUpdates.add(examGrade)
    }
    put("tempGa", tempGa)
    put("GA_ID", GradeTypeConstants.GA_ID)
    put("FINAL_ID", GradeTypeConstants.FINAL_ID)
    put("examStatuses", entityDao.search(query))
    put("normalExamStatus", ExamStatus.NORMAL)
    put("examGrades", toUpdates)
  }

  override def save(): String = {
    val teacher = getLoginTeacher
    val courseGradeId = getLong("courseGrade.id")
    if (null == courseGradeId || null == teacher) {
      return forwardError("error.parameters.needed")
    }
    val courseGrade = entityDao.get(classOf[CourseGrade], courseGradeId)
    val examGrades = getModels(classOf[ExamGrade], getLongIds("examGrade"))
    val date = new Date()
    val applies = CollectUtils.newArrayList()
    val endStatusList = CollectUtils.newArrayList()
    endStatusList.add(GradeModifyStatus.FINAL_AUDIT_PASSED)
    endStatusList.add(GradeModifyStatus.FINAL_AUDIT_UNPASSED)
    endStatusList.add(GradeModifyStatus.DEPART_AUDIT_UNPASSED)
    endStatusList.add(GradeModifyStatus.ADMIN_AUDIT_UNPASSED)
    val user = entityDao.get(classOf[User], getUserId)
    for (examGrade <- examGrades) {
      val apply = Model.newInstance(classOf[GradeModifyApplyBean])
      val scoreInputName = examGrade.gradeType.getShortName + "_" + courseGrade.getStd.id
      val examScoreStr = get(scoreInputName)
      var examStatusId = getInt("examStatus_" + scoreInputName)
      if ((null == examScoreStr && null == examStatusId && 
        examGrade.gradeType.id != GradeTypeConstants.GA_ID)) {
        //continue
      }
      if (null == examStatusId) {
        examStatusId = ExamStatus.NORMAL
      }
      val applyReason = get("applyReason_" + examGrade.gradeType.getShortName + 
        "_" + 
        courseGrade.getStd.id)
      if (Strings.isBlank(applyReason)) {
        //continue
      }
      apply.setScore(getFloat(scoreInputName))
      apply.setScoreText(gradeRateService.convert(apply.getScore, examGrade.getMarkStyle, courseGrade.getProject))
      apply.setExamStatus(Model.newInstance(classOf[ExamStatus], examStatusId))
      apply.setOrigScore(examGrade.getScore)
      apply.setExamStatusBefore(examGrade.getExamStatus)
      apply.setApplyReason(applyReason)
      if (!apply.hasChange()) {
        //continue
      }
      val builder = OqlBuilder.from(classOf[GradeModifyApply], "apply")
      builder.where("apply.project = :project", courseGrade.getProject)
      builder.where("apply.semester = :semester", courseGrade.getSemester)
      builder.where("apply.gradeType = :gradeType", examGrade.gradeType)
      builder.where("apply.std = :std", courseGrade.getStd)
      builder.where("apply.course = :course", courseGrade.getCourse)
      builder.where("apply.status not in (:statuses)", endStatusList)
      val existsApplies = entityDao.search(builder)
      if (!existsApplies.isEmpty) {
        //continue
      }
      apply.setOrigScoreText(examGrade.getScoreText)
      apply.setCourse(courseGrade.getCourse)
      apply.setStd(courseGrade.getStd)
      apply.setProject(courseGrade.getProject)
      apply.setSemester(courseGrade.getSemester)
      apply.setGradeType(examGrade.gradeType)
      apply.setApplyer(user.getFullname + "(" + user.getName + ")")
      apply.setStatus(GradeModifyStatus.NOT_AUDIT)
      apply.setCreatedAt(date)
      apply.setUpdatedAt(date)
      applies.add(apply)
    }
    try {
      entityDao.saveOrUpdate(applies)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
      }
    }
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }

  def setGradeRateService(gradeRateService: GradeRateService) {
    this.gradeRateService = gradeRateService
  }
}
