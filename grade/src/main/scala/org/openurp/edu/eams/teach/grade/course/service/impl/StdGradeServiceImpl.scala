package org.openurp.edu.eams.teach.grade.course.service.impl

import java.util.Date

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.course.service.StdGradeService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants



class StdGradeServiceImpl extends StdGradeService {

  private var entityDao: EntityDao = _

  def getStdByCode(stdCode: String, 
      project: Project, 
      departments: List[Department], 
      entityDao: EntityDao): Student = {
    val query = OqlBuilder.from(classOf[Student], "std")
    query.where("std.code=:code", stdCode)
    if (project == null || CollectUtils.isEmpty(departments)) {
      query.where("std is null")
    } else {
      query.where("std.project = :project", project)
      query.where("std.department in (:departments)", departments)
    }
    val stds = entityDao.search(query)
    if (CollectUtils.isEmpty(stds)) {
      return null
    }
    if (stds.size == 1) {
      stds.get(0)
    } else {
      throw new RuntimeException("数据异常")
    }
  }

  def buildGradeTypeQuery(): OqlBuilder[GradeType] = {
    val query = OqlBuilder.from(classOf[GradeType], "gradeType")
    query.where("gradeType.id not in (:ids)", Array(GradeTypeConstants.FINAL_ID))
    query.where("gradeType.effectiveAt <= :now and (gradeType.invalidAt is null or gradeType.invalidAt >= :now)", 
      new Date())
    query.orderBy(Order.parse("gradeType.code asc"))
    query
  }

  def buildGrade(grade: CourseGrade, gradeType: GradeType, markStyle: ScoreMarkStyle): ExamGrade = {
    var examGrade = grade.getExamGrade(gradeType)
    if (null != examGrade) {
      return examGrade
    }
    examGrade = Model.newInstance(classOf[ExamGrade])
    examGrade.setMarkStyle(markStyle)
    examGrade.setExamStatus(new ExamStatus(ExamStatus.NORMAL))
    examGrade.setCourseGrade(grade)
    examGrade.setGradeType(gradeType)
    examGrade.setCreatedAt(new Date())
    examGrade.setUpdatedAt(new Date())
    examGrade.setStatus(grade.getStatus)
    grade.addExamGrade(examGrade)
    examGrade
  }

  def getStatus(lessonNo: String, 
      stdId: String, 
      semesterId: String, 
      entityDao: EntityDao): Array[Any] = {
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.lesson.no = :lessonNo", lessonNo)
    query.where("take.std.id = :stdId", new java.lang.Long(stdId))
    query.where("not exists(from org.openurp.edu.teach.grade.CourseGrade grade where grade.std.id = :stdId and grade.lesson.no=:lessonNo)", 
      new java.lang.Long(stdId), lessonNo)
    query.where("take.lesson.semester.id = :semesterId", new java.lang.Long(semesterId))
    query.select("take.lesson.id,take.lesson.course.code,take.lesson.course.name,take.lesson.gradeState.markStyle.id,take.lesson.gradeState.markStyle.name")
    val takes = entityDao.search(query)
    if (CollectUtils.isEmpty(takes)) {
      return null
    }
    if (takes.size == 1) {
      takes.get(0).asInstanceOf[Array[Any]]
    } else {
      throw new RuntimeException("数据异常")
    }
  }

  def checkStdGradeExists(std: Student, 
      semester: Semester, 
      course: Course, 
      project: Project): Boolean = {
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.semester = :semester", semester)
    builder.where("courseGrade.project = :project", project)
    builder.where("courseGrade.std = :student", std)
    builder.where("courseGrade.course = :course", course)
    val courseGrades = entityDao.search(builder)
    if (courseGrades.size > 0) {
      return true
    }
    false
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
