package org.openurp.edu.eams.teach.grade.course.service

import java.util.Date



import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.transfer.TransferMessage
import org.beangle.commons.transfer.TransferResult
import org.beangle.commons.transfer.importer.listener.ItemImporterListener
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.lesson.Lesson



class CourseGradeImportListener(protected var entityDao: EntityDao, protected var project: Project, protected var calculator: CourseGradeCalculator)
    extends ItemImporterListener {

  override def onFinish(tr: TransferResult) {
  }

  override def onItemStart(tr: TransferResult) {
  }

  override def onItemFinish(tr: TransferResult) {
    val errors = tr.getErrs
    tr.getErrs.clear()
    val courseGrade = populateCourseGrade(tr)
    if (!tr.hasErrors()) {
      try {
        if (!courseGrade.isPersisted) {
          courseGrade.setCreatedAt(new Date(System.currentTimeMillis()))
        }
        courseGrade.setUpdatedAt(new Date(System.currentTimeMillis()))
        if (null == courseGrade.getCreatedAt) {
          courseGrade.setCreatedAt(new Date(System.currentTimeMillis()))
        }
        entityDao.saveOrUpdate(courseGrade)
      } catch {
        case e: ConstraintViolationException => {
          tr.getErrs.addAll(errors)
          for (constraintViolation <- e.getConstraintViolations) {
            tr.addFailure(constraintViolation.getPropertyPath + constraintViolation.getMessage, constraintViolation.getInvalidValue)
          }
        }
      }
    }
  }

  private def getPropEntity[T <: Entity[_]](clazz: Class[T], 
      tr: TransferResult, 
      key: String, 
      notNull: Boolean): T = {
    val description = importer.getDescriptions.get(key)
    val value = importer.getCurData.get(key).asInstanceOf[String]
    if (Strings.isBlank(value)) {
      if (notNull) {
        tr.addFailure(description + "不能为空", value)
      } else {
        return null
      }
    }
    if (classOf[Semester].isAssignableFrom(clazz)) {
      val query = OqlBuilder.from(clazz, "semester")
      query.where("(semester.schoolYear||semester.name)=:semesterTitle", value)
      val titleList = entityDao.search(query)
      if (titleList.size == 1) {
        return titleList.get(0)
      }
    }
    val nameList = entityDao.get(clazz, "name", value)
    if (nameList.size != 1) {
      val codeList = entityDao.get(clazz, "code", value)
      if (codeList.size == 1) {
        return codeList.get(0)
      } else if ((nameList.size + codeList.size) == 0) {
        tr.addFailure(importer.getDescriptions.get(key) + "不存在", value)
      } else {
        tr.addFailure(importer.getDescriptions.get(key) + "存在多条记录", value)
      }
      return null
    }
    nameList.get(0)
  }

  private def getLesson(tr: TransferResult, 
      key: String, 
      course: Course, 
      semester: Semester): Lesson = {
    val value = importer.getCurData.get(key).asInstanceOf[String]
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.semester =:semester", semester)
    builder.where("lesson.course = :course", course)
    if (value != null) {
      builder.where("lesson.no =:lessonNo", value)
    }
    val noList = entityDao.search(builder)
    if (noList.size == 1) {
      return noList.get(0)
    }
    null
  }

  private def populateCourseGrade(tr: TransferResult): CourseGrade = {
    val std = getPropEntity(classOf[Student], tr, "std", true)
    val course = getPropEntity(classOf[Course], tr, "course", true)
    val semester = getPropEntity(classOf[Semester], tr, "semester", true)
    val courseGrade = checkCourseGradeExists(project, std, course, semester, tr)
    val lesson = getLesson(tr, "lesson", course, semester)
    var cgs: CourseGradeState = null
    if (null != lesson) {
      courseGrade.setLesson(lesson)
      cgs = getCourseGradeState(lesson)
    }
    setExamGrades(courseGrade, tr, cgs)
    if (null != std) {
      calculator.calc(courseGrade, cgs)
    }
    val courseType = getPropEntity(classOf[CourseType], tr, "courseType", true)
    if (null != courseType) {
      courseGrade.setCourseType(courseType)
    }
    courseGrade
  }

  private def getCourseGradeState(lesson: Lesson): CourseGradeState = {
    val cgses = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    if (cgses.size == 1) {
      cgses.get(0)
    } else {
      null
    }
  }

  private def checkCourseGradeExists(project: Project, 
      std: Student, 
      course: Course, 
      semester: Semester, 
      tr: TransferResult): CourseGrade = {
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :student", std)
    builder.where("courseGrade.course = :course", course)
    builder.where("courseGrade.semester = :semester", semester)
    builder.where("courseGrade.project = :project", project)
    var courseGrades: List[CourseGrade] = null
    val courseGrade = Model.newInstance(classOf[CourseGrade])
    try {
      courseGrades = entityDao.search(builder)
      if (courseGrades.size == 1) {
        return courseGrades.get(0)
      } else if (courseGrades.size > 1) {
        tr.addFailure("存在多条记录(", std.getName + "," + project.getName + "," + course.getName + 
          "," + 
          semester.getCode + 
          ")")
        return null
      }
      courseGrade.setStd(std)
      courseGrade.setProject(project)
      courseGrade.setSemester(semester)
      courseGrade.setCourse(course)
    } catch {
      case e: Exception => 
    }
    courseGrade
  }

  private def setExamGrades(courseGrade: CourseGrade, tr: TransferResult, cgs: CourseGradeState) {
    var examGrades = courseGrade.getExamGrades
    val gradeTypes = entityDao.getAll(classOf[GradeType])
    if (CollectUtils.isEmpty(examGrades)) {
      examGrades = CollectUtils.newHashSet()
    }
    var examStatus = getPropEntity(classOf[ExamStatus], tr, "examStatus", false)
    if (examStatus == null) {
      examStatus = entityDao.get(classOf[ExamStatus], ExamStatus.NORMAL)
    }
    var markStyle = getPropEntity(classOf[ScoreMarkStyle], tr, "markStyle", false)
    if (null == markStyle) {
      markStyle = entityDao.get(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT)
    }
    courseGrade.setMarkStyle(markStyle)
    for (gradeType <- gradeTypes) {
      val value = importer.getCurData.get(gradeType.getShortName).asInstanceOf[String]
      val examGrade = checkExamGradeExists(examGrades, gradeType)
      examGrade.setGradeType(gradeType)
      examGrade.setExamStatus(examStatus)
      examGrade.setCourseGrade(courseGrade)
      examGrade.setMarkStyle(markStyle)
      checkGrade(value, examGrade, tr, markStyle)
      courseGrade.addExamGrade(examGrade)
    }
    calculator.calc(courseGrade, null)
  }

  private def checkGrade(value: String, 
      examGrade: ExamGrade, 
      tr: TransferResult, 
      markStyle: ScoreMarkStyle) {
    if (Strings.isNotEmpty(value)) {
      if (value.matches("^\\d*\\.?\\d*$") && java.lang.Float.parseFloat(value) <= 100) {
        examGrade.setScore(java.lang.Float.parseFloat(value))
      } else if (value.matches("^\\d*\\.?\\d*$") && java.lang.Float.parseFloat(value) > 100) {
        tr.addFailure("分数不能大于100", value)
      } else {
        examGrade.setScoreText(value)
        examGrade.setScore(calculator.gradeRateService.convert(examGrade.getScoreText, markStyle, 
          examGrade.getCourseGrade.getProject))
      }
    }
  }

  private def checkExamGradeExists(examGrades: Set[ExamGrade], gradeType: GradeType): ExamGrade = {
    var itor = examGrades.iterator()
    while (itor.hasNext) {
      val examGrade = itor.next()
      if (examGrade.gradeType.id == gradeType.id) {
        return examGrade
      }
    }
    Model.newInstance(classOf[ExamGrade])
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setProject(project: Project) {
    this.project = project
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }
}
