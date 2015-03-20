package org.openurp.edu.eams.teach.grade.course.web.action


import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Department
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class StdGradeSearchAction extends SemesterSupportAction {

  protected var courseGradeService: CourseGradeService = _

  protected override def indexSetting() {
    put("courseTakeTypes", baseCodeService.getCodes(classOf[CourseTakeType]))
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
  }

  def search(): String = {
    put("courseGrades", entityDao.search(buildGradeQuery()))
    forward()
  }

  protected def buildGradeQuery(): OqlBuilder[CourseGrade] = {
    val project = getProject
    val departments = getDeparts
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    populateConditions(query)
    if (project == null) {
      query.where("courseGrade is null")
    } else {
      val scoreFrom = getFloat("scoreFrom")
      if (null != scoreFrom) {
        query.where("courseGrade.score>=:scoreFrom", scoreFrom)
      }
      val scoreTo = getFloat("scoreTo")
      if (null != scoreTo) {
        query.where("courseGrade.score<=:scoreTo", scoreTo)
      }
      val isPassed = getInt("isPass")
      if (null != isPassed) {
        if (isPassed.intValue() == 1) {
          query.where("courseGrade.passed = true")
        } else if (isPassed.intValue() == 0) {
          query.where("courseGrade.passed = false")
        } else if (isPassed.intValue() == 3) {
          val hql = "courseGrade.passed = false and not exists(from " + classOf[CourseGrade].getName + 
            " cg where cg.std = courseGrade.std and cg.course = courseGrade.course and cg.passed = true)"
          query.where(hql)
        }
      }
      query.where("courseGrade.project =:project", project)
      query.where("courseGrade.std.department in (:departments)", departments)
    }
    query.limit(getPageLimit)
    query.orderBy(Order.parse(get("orderBy")))
    query
  }

  protected def getExportDatas(): Iterable[CourseGrade] = {
    entityDao.search(buildGradeQuery().limit(null))
  }

  def stdReport(): String = {
    val grade = entityDao.get(classOf[CourseGrade], getLong("courseGrade.id"))
    put("courseGrades", getCourseGrades(grade.getStd))
    put("std", grade.getStd)
    put("RESTUDY", CourseTakeType.RESTUDY)
    forward()
  }

  private def getCourseGrades(std: Student): List[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.std = :std", std)
    query.where("grade.status > 0")
    query.orderBy("grade.semester")
    entityDao.search(query)
  }

  def info(): String = {
    val courseGrade = entityDao.get(classOf[CourseGrade], getLong("courseGrade.id"))
    put("courseGrade", courseGrade)
    val gradeAlterations = Collections.emptyList()
    put("courseGradeAlterInfos", gradeAlterations)
    put("gradeState", courseGradeService.getState(courseGrade.getLesson))
    put("examGradeAlterInfos", gradeAlterations)
    forward()
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }
}
